package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiOreDictExport;
import extracells.render.TextureManager;
import extracells.util.ItemUtils;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class PartOreDictExporter extends PartECBase implements IGridTickable {

	private String filter = "";

	private Predicate<AEItemStack> filterPredicate = null;

	@Override
	public int cableConnectionRenderTo() {
		return 5;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		updateFilter();
		saveData();
	}
	private static class OreListMatcher implements Predicate<AEItemStack>	{
		HashSet<AEItemStack> ores = new HashSet<>();
		public OreListMatcher(ArrayList<ItemStack> input){
			for (ItemStack is: input)
				ores.add(AEItemStack.create(is));
		}
		public boolean test(AEItemStack t) {
			return ores.contains(t);
		}
	}
	/**
	 * Call when the filter string has changed to parse and recompile the filter.
	 */
	private void updateFilter() {
		Predicate<ItemStack> matcher = null;
		if (filter.contains("\\")
				|| filter.contains("^")
				|| filter.contains("$")
				|| filter.contains("+")
				|| filter.contains("(")
				|| filter.contains(")")
				|| filter.contains("[")
				|| filter.contains("]"))
		{
			final Predicate<String> test = Pattern.compile(filter).asPredicate();
			matcher = (is) -> is != null &&
					IntStream.of(OreDictionary.getOreIDs(is))
							.mapToObj(OreDictionary::getOreName)
							.anyMatch(test);
		}
		else if (!this.filter.trim().isEmpty()) {
			String[] filters = this.filter.split("[&|]");
			String lastFilter = null;

			for (String filter : filters) {
				filter = filter.trim();
				boolean negated = filter.startsWith("!");
				if (negated)
					filter = filter.substring(1);

				Predicate<ItemStack> test = filterToItemStackPredicate(filter);

				if (negated)
					test = test.negate();

				if (matcher == null) {
					matcher = test;
					lastFilter = filter;
				} else {
					int endLast = this.filter.indexOf(lastFilter) + lastFilter.length();
					int startThis = this.filter.indexOf(filter);
					boolean or = this.filter.substring(endLast, startThis).contains("|");

					if (or) {
						matcher = matcher.or(test);
					} else {
						matcher = matcher.and(test);
					}
				}
			}
		}

		// Mod name and path evaluation are disabled in this version
		if (matcher != null && !this.filter.contains("@") && !this.filter.contains("~")) {
			ArrayList<ItemStack> filtered = new ArrayList<>();
			for (String name : OreDictionary.getOreNames())
				for (ItemStack s : OreDictionary.getOres(name))
					if (matcher.test(s))
						filtered.add(s);
			filterPredicate = new OreListMatcher(filtered);
		}
		else
			filterPredicate = null;
	}

	/**
	 * Given a filter string, returns a predicate that matches a given ItemStack
	 *
	 * @param filter Filter string.
	 * @return Predicate for filter string.
	 */
	private Predicate<ItemStack> filterToItemStackPredicate(String filter) {
		final Predicate<String> test = filterToPredicate(filter);
		return (is) -> is != null &&
				IntStream.of(OreDictionary.getOreIDs(is))
						.mapToObj(OreDictionary::getOreName)
						.anyMatch(test);
	}

	/**
	 * Given a filter string, returns a Predicate that matches a string.
	 *
	 * @param filter Filter string
	 * @return Predicate for filter string.
	 */
	private Predicate<String> filterToPredicate(String filter) {
		int numStars = StringUtils.countMatches(filter, "*");
		if (numStars == filter.length()) {
			return (str) -> true;
		} else if (filter.length() > 2 && filter.startsWith("*") && filter.endsWith("*") && numStars == 2) {
			final String pattern = filter.substring(1, filter.length() - 1);
			return (str) -> str.contains(pattern);
		} else if (filter.length() >= 2 && filter.startsWith("*") && numStars == 1) {
			final String pattern = filter.substring(1);
			return (str) -> str.endsWith(pattern);
		} else if (filter.length() >= 2 && filter.endsWith("*") && numStars == 1) {
			final String pattern = filter.substring(0, filter.length() - 1);
			return (str) -> str.startsWith(pattern);
		} else if (numStars == 0) {
			return (str) -> str.equals(filter);
		} else {
			String filterRegexFragment = filter.replace("*", ".*");
			String regexPattern = "^" + filterRegexFragment + "$";
			final Pattern pattern = Pattern.compile(regexPattern);
			return pattern.asPredicate();
		}
	}

	public boolean doWork(int rate, int ticksSinceLastCall) {
		int amount = Math.min(rate * ticksSinceLastCall, 64);
		IStorageGrid storage = getStorageGrid();
		assert storage != null;
		IMEMonitor<IAEItemStack> inv = storage.getItemInventory();
		MachineSource src = new MachineSource(this);

		if (this.filterPredicate != null) {
			IItemList<IAEItemStack> items = inv.getStorageList();
			for (IAEItemStack stack : items) {
				if (stack == null || !this.filterPredicate.test((AEItemStack)stack))
					continue;

				IAEItemStack toExtract = stack.copy();
				toExtract.setStackSize(amount);

				IAEItemStack extracted = inv.extractItems(toExtract, Actionable.SIMULATE, src);
				if (extracted != null) {
					IAEItemStack exported = exportStack(extracted.copy());
					if (exported != null) {
						inv.extractItems(exported, Actionable.MODULATE, src);
						return true;
					}
				}
			}
		}
		return false;
	}

	public IAEItemStack exportStack(IAEItemStack stack0) {
		if (this.tile == null || !this.tile.hasWorldObj() || stack0 == null)
			return null;
		ForgeDirection dir = getSide();
		TileEntity tile = this.tile.getWorldObj().getTileEntity(
				this.tile.xCoord + dir.offsetX, this.tile.yCoord + dir.offsetY,
				this.tile.zCoord + dir.offsetZ);
		if (tile == null)
			return null;
		IAEItemStack stack = stack0.copy();
		if (tile instanceof IInventory) {
			if (tile instanceof ISidedInventory) {
				ISidedInventory inv = (ISidedInventory) tile;
				for (int i : inv.getAccessibleSlotsFromSide(dir.getOpposite()
						.ordinal())) {
					if (inv.canInsertItem(i, stack.getItemStack(), dir
							.getOpposite().ordinal())) {
						if (inv.getStackInSlot(i) == null) {
							inv.setInventorySlotContents(i,
									stack.getItemStack());
							return stack0;
						} else if (ItemUtils.areItemEqualsIgnoreStackSize(
								inv.getStackInSlot(i), stack.getItemStack())) {
							int max = inv.getInventoryStackLimit();
							int current = inv.getStackInSlot(i).stackSize;
							int outStack = (int) stack.getStackSize();
							if (max == current)
								continue;
							ItemStack s = inv.getStackInSlot(i).copy();
							if (current + outStack <= max) {
								s.stackSize = s.stackSize + outStack;
								inv.setInventorySlotContents(i, s);
								return stack0;
							} else {
								s.stackSize = max;
								inv.setInventorySlotContents(i, s);
								stack.setStackSize(max - current);
								return stack;
							}
						}
					}
				}
			} else {
				IInventory inv = (IInventory) tile;
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					if (inv.isItemValidForSlot(i, stack.getItemStack())) {
						if (inv.getStackInSlot(i) == null) {
							inv.setInventorySlotContents(i,
									stack.getItemStack());
							return stack0;
						} else if (ItemUtils.areItemEqualsIgnoreStackSize(
								inv.getStackInSlot(i), stack.getItemStack())) {
							int max = inv.getInventoryStackLimit();
							int current = inv.getStackInSlot(i).stackSize;
							int outStack = (int) stack.getStackSize();
							if (max == current)
								continue;
							ItemStack s = inv.getStackInSlot(i).copy();
							if (current + outStack <= max) {
								s.stackSize = s.stackSize + outStack;
								inv.setInventorySlotContents(i, s);
								return stack0;
							} else {
								s.stackSize = max;
								inv.setInventorySlotContents(i, s);
								stack.setStackSize(max - current);
								return stack;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(6, 6, 12, 10, 10, 13);
		bch.addBox(4, 4, 13, 12, 12, 14);
		bch.addBox(5, 5, 14, 11, 11, 15);
		bch.addBox(6, 6, 15, 10, 10, 16);
		bch.addBox(6, 6, 11, 10, 10, 12);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiOreDictExport(player, this);
	}

	@Override
	public double getPowerUsage() {
		return 10.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerOreDictExport(player, this);
	}

	private IStorageGrid getStorageGrid() {
		IGridNode node = getGridNode();
		if (node == null)
			return null;
		IGrid grid = node.getGrid();
		if (grid == null)
			return null;
		return grid.getCache(IStorageGrid.class);
	}

	@Override
	public final TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public List<String> getWailaBodey(NBTTagCompound data, List<String> list) {
		super.getWailaBodey(data, list);
		if (data.hasKey("name"))
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.oredict")
					+ ": "
					+ data.getString("name"));
		else
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.oredict") + ":");
		return list;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		super.getWailaTag(tag);
		tag.setString("name", this.filter);
		return tag;
	}

	@MENetworkEventSubscribe
	public void powerChange(MENetworkPowerStatusChange event) {
		IGridNode node = getGridNode();
		if (node != null) {
			boolean isNowActive = node.isActive();
			if (isNowActive != isActive()) {
				setActive(isNowActive);
				onNeighborChanged();
				getHost().markForUpdate();
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if (data.hasKey("filter"))
			this.filter = data.getString("filter");
		updateFilter();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;
		rh.setTexture(TextureManager.EXPORT_SIDE.getTexture());
		rh.setBounds(6, 6, 12, 10, 10, 13);
		rh.renderInventoryBox(renderer);

		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5, 5, 14, 11, 11, 15);
		rh.renderInventoryBox(renderer);

		IIcon side = TextureManager.EXPORT_SIDE.getTexture();
		rh.setTexture(side, side, side,
				TextureManager.EXPORT_FRONT.getTexture(), side, side);
		rh.setBounds(6, 6, 15, 10, 10, 16);
		rh.renderInventoryBox(renderer);

		rh.setInvColor(AEColor.Black.mediumVariant);
		ts.setBrightness(15 << 20 | 15 << 4);
		rh.renderInventoryFace(TextureManager.EXPORT_FRONT.getTextures()[1],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(6, 6, 11, 10, 10, 12);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;
		rh.setTexture(TextureManager.EXPORT_SIDE.getTexture());
		rh.setBounds(6, 6, 12, 10, 10, 13);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(5, 5, 14, 11, 11, 15);
		rh.renderBlock(x, y, z, renderer);

		IIcon side = TextureManager.EXPORT_SIDE.getTexture();
		rh.setTexture(side, side, side,
				TextureManager.EXPORT_FRONT.getTextures()[0], side, side);
		rh.setBounds(6, 6, 15, 10, 10, 16);
		rh.renderBlock(x, y, z, renderer);

		ts.setColorOpaque_I(AEColor.Black.mediumVariant);
		if (isActive())
			ts.setBrightness(15 << 20 | 15 << 4);
		rh.renderFace(x, y, z, TextureManager.EXPORT_FRONT.getTextures()[1],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(6, 6, 11, 10, 10, 12);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	@Override
	public final TickRateModulation tickingRequest(IGridNode node,
			int TicksSinceLastCall) {
		if (isActive())
			return doWork(10, TicksSinceLastCall) ? TickRateModulation.FASTER
					: TickRateModulation.SLOWER;
		return TickRateModulation.SLOWER;
	}

	@MENetworkEventSubscribe
	public void updateChannels(MENetworkChannelsChanged channel) {
		IGridNode node = getGridNode();
		if (node != null) {
			boolean isNowActive = node.isActive();
			if (isNowActive != isActive()) {
				setActive(isNowActive);
				onNeighborChanged();
				getHost().markForUpdate();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setString("filter", this.filter);
	}
}

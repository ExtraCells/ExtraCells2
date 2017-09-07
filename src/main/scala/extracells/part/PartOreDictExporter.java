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
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiOreDictExport;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
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

import java.util.List;

public class PartOreDictExporter extends PartECBase implements IGridTickable {

	public String filter = "";

	@Override
	public int cableConnectionRenderTo() {
		return 5;
	}

	private boolean checkItem(IAEItemStack s) {
		if (s == null || this.filter.equals(""))
			return false;

		int[] ids = OreDictionary.getOreIDs(s.getItemStack());
		GameRegistry.UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(s.getItem());

		String[] filters = StringUtils.split(this.filter, '&');

		boolean result = true;
		for (String filter : filters) {
			filter = filter.trim();

			boolean exclude = filter.startsWith("!");
			if (exclude) {
				filter = filter.substring(1);
			}

			if (filter.startsWith("@")) {
				filter = filter.substring(1);
				if (!CheckFilter(exclude, filter, identifier.modId)) result = false;
			} else if (filter.startsWith("~")) {
				filter = filter.substring(1);
				if (!CheckFilter(exclude, filter, identifier.name)) result = false;
			} else {
				if (ids.length == 0) {
					result = false;
				} else {
					boolean oreDictResult = true;
					for (int id : ids) {
						if (!CheckFilter(exclude, filter, OreDictionary.getOreName(id))) {
							oreDictResult = false;
						} else if (!exclude) {
							oreDictResult = true;
							break;
						}
					}

					if (!oreDictResult) result = false;
				}
			}
		}

		return result;
	}

	private boolean CheckFilter(Boolean exclude, String filter, String name) {
		if (filter.startsWith("*") && filter.endsWith("*")) {
			filter = filter.replace("*", "");
			if (filter.equals(""))
				return true;

			if (exclude && name.contains(filter))
				return false;
			if (!exclude && !name.contains(filter))
				return false;
		} else if (filter.startsWith("*")) {
			filter = filter.replace("*", "");
			if (exclude && name.endsWith(filter))
				return false;
			if (!exclude && !name.endsWith(filter))
				return false;
		} else if (filter.endsWith("*")) {
			filter = filter.replace("*", "");
			if (exclude && name.startsWith(filter))
				return false;
			if (!exclude && !name.startsWith(filter))
				return false;
		} else {
			if (exclude && name.equals(filter))
				return false;
			if (!exclude && !name.equals(filter))
				return false;
		}

		return true;
	}

	public boolean doWork(int rate, int TicksSinceLastCall) {
		int amount = rate * TicksSinceLastCall >= 64 ? 64 : rate
				* TicksSinceLastCall;
		IStorageGrid storage = getStorageGrid();
		IAEItemStack stack = null;
		for (IAEItemStack s : storage.getItemInventory().getStorageList()) {
			if (checkItem(s.copy())) {
				stack = s.copy();
				break;
			}
		}
		if (stack == null)
			return false;
		stack.setStackSize(amount);
		stack = storage.getItemInventory().extractItems(stack.copy(),
				Actionable.SIMULATE, new MachineSource(this));
		if (stack == null)
			return false;
		IAEItemStack exported = exportStack(stack.copy());
		if (exported == null)
			return false;
		storage.getItemInventory().extractItems(exported, Actionable.MODULATE,
				new MachineSource(this));
		return true;
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
							if (current + outStack <= max) {
								ItemStack s = inv.getStackInSlot(i).copy();
								s.stackSize = s.stackSize + outStack;
								inv.setInventorySlotContents(i, s);
								return stack0;
							} else {
								ItemStack s = inv.getStackInSlot(i).copy();
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
							if (current + outStack <= max) {
								ItemStack s = inv.getStackInSlot(i).copy();
								s.stackSize = s.stackSize + outStack;
								inv.setInventorySlotContents(i, s);
								return stack0;
							} else {
								ItemStack s = inv.getStackInSlot(i).copy();
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
	public ItemStack getItemStack(PartItemStack type) {
		ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1,
				PartEnum.getPartID(this));
		if (type != PartItemStack.Break) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("filter", this.filter);
			is.setTagCompound(tag);
		}
		return is;
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

package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.api.IFluidInterface;
import extracells.api.crafting.IFluidCraftingPatternDetails;
import extracells.container.ContainerFluidInterface;
import extracells.container.IContainerListener;
import extracells.crafting.CraftingPattern;
import extracells.crafting.CraftingPattern2;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.render.TextureManager;
import extracells.util.EmptyMeItemMonitor;
import extracells.util.ItemUtils;
import extracells.util.PermissionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PartFluidInterface extends PartECBase implements IFluidHandler,
		IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable,
		IStorageMonitorable, IGridTickable, ICraftingProvider {

	private class FluidInterfaceInventory implements IInventory {

		private ItemStack[] inv = new ItemStack[9];

		@Override
		public void closeInventory() {}

		@Override
		public ItemStack decrStackSize(int slot, int amt) {
			ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				if (stack.stackSize <= amt) {
					setInventorySlotContents(slot, null);
				} else {
					stack = stack.splitStack(amt);
					if (stack.stackSize == 0) {
						setInventorySlotContents(slot, null);
					}
				}
			}
			PartFluidInterface.this.update = true;
			return stack;
		}

		@Override
		public String getInventoryName() {
			return "inventory.fluidInterface";
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public int getSizeInventory() {
			return this.inv.length;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.inv[slot];
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int slot) {
			return null;
		}

		@Override
		public boolean hasCustomInventoryName() {
			return false;
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			if (stack.getItem() instanceof ICraftingPatternItem) {
				IGridNode n = getGridNode();
				World w;
				if (n == null) {
					w = getClientWorld();
				} else {
					w = n.getWorld();
				}
				if (w == null)
					return false;
				ICraftingPatternDetails details = ((ICraftingPatternItem) stack
						.getItem()).getPatternForItem(stack, w);
				return details != null;
			}
			return false;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return PartFluidInterface.this.isValid();
		}

		@Override
		public void markDirty() {}

		@Override
		public void openInventory() {}

		public void readFromNBT(NBTTagCompound tagCompound) {

			NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if (slot >= 0 && slot < this.inv.length) {
					this.inv[slot] = ItemStack.loadItemStackFromNBT(tag);
				}
			}
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			this.inv[slot] = stack;
			if (stack != null && stack.stackSize > getInventoryStackLimit()) {
				stack.stackSize = getInventoryStackLimit();
			}
			PartFluidInterface.this.originalPatternsCache[slot] = null;
			PartFluidInterface.this.update = true;
		}

		public void writeToNBT(NBTTagCompound tagCompound) {
			NBTTagList itemList = new NBTTagList();
			for (int i = 0; i < this.inv.length; i++) {
				ItemStack stack = this.inv[i];
				if (stack != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setByte("Slot", (byte) i);
					stack.writeToNBT(tag);
					itemList.appendTag(tag);
				}
			}
			tagCompound.setTag("Inventory", itemList);
		}
	}

	List<IContainerListener> listeners = new ArrayList<IContainerListener>();
	private List<ICraftingPatternDetails> patternHandlers = new ArrayList<ICraftingPatternDetails>();
	private HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails> patternConvert = new HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>();
	private List<IAEItemStack> requestedItems = new ArrayList<IAEItemStack>();
	private List<IAEItemStack> removeList = new ArrayList<IAEItemStack>();
	private final ICraftingPatternDetails[] originalPatternsCache = new ICraftingPatternDetails[9];
	public final FluidInterfaceInventory inventory = new FluidInterfaceInventory();

	private boolean update = false;
	private List<IAEStack> export = new ArrayList<IAEStack>();
	private List<IAEStack> removeFromExport = new ArrayList<IAEStack>();

	private List<IAEStack> addToExport = new ArrayList<IAEStack>();
	private IAEItemStack toExport = null;

	private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orNull();
	private FluidTank tank = new FluidTank(10000) {
		@Override
		public FluidTank readFromNBT(NBTTagCompound nbt) {
			if (!nbt.hasKey("Empty")) {
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
				setFluid(fluid);
			} else {
				setFluid(null);
			}
			return this;
		}
	};
	private int fluidFilter = -1;
	public boolean doNextUpdate = false;
	private boolean needBreake = false;

	private int tickCount = 0;

	@Override
	public int cableConnectionRenderTo() {
		return 3;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		FluidStack tankFluid = this.tank.getFluid();
		return tankFluid != null && tankFluid.getFluid() == fluid;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return this.tank.fill(new FluidStack(fluid, 1), false) > 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		FluidStack tankFluid = this.tank.getFluid();
		if (resource == null || tankFluid == null
				|| tankFluid.getFluid() != resource.getFluid())
			return null;
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		FluidStack drained = this.tank.drain(maxDrain, doDrain);
		if (drained != null)
			getHost().markForUpdate();
		this.doNextUpdate = true;
		return drained;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null)
			return 0;

		if ((this.tank.getFluid() == null || this.tank.getFluid().getFluid() == resource
				.getFluid())
				&& resource.getFluid() == FluidRegistry
						.getFluid(this.fluidFilter)) {
			int added = this.tank.fill(resource.copy(), doFill);
			if (added == resource.amount) {
				this.doNextUpdate = true;
				return added;
			}
			added += fillToNetwork(new FluidStack(resource.getFluid(),
					resource.amount - added), doFill);
			this.doNextUpdate = true;
			return added;
		}

		int filled = 0;
		filled += fillToNetwork(resource, doFill);

		if (filled < resource.amount)
			filled += this.tank.fill(new FluidStack(resource.getFluid(),
					resource.amount - filled), doFill);
		if (filled > 0)
			getHost().markForUpdate();
		this.doNextUpdate = true;
		return filled;
	}

	public int fillToNetwork(FluidStack resource, boolean doFill) {
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if (node == null || resource == null)
			return 0;
		IGrid grid = node.getGrid();
		if (grid == null)
			return 0;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if (storage == null)
			return 0;
		IAEFluidStack notRemoved;
		FluidStack copy = resource.copy();
		if (doFill) {
			notRemoved = storage.getFluidInventory().injectItems(
					AEApi.instance().storage().createFluidStack(resource),
					Actionable.MODULATE, new MachineSource(this));
		} else {
			notRemoved = storage.getFluidInventory().injectItems(
					AEApi.instance().storage().createFluidStack(resource),
					Actionable.SIMULATE, new MachineSource(this));
		}
		if (notRemoved == null)
			return resource.amount;
		return (int) (resource.amount - notRemoved.getStackSize());
	}

	private void forceUpdate() {
		getHost().markForUpdate();
		for (IContainerListener listener : this.listeners) {
			if (listener != null)
				listener.updateContainer();
		}
		this.doNextUpdate = false;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 12, 11, 11, 14);

	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiFluidInterface(player, this, getSide());
	}

	@SideOnly(Side.CLIENT)
	private World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
			ItemStack pattern = this.inventory.getStackInSlot(i);
			if (pattern != null)
				drops.add(pattern);
		}
	}



	@Override
	public Fluid getFilter(ForgeDirection side) {
		return FluidRegistry.getFluid(this.fluidFilter);
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
		if (getGridNode(ForgeDirection.UNKNOWN) == null)
			return null;
		IGrid grid = getGridNode(ForgeDirection.UNKNOWN).getGrid();
		if (grid == null)
			return null;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if (storage == null)
			return null;
		return storage.getFluidInventory();
	}

	@Override
	public IFluidTank getFluidTank(ForgeDirection side) {
		return this.tank;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
		return new EmptyMeItemMonitor();
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side,
			BaseActionSource src) {
		return this;
	}

	@Override
	public IInventory getPatternInventory() {
		return this.inventory;
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerFluidInterface(player, this);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[] { this.tank.getInfo() };
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 40, false, false);
	}

	@Override
	public List<String> getWailaBodey(NBTTagCompound tag, List<String> list) {
		FluidStack fluid = null;
		int id = -1;
		int amount = 0;
		if (tag.hasKey("fluidID") && tag.hasKey("amount")) {
			id = tag.getInteger("fluidID");
			amount = tag.getInteger("amount");
		}
		if (id != -1)
			fluid = new FluidStack(id, amount);
		if (fluid == null) {
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
					+ ": "
					+ StatCollector
							.translateToLocal("extracells.tooltip.empty1"));
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.amount")
					+ ": 0mB / 10000mB");
		} else {
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
					+ ": " + fluid.getLocalizedName());
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.amount")
					+ ": "
					+ fluid.amount + "mB / 10000mB");
		}
		return list;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		if (this.tank.getFluid() == null
				|| this.tank.getFluid().getFluid() == null)
			tag.setInteger("fluidID", -1);
		else
			tag.setInteger("fluidID", this.tank.getFluid().getFluidID());
		tag.setInteger("amount", this.tank.getFluidAmount());
		return tag;
	}

	@Override
	public void initializePart(ItemStack partStack) {
		if (partStack.hasTagCompound()) {
			readFilter(partStack.getTagCompound());
		}
	}

	@Override
	public boolean isBusy() {
		return !this.export.isEmpty();
	}

	private ItemStack makeCraftingPatternItem(ICraftingPatternDetails details) {
		if (details == null)
			return null;
		NBTTagList in = new NBTTagList();
		NBTTagList out = new NBTTagList();
		for (IAEItemStack s : details.getInputs()) {
			if (s == null)
				in.appendTag(new NBTTagCompound());
			else
				in.appendTag(s.getItemStack().writeToNBT(new NBTTagCompound()));
		}
		for (IAEItemStack s : details.getOutputs()) {
			if (s == null)
				out.appendTag(new NBTTagCompound());
			else
				out.appendTag(s.getItemStack().writeToNBT(new NBTTagCompound()));
		}
		NBTTagCompound itemTag = new NBTTagCompound();
		itemTag.setTag("in", in);
		itemTag.setTag("out", out);
		itemTag.setBoolean("crafting", details.isCraftable());
		ItemStack pattern = new ItemStack(this.encodedPattern);
		pattern.setTagCompound(itemTag);
		return pattern;
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
				(IPart) this)) {
			return super.onActivate(player, pos);
		}
		return false;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.patternHandlers = new ArrayList<ICraftingPatternDetails>();
		this.patternConvert.clear();

		if (!this.isActive()) {
			return;
		}
		ItemStack[] inv = this.inventory.inv;
		for (int i = 0, invLength = inv.length; i < invLength; i++) {
			ItemStack currentPatternStack = inv[i];
			if (currentPatternStack != null
					&& currentPatternStack.getItem() != null
					&& currentPatternStack.getItem() instanceof ICraftingPatternItem) {
				ICraftingPatternItem currentPattern = (ICraftingPatternItem) currentPatternStack
						.getItem();

				ICraftingPatternDetails originalPattern = originalPatternsCache[i];
				if (originalPattern == null) {
					originalPattern = currentPattern.getPatternForItem(currentPatternStack, getGridNode().getWorld());
					originalPatternsCache[i] = originalPattern;
				}
				if (originalPattern != null) {
					IFluidCraftingPatternDetails pattern = new CraftingPattern2(originalPattern);
					this.patternHandlers.add(pattern);
					ItemStack is = makeCraftingPatternItem(pattern);
					if (is == null)
						continue;
					ICraftingPatternDetails p = ((ICraftingPatternItem) is
							.getItem()).getPatternForItem(is, getGridNode()
							.getWorld());
					if (p == null)
						continue;
					this.patternConvert.put(p, pattern);
					craftingTracker.addCraftingOption(this, p);
				}
			}
		}
	}

	private void pushItems() {
		for (IAEStack s : this.removeFromExport) {
			this.export.remove(s);
		}
		this.removeFromExport.clear();
		for (IAEStack s : this.addToExport) {
			this.export.add(s);
		}
		this.addToExport.clear();
		if (getGridNode().getWorld() == null || this.export.isEmpty())
			return;
		ForgeDirection dir = getSide();
		TileEntity tile = getGridNode().getWorld().getTileEntity(
				getGridNode().getGridBlock().getLocation().x + dir.offsetX,
				getGridNode().getGridBlock().getLocation().y + dir.offsetY,
				getGridNode().getGridBlock().getLocation().z + dir.offsetZ);
		if (tile != null) {
			IAEStack stack0 = this.export.iterator().next();
			IAEStack stack = stack0.copy();
			if (stack instanceof IAEItemStack && tile instanceof IInventory) {
				if (tile instanceof ISidedInventory) {
					ISidedInventory inv = (ISidedInventory) tile;
					for (int i : inv.getAccessibleSlotsFromSide(dir
							.getOpposite().ordinal())) {
						if (inv.canInsertItem(i, ((IAEItemStack) stack)
								.getItemStack(), dir.getOpposite().ordinal())) {
							if (inv.getStackInSlot(i) == null) {
								inv.setInventorySlotContents(i,
										((IAEItemStack) stack).getItemStack());
								this.removeFromExport.add(stack0);
								return;
							} else if (ItemUtils.areItemEqualsIgnoreStackSize(
									inv.getStackInSlot(i),
									((IAEItemStack) stack).getItemStack())) {
								int max = inv.getInventoryStackLimit();
								int current = inv.getStackInSlot(i).stackSize;
								int outStack = (int) stack.getStackSize();
								if (max == current)
									continue;
								if (current + outStack <= max) {
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = s.stackSize + outStack;
									inv.setInventorySlotContents(i, s);
									this.removeFromExport.add(stack0);
									return;
								} else {
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = max;
									inv.setInventorySlotContents(i, s);
									this.removeFromExport.add(stack0);
									stack.setStackSize(outStack - max + current);
									this.addToExport.add(stack);
									return;
								}
							}
						}
					}
				} else {
					IInventory inv = (IInventory) tile;
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						if (inv.isItemValidForSlot(i,
								((IAEItemStack) stack).getItemStack())) {
							if (inv.getStackInSlot(i) == null) {
								inv.setInventorySlotContents(i,
										((IAEItemStack) stack).getItemStack());
								this.removeFromExport.add(stack0);
								return;
							} else if (ItemUtils.areItemEqualsIgnoreStackSize(
									inv.getStackInSlot(i),
									((IAEItemStack) stack).getItemStack())) {
								int max = inv.getInventoryStackLimit();
								int current = inv.getStackInSlot(i).stackSize;
								int outStack = (int) stack.getStackSize();
								if (max == current)
									continue;
								if (current + outStack <= max) {
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = s.stackSize + outStack;
									inv.setInventorySlotContents(i, s);
									this.removeFromExport.add(stack0);
									return;
								} else {
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = max;
									inv.setInventorySlotContents(i, s);
									this.removeFromExport.add(stack0);
									stack.setStackSize(outStack - max + current);
									this.addToExport.add(stack);
									return;
								}
							}
						}
					}
				}
			} else if (stack instanceof IAEFluidStack
					&& tile instanceof IFluidHandler) {
				IFluidHandler handler = (IFluidHandler) tile;
				IAEFluidStack fluid = (IAEFluidStack) stack;
				if (handler.canFill(dir.getOpposite(), fluid.copy().getFluid())) {
					int amount = handler.fill(dir.getOpposite(), fluid
							.getFluidStack().copy(), false);
					if (amount == 0)
						return;
					if (amount == fluid.getStackSize()) {
						handler.fill(dir.getOpposite(), fluid.getFluidStack()
								.copy(), true);
						this.removeFromExport.add(stack0);
					} else {
						IAEFluidStack f = fluid.copy();
						f.setStackSize(f.getStackSize() - amount);
						FluidStack fl = fluid.getFluidStack().copy();
						fl.amount = amount;
						handler.fill(dir.getOpposite(), fl, true);
						this.removeFromExport.add(stack0);
						this.addToExport.add(f);
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patDetails,
			InventoryCrafting table) {
		if (isBusy() || !this.patternConvert.containsKey(patDetails))
			return false;
		ICraftingPatternDetails patternDetails = this.patternConvert
				.get(patDetails);
		if (patternDetails instanceof CraftingPattern) {
			CraftingPattern patter = (CraftingPattern) patternDetails;
			HashMap<Fluid, Long> fluids = new HashMap<Fluid, Long>();
			for (IAEFluidStack stack : patter.getCondensedFluidInputs()) {
				if (fluids.containsKey(stack.getFluid())) {
					Long amount = fluids.get(stack.getFluid())
							+ stack.getStackSize();
					fluids.remove(stack.getFluid());
					fluids.put(stack.getFluid(), amount);
				} else {
					fluids.put(stack.getFluid(), stack.getStackSize());
				}
			}
			IGrid grid = getGridNode().getGrid();
			if (grid == null)
				return false;
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if (storage == null)
				return false;
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory()
						.extractItems(
								AEApi.instance()
										.storage()
										.createFluidStack(
												new FluidStack(fluid,
														(int) (amount + 0))),
								Actionable.SIMULATE, new MachineSource(this));
				if (extractFluid == null
						|| extractFluid.getStackSize() != amount) {
					return false;
				}
			}
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory()
						.extractItems(
								AEApi.instance()
										.storage()
										.createFluidStack(
												new FluidStack(fluid,
														(int) (amount + 0))),
								Actionable.MODULATE, new MachineSource(this));
				this.export.add(extractFluid);
			}
			for (IAEItemStack s : patter.getCondensedInputs()) {
				if (s == null)
					continue;
				if (s.getItem() == ItemEnum.FLUIDPATTERN.getItem()) {
					this.toExport = s.copy();
					continue;
				}
				this.export.add(s);
			}
		}
		return true;
	}

	public void readFilter(NBTTagCompound tag) {
		if (tag.hasKey("filter"))
			this.fluidFilter = tag.getInteger("filter");
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if (data.hasKey("tank"))
			this.tank.readFromNBT(data.getCompoundTag("tank"));
		if (data.hasKey("filter"))
			this.fluidFilter = data.getInteger("filter");
		if (data.hasKey("inventory"))
			this.inventory.readFromNBT(data.getCompoundTag("inventory"));
		if (data.hasKey("export"))
			readOutputFromNBT(data.getCompoundTag("export"));
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		NBTTagCompound tag = ByteBufUtils.readTag(data);
		if (tag.hasKey("tank"))
			this.tank.readFromNBT(tag.getCompoundTag("tank"));
		if (tag.hasKey("filter"))
			this.fluidFilter = tag.getInteger("filter");
		if (tag.hasKey("inventory"))
			this.inventory.readFromNBT(tag.getCompoundTag("inventory"));
		return true;
	}

	private void readOutputFromNBT(NBTTagCompound tag) {
		this.addToExport.clear();
		this.removeFromExport.clear();
		this.export.clear();
		int i = tag.getInteger("remove");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("remove-" + j + "-isItem")) {
				IAEItemStack s = AEApi
						.instance()
						.storage()
						.createItemStack(
								ItemStack.loadItemStackFromNBT(tag
										.getCompoundTag("remove-" + j)));
				s.setStackSize(tag.getLong("remove-" + j + "-amount"));
				this.removeFromExport.add(s);
			} else {
				IAEFluidStack s = AEApi
						.instance()
						.storage()
						.createFluidStack(
								FluidStack.loadFluidStackFromNBT(tag
										.getCompoundTag("remove-" + j)));
				s.setStackSize(tag.getLong("remove-" + j + "-amount"));
				this.removeFromExport.add(s);
			}
		}
		i = tag.getInteger("add");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("add-" + j + "-isItem")) {
				IAEItemStack s = AEApi
						.instance()
						.storage()
						.createItemStack(
								ItemStack.loadItemStackFromNBT(tag
										.getCompoundTag("add-" + j)));
				s.setStackSize(tag.getLong("add-" + j + "-amount"));
				this.addToExport.add(s);
			} else {
				IAEFluidStack s = AEApi
						.instance()
						.storage()
						.createFluidStack(
								FluidStack.loadFluidStackFromNBT(tag
										.getCompoundTag("add-" + j)));
				s.setStackSize(tag.getLong("add-" + j + "-amount"));
				this.addToExport.add(s);
			}
		}
		i = tag.getInteger("export");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("export-" + j + "-isItem")) {
				IAEItemStack s = AEApi
						.instance()
						.storage()
						.createItemStack(
								ItemStack.loadItemStackFromNBT(tag
										.getCompoundTag("export-" + j)));
				s.setStackSize(tag.getLong("export-" + j + "-amount"));
				this.export.add(s);
			} else {
				IAEFluidStack s = AEApi
						.instance()
						.storage()
						.createFluidStack(
								FluidStack.loadFluidStackFromNBT(tag
										.getCompoundTag("export-" + j)));
				s.setStackSize(tag.getLong("export-" + j + "-amount"));
				this.export.add(s);
			}
		}
	}

	public void registerListener(IContainerListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(IContainerListener listener) {
		this.listeners.remove(listener);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side,
				TextureManager.INTERFACE.getTextures()[0], side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		rh.renderInventoryFace(TextureManager.INTERFACE.getTextures()[0],
				ForgeDirection.SOUTH, renderer);

		rh.setTexture(side);
		rh.setBounds(5, 5, 12, 11, 11, 14);
		rh.renderInventoryBox(renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side,
				TextureManager.INTERFACE.getTextures()[0], side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		ts.setBrightness(20971520);
		rh.renderFace(x, y, z, TextureManager.INTERFACE.getTextures()[0],
				ForgeDirection.SOUTH, renderer);

		rh.setTexture(side);
		rh.setBounds(5, 5, 12, 11, 11, 14);
		rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void setFilter(ForgeDirection side, Fluid fluid) {
		if (fluid == null) {
			this.fluidFilter = -1;
			this.doNextUpdate = true;
			return;
		}
		this.fluidFilter = fluid.getID();
		this.doNextUpdate = true;

	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		setFilter(ForgeDirection.getOrientation(_index), _fluid);
	}

	@Override
	public void setFluidTank(ForgeDirection side, FluidStack fluid) {
		this.tank.setFluid(fluid);
		this.doNextUpdate = true;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node,
			int TicksSinceLastCall) {
		if (this.doNextUpdate)
			forceUpdate();
		IGrid grid = node.getGrid();
		if (grid == null)
			return TickRateModulation.URGENT;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if (storage == null)
			return TickRateModulation.URGENT;
		pushItems();
		if (this.toExport != null) {
			storage.getItemInventory().injectItems(this.toExport,
					Actionable.MODULATE, new MachineSource(this));
			this.toExport = null;
		}
		if (this.update) {
			this.update = false;
			if (getGridNode() != null && getGridNode().getGrid() != null) {
				getGridNode().getGrid()
						.postEvent(
								new MENetworkCraftingPatternChange(this,
										getGridNode()));
			}
		}
		if (this.tank.getFluid() != null
				&& FluidRegistry.getFluid(this.fluidFilter) != this.tank
						.getFluid().getFluid()) {
			FluidStack s = this.tank.drain(125, false);
			if (s != null) {
				IAEFluidStack notAdded = storage.getFluidInventory()
						.injectItems(
								AEApi.instance().storage()
										.createFluidStack(s.copy()),
								Actionable.SIMULATE, new MachineSource(this));
				if (notAdded != null) {
					int toAdd = (int) (s.amount - notAdded.getStackSize());
					storage.getFluidInventory().injectItems(
							AEApi.instance()
									.storage()
									.createFluidStack(
											this.tank.drain(toAdd, true)),
							Actionable.MODULATE, new MachineSource(this));
					this.doNextUpdate = true;
					this.needBreake = false;
				} else {
					storage.getFluidInventory().injectItems(
							AEApi.instance()
									.storage()
									.createFluidStack(
											this.tank.drain(s.amount, true)),
							Actionable.MODULATE, new MachineSource(this));
					this.doNextUpdate = true;
					this.needBreake = false;
				}
			}
		}
		if ((this.tank.getFluid() == null || this.tank.getFluid().getFluid() == FluidRegistry
				.getFluid(this.fluidFilter))
				&& FluidRegistry.getFluid(this.fluidFilter) != null) {
			IAEFluidStack extracted = storage.getFluidInventory().extractItems(
					AEApi.instance()
							.storage()
							.createFluidStack(
									new FluidStack(FluidRegistry
											.getFluid(this.fluidFilter), 125)),
					Actionable.SIMULATE, new MachineSource(this));
			if (extracted == null)
				return TickRateModulation.URGENT;
			int accepted = this.tank.fill(extracted.getFluidStack(), false);
			if (accepted == 0)
				return TickRateModulation.URGENT;
			this.tank
					.fill(storage
							.getFluidInventory()
							.extractItems(
									AEApi.instance()
											.storage()
											.createFluidStack(
													new FluidStack(
															FluidRegistry
																	.getFluid(this.fluidFilter),
															accepted)),
									Actionable.MODULATE,
									new MachineSource(this)).getFluidStack(),
							true);
			this.doNextUpdate = true;
			this.needBreake = false;
		}
		return TickRateModulation.URGENT;
	}

	public NBTTagCompound writeFilter(NBTTagCompound tag) {
		if (FluidRegistry.getFluid(this.fluidFilter) == null)
			return null;
		tag.setInteger("filter", this.fluidFilter);
		return tag;
	}

	private NBTTagCompound writeOutputToNBT(NBTTagCompound tag) {
		int i = 0;
		for (IAEStack s : this.removeFromExport) {
			if (s != null) {
				tag.setBoolean("remove-" + i + "-isItem", s.isItem());
				NBTTagCompound data = new NBTTagCompound();
				if (s.isItem()) {
					((IAEItemStack) s).getItemStack().writeToNBT(data);
				} else {
					((IAEFluidStack) s).getFluidStack().writeToNBT(data);
				}
				tag.setTag("remove-" + i, data);
				tag.setLong("remove-" + i + "-amount", s.getStackSize());
			}
			i++;
		}
		tag.setInteger("remove", this.removeFromExport.size());
		i = 0;
		for (IAEStack s : this.addToExport) {
			if (s != null) {
				tag.setBoolean("add-" + i + "-isItem", s.isItem());
				NBTTagCompound data = new NBTTagCompound();
				if (s.isItem()) {
					((IAEItemStack) s).getItemStack().writeToNBT(data);
				} else {
					((IAEFluidStack) s).getFluidStack().writeToNBT(data);
				}
                tag.setTag("add-" + i, data);
				tag.setLong("add-" + i + "-amount", s.getStackSize());
			}
			i++;
		}
		tag.setInteger("add", this.addToExport.size());
		i = 0;
		for (IAEStack s : this.export) {
			if (s != null) {
				tag.setBoolean("export-" + i + "-isItem", s.isItem());
				NBTTagCompound data = new NBTTagCompound();
				if (s.isItem()) {
					((IAEItemStack) s).getItemStack().writeToNBT(data);
				} else {
					((IAEFluidStack) s).getFluidStack().writeToNBT(data);
				}
				tag.setTag("export-" + i, data);
				tag.setLong("export-" + i + "-amount", s.getStackSize());
			}
			i++;
		}
		tag.setInteger("export", this.export.size());
		return tag;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		writeToNBTWithoutExport(data);
		NBTTagCompound tag = new NBTTagCompound();
		writeOutputToNBT(tag);
		data.setTag("export", tag);
	}

	public void writeToNBTWithoutExport(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("tank", this.tank.writeToNBT(new NBTTagCompound()));
		data.setInteger("filter", this.fluidFilter);
		NBTTagCompound inventory = new NBTTagCompound();
		this.inventory.writeToNBT(inventory);
		data.setTag("inventory", inventory);
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("tank", this.tank.writeToNBT(new NBTTagCompound()));
		tag.setInteger("filter", this.fluidFilter);
		NBTTagCompound inventory = new NBTTagCompound();
		this.inventory.writeToNBT(inventory);
		tag.setTag("inventory", inventory);
		ByteBufUtils.writeTag(data, tag);
	}
}

package extracells.tileentity;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import extracells.api.IECTileEntity;
import extracells.api.IFluidInterface;
import extracells.api.crafting.IFluidCraftingPatternDetails;
import extracells.container.IContainerListener;
import extracells.crafting.CraftingPattern;
import extracells.crafting.CraftingPattern2;
import extracells.gridblock.ECFluidGridBlock;
import extracells.integration.waila.IWailaTile;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.ItemEnum;
import extracells.util.EmptyMeItemMonitor;
import extracells.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityFluidInterface extends TileBase implements
		IActionHost, IFluidHandler, IECTileEntity, IFluidInterface,
		IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable,
		ICraftingProvider, IWailaTile {

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
			TileEntityFluidInterface.this.update = true;
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
				ICraftingPatternDetails details = ((ICraftingPatternItem) stack
						.getItem()).getPatternForItem(stack, getWorldObj());
				return details != null;
			}
			return false;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return true;
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
			TileEntityFluidInterface.this.originalPatternsCache[slot] = null;
			TileEntityFluidInterface.this.update = true;
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
	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	public FluidTank[] tanks = new FluidTank[6];
	public Integer[] fluidFilter = new Integer[this.tanks.length];
	public boolean doNextUpdate = false;
	private boolean wasIdle = false;
	private int tickCount = 0;
	private boolean update = false;
	private List<ICraftingPatternDetails> patternHandlers = new ArrayList<ICraftingPatternDetails>();
	private HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails> patternConvert = new HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>();
	private final ICraftingPatternDetails[] originalPatternsCache = new ICraftingPatternDetails[9];
	private List<IAEItemStack> requestedItems = new ArrayList<IAEItemStack>();
	private List<IAEItemStack> removeList = new ArrayList<IAEItemStack>();
	public final FluidInterfaceInventory inventory;
	private IAEItemStack toExport = null;

	private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern()
			.maybeItem().orNull();
	private List<IAEStack> export = new ArrayList<IAEStack>();

	private List<IAEStack> addToExport = new ArrayList<IAEStack>();

	private List<IAEItemStack> watcherList = new ArrayList<IAEItemStack>();

	private boolean isFirstGetGridNode = true;

	public TileEntityFluidInterface() {
		super();
		this.inventory = new FluidInterfaceInventory();
		this.gridBlock = new ECFluidGridBlock(this);
		for (int i = 0; i < this.tanks.length; i++) {
			this.tanks[i] = new FluidTank(10000) {
				@Override
				public FluidTank readFromNBT(NBTTagCompound nbt) {
					if (!nbt.hasKey("Empty")) {
						FluidStack fluid = FluidStack
								.loadFluidStackFromNBT(nbt);
						setFluid(fluid);
					} else {
						setFluid(null);
					}
					return this;
				}
			};
			this.fluidFilter[i] = -1;
		}
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if (from == ForgeDirection.UNKNOWN)
			return false;
		FluidStack tankFluid = this.tanks[from.ordinal()].getFluid();
		return tankFluid != null && tankFluid.getFluid() == fluid;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return from != ForgeDirection.UNKNOWN
				&& this.tanks[from.ordinal()].fill(new FluidStack(fluid, 1), false) > 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource,
			boolean doDrain) {
		FluidStack tankFluid = this.tanks[from.ordinal()].getFluid();
		if (resource == null || tankFluid == null
				|| tankFluid.getFluid() != resource.getFluid())
			return null;
		return drain(from, resource.amount, doDrain);

	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		if (from == ForgeDirection.UNKNOWN)
			return null;
		FluidStack drained = this.tanks[from.ordinal()]
				.drain(maxDrain, doDrain);
		if (drained != null)
			if (getWorldObj() != null)
				getWorldObj().markBlockForUpdate(this.xCoord, this.yCoord,
						this.zCoord);
		this.doNextUpdate = true;
		return drained;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (from == ForgeDirection.UNKNOWN || resource == null)
			return 0;

		if ((this.tanks[from.ordinal()].getFluid() == null || this.tanks[from
				.ordinal()].getFluid().getFluid() == resource.getFluid())
				&& resource.getFluid() == FluidRegistry
						.getFluid(this.fluidFilter[from.ordinal()])) {
			int added = this.tanks[from.ordinal()]
					.fill(resource.copy(), doFill);
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
			filled += this.tanks[from.ordinal()].fill(new FluidStack(
					resource.getFluid(), resource.amount - filled), doFill);
		if (filled > 0)
			if (getWorldObj() != null)
				getWorldObj().markBlockForUpdate(this.xCoord, this.yCoord,
						this.zCoord);
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
		getWorldObj().markBlockForUpdate(this.yCoord, this.yCoord, this.zCoord);
		for (IContainerListener listener : this.listeners) {
			if (listener != null)
				listener.updateContainer();
		}
		this.doNextUpdate = false;
	}

	@Override
	public IGridNode getActionableNode() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return null;
		if (this.node == null) {
			this.node = AEApi.instance().createGridNode(this.gridBlock);
		}
		return this.node;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		writeToNBTWithoutExport(nbtTag);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord,
				this.zCoord, 1, nbtTag);
	}

	@Override
	public Fluid getFilter(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN)
			return null;
		return FluidRegistry.getFluid(this.fluidFilter[side.ordinal()]);
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
		return getFluidInventory(ForgeDirection.UNKNOWN);
	}

	@Override
	public IFluidTank getFluidTank(ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN)
			return null;
		return this.tanks[side.ordinal()];
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (FMLCommonHandler.instance().getSide().isClient()
				&& (getWorldObj() == null || getWorldObj().isRemote))
			return null;
		if (this.isFirstGetGridNode) {
			this.isFirstGetGridNode = false;
			getActionableNode().updateState();
		}
		return this.node;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
		return new EmptyMeItemMonitor();
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
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
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		if (from == ForgeDirection.UNKNOWN)
			return null;
		return new FluidTankInfo[] { this.tanks[from.ordinal()].getInfo() };
	}

	@Override
	public List<String> getWailaBody(List<String> list, NBTTagCompound tag,
			ForgeDirection side) {
		if (side == null || side == ForgeDirection.UNKNOWN)
			return list;
		list.add(StatCollector.translateToLocal("extracells.tooltip.direction."
				+ side.ordinal()));
		FluidTank[] tanks = new FluidTank[6];
		for (int i = 0; i < tanks.length; i++) {
			tanks[i] = new FluidTank(10000) {
				@Override
				public FluidTank readFromNBT(NBTTagCompound nbt) {
					if (!nbt.hasKey("Empty")) {
						FluidStack fluid = FluidStack
								.loadFluidStackFromNBT(nbt);
						setFluid(fluid);
					} else {
						setFluid(null);
					}
					return this;
				}
			};
		}

		for (int i = 0; i < tanks.length; i++) {
			if (tag.hasKey("tank#" + i))
				tanks[i].readFromNBT(tag.getCompoundTag("tank#" + i));
		}
		FluidTank tank = tanks[side.ordinal()];
		if (tank == null || tank.getFluid() == null
				|| tank.getFluid().getFluid() == null) {
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
					+ ": "
					+ StatCollector
							.translateToLocal("extracells.tooltip.empty1"));
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.amount")
					+ ": 0mB / 10000mB");
		} else {
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
					+ ": " + tank.getFluid().getLocalizedName());
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.amount")
					+ ": "
					+ tank.getFluidAmount() + "mB / 10000mB");
		}
		return list;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		for (int i = 0; i < this.tanks.length; i++) {
			tag.setTag("tank#" + i,
					this.tanks[i].writeToNBT(new NBTTagCompound()));
		}
		return tag;
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
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.patternHandlers = new ArrayList<ICraftingPatternDetails>();
		this.patternConvert.clear();

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
					originalPattern = currentPattern.getPatternForItem(currentPatternStack, getWorldObj());
					originalPatternsCache[i] = originalPattern;
				}
				if (originalPattern != null) {
					IFluidCraftingPatternDetails pattern = new CraftingPattern2(originalPattern);
					this.patternHandlers.add(pattern);
					ItemStack is = makeCraftingPatternItem(pattern);
					if (is == null)
						continue;
					ICraftingPatternDetails p = ((ICraftingPatternItem) is
							.getItem()).getPatternForItem(is, getWorldObj());
					this.patternConvert.put(p, pattern);
					craftingTracker.addCraftingOption(this, p);
				}
			}
		}
	}

	private void pushItems() {
		this.export.addAll(this.addToExport);
		this.addToExport.clear();
		if (!hasWorldObj() || this.export.isEmpty())
			return;
		ForgeDirection[] directions = ForgeDirection.VALID_DIRECTIONS;
		for (ForgeDirection dir : directions) {
			TileEntity tile = getWorldObj().getTileEntity(
					this.xCoord + dir.offsetX, this.yCoord + dir.offsetY,
					this.zCoord + dir.offsetZ);
			if (tile != null) {
				IAEStack stack0 = this.export.get(0);
				IAEStack stack = stack0.copy();
				if (stack instanceof IAEItemStack && tile instanceof IInventory) {
					if (tile instanceof ISidedInventory) {
						ISidedInventory inv = (ISidedInventory) tile;
						for (int i : inv.getAccessibleSlotsFromSide(dir
								.getOpposite().ordinal())) {
							if (inv.canInsertItem(i, ((IAEItemStack) stack)
									.getItemStack(), dir.getOpposite()
									.ordinal())) {
								if (inv.getStackInSlot(i) == null) {
									inv.setInventorySlotContents(i,
											((IAEItemStack) stack)
													.getItemStack());
									this.export.remove(0);
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
										ItemStack s = inv.getStackInSlot(i)
												.copy();
										s.stackSize = s.stackSize + outStack;
										inv.setInventorySlotContents(i, s);
										this.export.remove(0);
										return;
									} else {
										ItemStack s = inv.getStackInSlot(i)
												.copy();
										s.stackSize = max;
										inv.setInventorySlotContents(i, s);
										this.export.get(0).setStackSize(outStack - max + current);
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
											((IAEItemStack) stack)
													.getItemStack());
									this.export.remove(0);
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
										ItemStack s = inv.getStackInSlot(i)
												.copy();
										s.stackSize = s.stackSize + outStack;
										inv.setInventorySlotContents(i, s);
										this.export.remove(0);
										return;
									} else {
										ItemStack s = inv.getStackInSlot(i)
												.copy();
										s.stackSize = max;
										inv.setInventorySlotContents(i, s);
										this.export.get(0).setStackSize(outStack - max + current);
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
					if (handler.canFill(dir.getOpposite(), fluid.copy()
							.getFluid())) {
						int amount = handler.fill(dir.getOpposite(), fluid
								.getFluidStack().copy(), false);
						if (amount == 0)
							continue;
						if (amount == fluid.getStackSize()) {
							handler.fill(dir.getOpposite(), fluid
									.getFluidStack().copy(), true);
							this.export.remove(0);
						} else {
							FluidStack fl = fluid.getFluidStack().copy();
							fl.amount = amount;
							this.export.get(0).setStackSize(fluid.getStackSize() - handler.fill(dir.getOpposite(), fl, true));
							return;
						}
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
			IGrid grid = this.node.getGrid();
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
				this.export.add(extractFluid.copy());
			}
			for (IAEItemStack s : patter.getCondensedInputs()) {
				if (s == null)
					continue;
				if (s.getItem() == ItemEnum.FLUIDPATTERN.getItem()) {
					this.toExport = s.copy();
					continue;
				}
				this.export.add(s.copy());
			}

		}
		return true;
	}

	public void readFilter(NBTTagCompound tag) {
		for (int i = 0; i < this.fluidFilter.length; i++) {
			if (tag.hasKey("fluid#" + i))
				this.fluidFilter[i] = tag.getInteger("fluid#" + i);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		for (int i = 0; i < this.tanks.length; i++) {
			if (tag.hasKey("tank#" + i))
				this.tanks[i].readFromNBT(tag.getCompoundTag("tank#" + i));
			if (tag.hasKey("filter#" + i))
				this.fluidFilter[i] = tag.getInteger("filter#" + i);
		}
		if (hasWorldObj()) {
			IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
			if (tag.hasKey("nodes") && node != null) {
				node.loadFromNBT("node0", tag.getCompoundTag("nodes"));
				node.updateState();
			}
		}
		if (tag.hasKey("inventory"))
			this.inventory.readFromNBT(tag.getCompoundTag("inventory"));
		if (tag.hasKey("export"))
			readOutputFromNBT(tag.getCompoundTag("export"));
	}

	private void readOutputFromNBT(NBTTagCompound tag) {
		this.addToExport.clear();
		this.export.clear();
		int i = tag.getInteger("add");
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

	@Override
	public void securityBreak() {

	}

	@Override
	public void setFilter(ForgeDirection side, Fluid fluid) {
		if (side == null || side == ForgeDirection.UNKNOWN)
			return;
		if (fluid == null) {
			this.fluidFilter[side.ordinal()] = -1;
			this.doNextUpdate = true;
			return;
		}
		this.fluidFilter[side.ordinal()] = fluid.getID();
		this.doNextUpdate = true;
	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		setFilter(ForgeDirection.getOrientation(_index), _fluid);
	}

	@Override
	public void setFluidTank(ForgeDirection side, FluidStack fluid) {
		if (side == null || side == ForgeDirection.UNKNOWN)
			return;
		this.tanks[side.ordinal()].setFluid(fluid);
		this.doNextUpdate = true;
	}

	private void tick() {
		if (this.tickCount >= 40 || !this.wasIdle) {
			this.tickCount = 0;
			this.wasIdle = true;
		} else {
			this.tickCount++;
			return;
		}
		if (this.node == null)
			return;
		IGrid grid = this.node.getGrid();
		if (grid == null)
			return;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if (storage == null)
			return;
		if (this.toExport != null) {
			storage.getItemInventory().injectItems(this.toExport,
					Actionable.MODULATE, new MachineSource(this));
			this.toExport = null;
		}
		for (int i = 0; i < this.tanks.length; i++) {
			if (this.tanks[i].getFluid() != null
					&& FluidRegistry.getFluid(this.fluidFilter[i]) != this.tanks[i]
							.getFluid().getFluid()) {
				FluidStack s = this.tanks[i].drain(125, false);
				if (s != null) {
					IAEFluidStack notAdded = storage.getFluidInventory()
							.injectItems(
									AEApi.instance().storage()
											.createFluidStack(s.copy()),
									Actionable.SIMULATE,
									new MachineSource(this));
					if (notAdded != null) {
						int toAdd = (int) (s.amount - notAdded.getStackSize());
						storage.getFluidInventory().injectItems(
								AEApi.instance()
										.storage()
										.createFluidStack(
												this.tanks[i]
														.drain(toAdd, true)),
								Actionable.MODULATE, new MachineSource(this));
						this.doNextUpdate = true;
						this.wasIdle = false;
					} else {
						storage.getFluidInventory().injectItems(
								AEApi.instance()
										.storage()
										.createFluidStack(
												this.tanks[i].drain(s.amount,
														true)),
								Actionable.MODULATE, new MachineSource(this));
						this.doNextUpdate = true;
						this.wasIdle = false;
					}
				}
			}
			if ((this.tanks[i].getFluid() == null || this.tanks[i].getFluid()
					.getFluid() == FluidRegistry.getFluid(this.fluidFilter[i]))
					&& FluidRegistry.getFluid(this.fluidFilter[i]) != null) {
				IAEFluidStack extracted = storage
						.getFluidInventory()
						.extractItems(
								AEApi.instance()
										.storage()
										.createFluidStack(
												new FluidStack(
														FluidRegistry
																.getFluid(this.fluidFilter[i]),
														125)),
								Actionable.SIMULATE, new MachineSource(this));
				if (extracted == null)
					continue;
				int accepted = this.tanks[i].fill(extracted.getFluidStack(),
						false);
				if (accepted == 0)
					continue;
				this.tanks[i]
						.fill(storage
								.getFluidInventory()
								.extractItems(
										AEApi.instance()
												.storage()
												.createFluidStack(
														new FluidStack(
																FluidRegistry
																		.getFluid(this.fluidFilter[i]),
																accepted)),
										Actionable.MODULATE,
										new MachineSource(this))
								.getFluidStack(), true);
				this.doNextUpdate = true;
				this.wasIdle = false;
			}
		}
	}

	@Override
	public void updateEntity() {
		if (getWorldObj() == null || getWorldObj().provider == null
				|| getWorldObj().isRemote)
			return;
		if (this.update) {
			this.update = false;
			if (getGridNode(ForgeDirection.UNKNOWN) != null
					&& getGridNode(ForgeDirection.UNKNOWN).getGrid() != null) {
				getGridNode(ForgeDirection.UNKNOWN).getGrid().postEvent(
						new MENetworkCraftingPatternChange(this,
								getGridNode(ForgeDirection.UNKNOWN)));
			}
		}
		pushItems();
		if (this.doNextUpdate)
			forceUpdate();
		tick();
	}

	public NBTTagCompound writeFilter(NBTTagCompound tag) {
		for (int i = 0; i < this.fluidFilter.length; i++) {
			tag.setInteger("fluid#" + i, this.fluidFilter[i]);
		}
		return tag;
	}

	private NBTTagCompound writeOutputToNBT(NBTTagCompound tag) {
		int i = 0;
		i = 0;
		for (IAEStack s : this.addToExport) {
			if (s != null) {
				tag.setBoolean("add-" + i + "-isItem", s.isItem());
				NBTTagCompound data = new NBTTagCompound();
				if (s.isItem()) {
					((IAEItemStack) s).getItemStack().writeToNBT(data);
				} else {
					((IAEFluidStack) s).getFluidStack().writeToNBT(data);
				};
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

	public void writeToNBTWithoutExport(NBTTagCompound tag) {
		super.writeToNBT(tag);
		for (int i = 0; i < this.tanks.length; i++) {
			tag.setTag("tank#" + i,
					this.tanks[i].writeToNBT(new NBTTagCompound()));
			tag.setInteger("filter#" + i, this.fluidFilter[i]);
		}
		if (!hasWorldObj())
			return;
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if (node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tag.setTag("nodes", nodeTag);
		}
		NBTTagCompound inventory = new NBTTagCompound();
		this.inventory.writeToNBT(inventory);
		tag.setTag("inventory", inventory);
	}
}

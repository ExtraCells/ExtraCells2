package extracells.tileentity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import appeng.api.storage.IStorageChannel;
import extracells.util.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.api.IFluidInterface;
import extracells.api.crafting.IFluidCraftingPatternDetails;
import extracells.container.IContainerListener;
import extracells.container.fluid.ContainerFluidInterface;
import extracells.crafting.CraftingPattern;
import extracells.crafting.CraftingPattern2;
import extracells.gridblock.ECFluidGridBlock;
import extracells.gui.fluid.GuiFluidInterface;
import extracells.gui.widget.fluid.IFluidSlotListener;
import extracells.integration.Capabilities;
import extracells.integration.waila.IWailaTile;
import extracells.network.IGuiProvider;
import extracells.registries.ItemEnum;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityFluidInterface extends TileBase implements
	IActionHost, IECTileEntity, IFluidInterface,
	IFluidSlotListener, IStorageMonitorable,
	ICraftingProvider, IWailaTile, ITickable, IGuiProvider {

	List<IContainerListener> listeners = new ArrayList<IContainerListener>();
	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	public FluidTank[] tanks = new FluidTank[6];
	public String[] fluidFilter = new String[this.tanks.length];
	public IFluidHandler[] fluidHandlers = new IFluidHandler[6];
	public boolean doNextUpdate = false;
	private boolean wasIdle = false;
	private int tickCount = 0;
	private boolean update = false;
	private List<ICraftingPatternDetails> patternHandlers = new ArrayList<ICraftingPatternDetails>();
	private HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails> patternConvert = new HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>();
	//private List<IAEItemStack> requestedItems = new ArrayList<IAEItemStack>();
	//private List<IAEItemStack> removeList = new ArrayList<IAEItemStack>();
	public final FluidInterfaceInventory inventory;
	private IAEItemStack toExport = null;

	private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern()
		.maybeItem().orElse(null);
	private List<IAEStack> export = new ArrayList<IAEStack>();
	private List<IAEStack> removeFromExport = new ArrayList<IAEStack>();

	private List<IAEStack> addToExport = new ArrayList<IAEStack>();

	//private List<IAEItemStack> watcherList = new ArrayList<IAEItemStack>();

	private boolean isFirstGetGridNode = true;
	private boolean markForSave = false;

	public TileEntityFluidInterface() {
		super();
		this.inventory = new FluidInterfaceInventory();
		this.gridBlock = new ECFluidGridBlock(this);
		for (int i = 0; i < this.tanks.length; i++) {
			FluidTank tank = this.tanks[i] = new FluidTank(10000) {
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

				@Override
				protected void onContentsChanged() {
					saveData();
				}
			};
			this.fluidFilter[i] = "";
			fluidHandlers[i] = new FluidHandler(tank, i);
		}
	}

	private void forceUpdate() {
		updateBlock();
		for (IContainerListener listener : this.listeners) {
			if (listener != null) {
				listener.updateContainer();
			}
		}
		this.doNextUpdate = false;
	}

	@Override
	public IGridNode getActionableNode() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return null;
		}
		if (this.node == null) {
			this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
		}
		return this.node;
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation aePartLocation) {
		return AECableType.DENSE_SMART;
	}

	@Override
	public Fluid getFilter(AEPartLocation side) {
		if (side == null || side == AEPartLocation.INTERNAL) {
			return null;
		}
		return FluidRegistry.getFluid(this.fluidFilter[side.ordinal()]);
	}

	@Override
	public IFluidTank getFluidTank(AEPartLocation side) {
		if (side == null || side == AEPartLocation.INTERNAL) {
			return null;
		}
		return this.tanks[side.ordinal()];
	}

	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		if (FMLCommonHandler.instance().getSide().isClient()
			&& (world == null || world.isRemote)) {
			return null;
		}
		if (this.isFirstGetGridNode) {
			this.isFirstGetGridNode = false;
			getActionableNode().updateState();
		}
		return this.node;
	}

	@Override
	public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> iStorageChannel) {
		if (iStorageChannel == StorageChannels.ITEM())
			return (IMEMonitor<T>) new EmptyMeItemMonitor();
		else
			return getInventory(AEPartLocation.INTERNAL, iStorageChannel);
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
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
	public List<String> getWailaBody(List<String> list, NBTTagCompound tag, EnumFacing side) {
		if (side == null) {
			return list;
		}
		list.add(I18n.translateToLocal("extracells.tooltip.direction."
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
			if (tag.hasKey("tank#" + i)) {
				tanks[i].readFromNBT(tag.getCompoundTag("tank#" + i));
			}
		}
		FluidTank tank = tanks[side.ordinal()];
		if (tank == null || tank.getFluid() == null
			|| tank.getFluid().getFluid() == null) {
			list.add(I18n.translateToLocal("extracells.tooltip.fluid")
				+ ": "
				+ I18n
				.translateToLocal("extracells.tooltip.empty1"));
			list.add(I18n
				.translateToLocal("extracells.tooltip.amount")
				+ ": 0mB / 10000mB");
		} else {
			list.add(I18n.translateToLocal("extracells.tooltip.fluid")
				+ ": " + tank.getFluid().getLocalizedName());
			list.add(I18n
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
		if (details == null) {
			return null;
		}
		NBTTagList in = new NBTTagList();
		NBTTagList out = new NBTTagList();
		for (IAEItemStack s : details.getInputs()) {
			if (s == null) {
				in.appendTag(new NBTTagCompound());
			} else {
				in.appendTag(s.createItemStack().writeToNBT(new NBTTagCompound()));
			}
		}
		for (IAEItemStack s : details.getOutputs()) {
			if (s == null) {
				out.appendTag(new NBTTagCompound());
			} else {
				out.appendTag(s.createItemStack().writeToNBT(new NBTTagCompound()));
			}
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
	public NBTTagCompound getUpdateTag() {
		return writeToNBTWithoutExport(new NBTTagCompound());
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.patternHandlers = new ArrayList<ICraftingPatternDetails>();
		this.patternConvert.clear();

		for (ItemStack currentPatternStack : this.inventory.inv) {
			if (currentPatternStack != null && !currentPatternStack.isEmpty()
				&& currentPatternStack.getItem() != null && currentPatternStack.getItem() instanceof ICraftingPatternItem) {
				ICraftingPatternItem currentPattern = (ICraftingPatternItem) currentPatternStack.getItem();

				if (currentPattern.getPatternForItem(currentPatternStack, world) != null) {
					IFluidCraftingPatternDetails pattern = new CraftingPattern2(currentPattern.getPatternForItem(currentPatternStack, world));
					this.patternHandlers.add(pattern);
					ItemStack is = makeCraftingPatternItem(pattern);
					if (is == null) {
						continue;
					}
					ICraftingPatternDetails p = ((ICraftingPatternItem) is
						.getItem()).getPatternForItem(is, world);
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
			markForSave = true;
		}
		this.removeFromExport.clear();
		for (IAEStack s : this.addToExport) {
			this.export.add(s);
			markForSave = true;
		}
		this.addToExport.clear();
		if (!hasWorld() || this.export.isEmpty()) {
			return;
		}
		EnumFacing[] facings = EnumFacing.VALUES;
		for (EnumFacing facing : facings) {
			TileEntity tile = getWorld().getTileEntity(pos.offset(facing));
			if (tile != null) {
				IAEStack stack0 = this.export.iterator().next();
				IAEStack stack = stack0.copy();
				if (stack instanceof IAEItemStack) {
					if(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())){
						IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
						ItemStack itemStack = ((IAEItemStack) stack).createItemStack();
						if (ItemHandlerUtil.insertItemStack(itemHandler, itemStack, false).isEmpty()){
							ItemHandlerUtil.insertItemStack(itemHandler, itemStack, true);
							this.removeFromExport.add(stack0);
							markForSave = true;
							return;
						}
					} else if (tile instanceof IInventory) {
						if (tile instanceof ISidedInventory) {
							ISidedInventory inv = (ISidedInventory) tile;
							for (int i : inv.getSlotsForFace(facing.getOpposite())) {
								if (inv.canInsertItem(i, ((IAEItemStack) stack).createItemStack(), facing.getOpposite())) {
									if (inv.getStackInSlot(i).isEmpty()) {
										inv.setInventorySlotContents(i, ((IAEItemStack) stack).createItemStack());
										this.removeFromExport.add(stack0);
										markForSave = true;
										return;
									} else if (ItemUtils.areItemEqualsIgnoreStackSize(inv.getStackInSlot(i), ((IAEItemStack) stack).createItemStack())) {
										int max = inv.getInventoryStackLimit();
										int current = inv.getStackInSlot(i).getCount();
										int outStack = (int) stack.getStackSize();
										if (max == current) {
											continue;
										}
										if (current + outStack <= max) {
											ItemStack s = inv.getStackInSlot(i).copy();
											s.setCount(s.getCount() + outStack);
											inv.setInventorySlotContents(i, s);
											this.removeFromExport.add(stack0);
											markForSave = true;
											return;
										} else {
											ItemStack s = inv.getStackInSlot(i).copy();
											s.setCount(max);
											inv.setInventorySlotContents(i, s);
											this.removeFromExport.add(stack0);
											stack.setStackSize(outStack - max + current);
											this.addToExport.add(stack);
											markForSave = true;
											return;
										}
									}
								}
							}
						} else {
							IInventory inv = (IInventory) tile;
							for (int i = 0; i < inv.getSizeInventory(); i++) {
								if (inv.isItemValidForSlot(i, ((IAEItemStack) stack).createItemStack())) {
									if (inv.getStackInSlot(i).isEmpty()) {
										inv.setInventorySlotContents(i, ((IAEItemStack) stack).createItemStack());
										this.removeFromExport.add(stack0);
										markForSave = true;
										return;
									} else if (ItemUtils.areItemEqualsIgnoreStackSize(inv.getStackInSlot(i), ((IAEItemStack) stack).createItemStack())) {
										int max = inv.getInventoryStackLimit();
										int current = inv.getStackInSlot(i).getCount();
										int outStack = (int) stack.getStackSize();
										if (max == current) {
											continue;
										}
										if (current + outStack <= max) {
											ItemStack s = inv.getStackInSlot(i).copy();
											s.setCount(s.getCount() + outStack);
											inv.setInventorySlotContents(i, s);
											this.removeFromExport.add(stack0);
											markForSave = true;
											return;
										} else {
											ItemStack s = inv.getStackInSlot(i).copy();
											s.setCount(max);
											inv.setInventorySlotContents(i, s);
											this.removeFromExport.add(stack0);
											markForSave = true;
											stack.setStackSize(outStack - max + current);
											this.addToExport.add(stack);
											markForSave = true;
											return;
										}
									}
								}
							}
						}
					}
				} else if (stack instanceof IAEFluidStack && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
					IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
					IAEFluidStack fluid = (IAEFluidStack) stack;
					int amount = handler.fill(fluid.getFluidStack().copy(), false);
					if (amount == 0) {
						continue;
					}
					if (amount == fluid.getStackSize()) {
						handler.fill(fluid.getFluidStack().copy(), true);
						this.removeFromExport.add(stack0);
						markForSave = true;
						return;
					} else {
						IAEFluidStack aeFluidStack = fluid.copy();
						aeFluidStack.setStackSize(aeFluidStack.getStackSize() - amount);
						FluidStack fluidStack = fluid.getFluidStack().copy();
						fluidStack.amount = amount;
						handler.fill(fluidStack, true);
						this.removeFromExport.add(stack0);
						this.addToExport.add(aeFluidStack);
						markForSave = true;
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patDetails,
		InventoryCrafting table) {
		boolean markForSave = false;
		if (isBusy() || !this.patternConvert.containsKey(patDetails)) {
			return false;
		}
		ICraftingPatternDetails patternDetails = this.patternConvert.get(patDetails);
		if (patternDetails instanceof CraftingPattern) {
			CraftingPattern patter = (CraftingPattern) patternDetails;
			HashMap<Fluid, Long> fluids = new HashMap<Fluid, Long>();
			for (IAEFluidStack stack : patter.getCondensedFluidInputs()) {
				if (fluids.containsKey(stack.getFluid())) {
					Long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
					fluids.remove(stack.getFluid());
					fluids.put(stack.getFluid(), amount);
				} else {
					fluids.put(stack.getFluid(), stack.getStackSize());
				}
			}
			IGrid grid = this.node.getGrid();
			if (grid == null) {
				return false;
			}
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if (storage == null) {
				return false;
			}
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getInventory(StorageChannels.FLUID()).extractItems(AEUtils.createFluidStack(
						new FluidStack(fluid, (int) (amount + 0))), Actionable.SIMULATE, new MachineSource(this));
				if (extractFluid == null || extractFluid.getStackSize() != amount) {
					return false;
				}
			}
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getInventory(StorageChannels.FLUID()).extractItems(AEUtils.createFluidStack(new FluidStack(fluid, (int) (amount + 0))), Actionable.MODULATE, new MachineSource(this));
				this.export.add(extractFluid);
				markForSave = true;
			}
			for (IAEItemStack s : patter.getCondensedInputs()) {
				if (s == null) {
					continue;
				}
				if (s.getItem() == ItemEnum.FLUIDPATTERN.getItem()) {
					this.toExport = s.copy();
					continue;
				}
				this.export.add(s);
				markForSave = true;
			}

		}
		if(markForSave)
			markDirty();
		return true;
	}

	public void readFilter(NBTTagCompound tag) {
		for (int i = 0; i < this.fluidFilter.length; i++) {
			if (tag.hasKey("fluid#" + i)) {
				this.fluidFilter[i] = tag.getString("fluid#" + i);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		for (int i = 0; i < this.tanks.length; i++) {
			if (tag.hasKey("tank#" + i)) {
				this.tanks[i].readFromNBT(tag.getCompoundTag("tank#" + i));
			}
			if (tag.hasKey("filter#" + i)) {
				this.fluidFilter[i] = tag.getString("filter#" + i);
			}
		}
		if (hasWorld()) {
			IGridNode node = getGridNode(AEPartLocation.INTERNAL);
			if (tag.hasKey("nodes") && node != null) {
				node.loadFromNBT("node0", tag.getCompoundTag("nodes"));
				node.updateState();
			}
		}
		if (tag.hasKey("inventory")) {
			this.inventory.readFromNBT(tag.getCompoundTag("inventory"));
		}
		if (tag.hasKey("export")) {
			readOutputFromNBT(tag.getCompoundTag("export"));
		}
	}

	private void readOutputFromNBT(NBTTagCompound tag) {
		this.addToExport.clear();
		this.removeFromExport.clear();
		this.export.clear();
		int i = tag.getInteger("remove");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("remove-" + j + "-isItem")) {
				IAEItemStack s = AEUtils.createItemStack(new ItemStack(tag.getCompoundTag("remove-" + j)));
				s.setStackSize(tag.getLong("remove-" + j + "-amount"));
				this.removeFromExport.add(s);
			} else {
				IAEFluidStack s = AEUtils.createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("remove-" + j)));
				s.setStackSize(tag.getLong("remove-" + j + "-amount"));
				this.removeFromExport.add(s);
			}
		}
		i = tag.getInteger("add");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("add-" + j + "-isItem")) {
				IAEItemStack s = AEUtils.createItemStack(new ItemStack(tag.getCompoundTag("add-" + j)));
				s.setStackSize(tag.getLong("add-" + j + "-amount"));
				this.addToExport.add(s);
			} else {
				IAEFluidStack s = AEUtils.createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("add-" + j)));
				s.setStackSize(tag.getLong("add-" + j + "-amount"));
				this.addToExport.add(s);
			}
		}
		i = tag.getInteger("export");
		for (int j = 0; j < i; j++) {
			if (tag.getBoolean("export-" + j + "-isItem")) {
				IAEItemStack s = AEUtils.createItemStack(new ItemStack(tag.getCompoundTag("export-" + j)));
				s.setStackSize(tag.getLong("export-" + j + "-amount"));
				this.export.add(s);
			} else {
				IAEFluidStack s = AEUtils.createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("export-" + j)));
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
	public void setFilter(AEPartLocation side, Fluid fluid) {
		if (side == null || side == AEPartLocation.INTERNAL) {
			return;
		}
		if (fluid == null) {
			this.fluidFilter[side.ordinal()] = "";
			this.doNextUpdate = true;
			return;
		}
		this.fluidFilter[side.ordinal()] = fluid.getName();
		this.doNextUpdate = true;
	}

	@Override
	public void setFluid(int index, Fluid fluid, EntityPlayer player) {
		setFilter(AEPartLocation.fromOrdinal(index), fluid);
	}

	@Override
	public void setFluidTank(AEPartLocation side, FluidStack fluid) {
		if (side == null || side == AEPartLocation.INTERNAL) {
			return;
		}
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
		if (this.node == null) {
			return;
		}
		IGrid grid = this.node.getGrid();
		if (grid == null) {
			return;
		}
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if (storage == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> fluidInv = storage.getInventory(StorageChannels.FLUID());
		IMEMonitor<IAEItemStack> itemInv = storage.getInventory(StorageChannels.ITEM());
		if (this.toExport != null) {
			itemInv.injectItems(this.toExport, Actionable.MODULATE, new MachineSource(this));
			this.toExport = null;
		}
		for (int i = 0; i < this.tanks.length; i++) {
			FluidTank tank = tanks[i];
			FluidStack containedFluid = tank.getFluid();
			String fluidFilter = this.fluidFilter[i];
			if (containedFluid != null && FluidRegistry.getFluid(fluidFilter) != containedFluid.getFluid()) {
				FluidStack fluidStack = tank.drain(125, false);
				if (fluidStack != null) {
					IAEFluidStack notAdded = fluidInv.injectItems(AEUtils.createFluidStack(fluidStack.copy()), Actionable.SIMULATE, new MachineSource(this));
					if (notAdded != null) {
						int toAdd = (int) (fluidStack.amount - notAdded.getStackSize());
						if (toAdd == 0)
							continue;
						fluidInv.injectItems(AEUtils.createFluidStack(tank.drain(toAdd, true)), Actionable.MODULATE, new MachineSource(this));
						this.doNextUpdate = true;
						this.wasIdle = false;
					} else {
						fluidInv.injectItems(AEUtils.createFluidStack(tank.drain(fluidStack.amount, true)), Actionable.MODULATE, new MachineSource(this));
						this.doNextUpdate = true;
						this.wasIdle = false;
					}
				}
			}
			if ((containedFluid == null
				|| containedFluid.getFluid() == FluidRegistry.getFluid(fluidFilter))
				&& FluidRegistry.getFluid(fluidFilter) != null) {
				IAEFluidStack extracted = fluidInv.extractItems(AEUtils.createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter), 125)), Actionable.SIMULATE, new MachineSource(this));
				if (extracted == null) {
					continue;
				}
				int accepted = tank.fill(extracted.getFluidStack(), false);
				if (accepted == 0) {
					continue;
				}
				tank.fill(fluidInv.extractItems(AEUtils.createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter), accepted)), Actionable.MODULATE, new MachineSource(this)).getFluidStack(), true);
				this.doNextUpdate = true;
				this.wasIdle = false;
			}
		}
	}

	@Override
	public void update() {
		if (world.isRemote) {
			return;
		}
		if (this.update) {
			this.update = false;
			IGridNode gridNode = getGridNode(AEPartLocation.INTERNAL);
			if (gridNode != null && gridNode.getGrid() != null) {
				gridNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, gridNode));
			}
		}
		pushItems();
		if (this.doNextUpdate) {
			forceUpdate();
		}
		tick();
		if (markForSave){
			markForSave = false;
			markDirty();
		}
	}

	public NBTTagCompound writeFilter(NBTTagCompound tag) {
		for (int i = 0; i < this.fluidFilter.length; i++) {
			tag.setString("fluid#" + i, this.fluidFilter[i]);
		}
		return tag;
	}

	private NBTTagCompound writeOutputToNBT(NBTTagCompound tag) {
		int i = 0;
		for (IAEStack s : this.removeFromExport) {
			if (s != null) {
				tag.setBoolean("remove-" + i + "-isItem", s.isItem());
				NBTTagCompound data = new NBTTagCompound();
				if (s.isItem()) {
					((IAEItemStack) s).createItemStack().writeToNBT(data);
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
					((IAEItemStack) s).createItemStack().writeToNBT(data);
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
					((IAEItemStack) s).createItemStack().writeToNBT(data);
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
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		writeToNBTWithoutExport(data);
		NBTTagCompound tag = new NBTTagCompound();
		writeOutputToNBT(tag);
		data.setTag("export", tag);
		return data;
	}

	public NBTTagCompound writeToNBTWithoutExport(NBTTagCompound tag) {
		super.writeToNBT(tag);
		for (int i = 0; i < this.tanks.length; i++) {
			tag.setTag("tank#" + i,
				this.tanks[i].writeToNBT(new NBTTagCompound()));
			tag.setString("filter#" + i, this.fluidFilter[i]);
		}
		if (!hasWorld()) {
			return tag;
		}
		IGridNode node = getGridNode(AEPartLocation.INTERNAL);
		if (node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tag.setTag("nodes", nodeTag);
		}
		NBTTagCompound inventory = new NBTTagCompound();
		this.inventory.writeToNBT(inventory);
		tag.setTag("inventory", inventory);
		return tag;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (facing == null)
			return null;
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandlers[facing.ordinal()]);
		}
		if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
			return Capabilities.STORAGE_MONITORABLE_ACCESSOR.cast((m) -> this);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (facing == null)
			return false;
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
			|| capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR
			|| super.hasCapability(capability, facing);
	}

	private class FluidInterfaceInventory implements IInventory {

		private ItemStack[] inv = new ItemStack[9];

		private FluidInterfaceInventory(){
			for(int i = 0; i < inv.length; i++){
				inv[i] = ItemStack.EMPTY;
			}
		}

		@Override
		public void closeInventory(EntityPlayer player) {
		}

		@Override
		public ItemStack decrStackSize(int slot, int amt) {
			ItemStack stack = getStackInSlot(slot);
			if (stack != null && !stack.isEmpty()) {
				if (stack.getCount() <= amt) {
					setInventorySlotContents(slot, ItemStack.EMPTY);
				} else {
					stack = stack.splitStack(amt);
					if (stack.getCount() == 0) {
						setInventorySlotContents(slot, ItemStack.EMPTY);
					}
				}
			}
			TileEntityFluidInterface.this.update = true;
			return stack;
		}

		@Override
		public String getName() {
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

		@Nullable
		@Override
		public ItemStack removeStackFromSlot(int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			if (stack.getItem() instanceof ICraftingPatternItem) {
				ICraftingPatternDetails details = ((ICraftingPatternItem) stack
					.getItem()).getPatternForItem(stack, getWorld());
				return details != null;
			}
			return false;
		}

		@Override
		public boolean isEmpty() {
			for (int i = 0; i < inv.length; i++){
				if(inv[i] != null && !inv[i].isEmpty())
					return true;
			}
			return false;
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void markDirty() {
		}

		@Override
		public void openInventory(EntityPlayer player) {
		}

		public void readFromNBT(NBTTagCompound tagCompound) {

			NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if (slot >= 0 && slot < this.inv.length) {
					this.inv[slot] = new ItemStack(tag);
				}
			}
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			this.inv[slot] = stack;
			if (stack != null && (!stack.isEmpty()) && stack.getCount() > getInventoryStackLimit()) {
				stack.setCount(getInventoryStackLimit());
			}
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

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {

		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {

		}

		@Override
		public ITextComponent getDisplayName() {
			return new TextComponentString(getName());
		}
	}

	//TODO: Clean Up
	private class FluidHandler implements IFluidHandler {
		FluidTank fluidTank;
		int filterIndex;

		public FluidHandler(FluidTank fluidTank, int filterIndex) {
			this.fluidTank = fluidTank;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			FluidStack tankFluid = fluidTank.getFluid();
			if (resource == null || tankFluid == null
				|| tankFluid.getFluid() != resource.getFluid()) {
				return null;
			}
			return drain(resource.amount, doDrain);

		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			FluidStack drained = fluidTank.drain(maxDrain, doDrain);
			if (drained != null) {
				if (world != null) {
					updateBlock();
				}
			}
			doNextUpdate = true;
			return drained;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return fluidTank.getTankProperties();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null) {
				return 0;
			}

			if ((fluidTank.getFluid() == null || fluidTank.getFluid().getFluid() == resource.getFluid())
				&& resource.getFluid() == FluidRegistry
				.getFluid(fluidFilter[filterIndex])) {
				int added = fluidTank.fill(resource.copy(), doFill);
				if (added == resource.amount) {
					doNextUpdate = true;
					return added;
				}
				added += fillToNetwork(new FluidStack(resource.getFluid(),
					resource.amount - added), doFill);
				doNextUpdate = true;
				return added;
			}

			int filled = 0;
			filled += fillToNetwork(resource, doFill);

			if (filled < resource.amount) {
				filled += fluidTank.fill(new FluidStack(
					resource.getFluid(), resource.amount - filled), doFill);
			}
			if (filled > 0) {
				if (world != null) {
					updateBlock();
				}
			}
			doNextUpdate = true;
			return filled;
		}

		public int fillToNetwork(FluidStack resource, boolean doFill) {
			IGridNode node = getGridNode(AEPartLocation.INTERNAL);
			if (node == null || resource == null) {
				return 0;
			}
			IGrid grid = node.getGrid();
			if (grid == null) {
				return 0;
			}
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if (storage == null) {
				return 0;
			}
			IAEFluidStack notRemoved;
			if (doFill) {
				notRemoved = storage.getInventory(StorageChannels.FLUID()).injectItems(
					StorageChannels.FLUID().createStack(resource),
					Actionable.MODULATE, new MachineSource(TileEntityFluidInterface.this));
			} else {
				notRemoved = storage.getInventory(StorageChannels.FLUID()).injectItems(
						StorageChannels.FLUID().createStack(resource),
					Actionable.SIMULATE, new MachineSource(TileEntityFluidInterface.this));
			}
			if (notRemoved == null) {
				return resource.amount;
			}
			return (int) (resource.amount - notRemoved.getStackSize());
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiFluidInterface(player, this);
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerFluidInterface(player, this);
	}
}

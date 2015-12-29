package extracells.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.Optional;
import extracells.api.IPortableGasStorageCell;
import extracells.api.IWirelessGasTermHandler;
import extracells.container.slot.SlotPlayerInventory;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiGasStorage;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.integration.Integration;
import extracells.inventory.HandlerItemStorageFluid;
import extracells.network.packet.part.PacketFluidStorage;
import extracells.util.FluidUtil;
import extracells.util.GasUtil;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

public class ContainerGasStorage extends Container implements
		IMEMonitorHandlerReceiver<IAEFluidStack>, IFluidSelectorContainer,
		IInventoryUpdateReceiver, IStorageContainer {

	private boolean isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled();
	private GuiGasStorage guiGasStorage;
	private IItemList<IAEFluidStack> fluidStackList;
	private Fluid selectedFluid;
	private IAEFluidStack selectedFluidStack;
	private EntityPlayer player;
	private IMEMonitor<IAEFluidStack> monitor;
	private HandlerItemStorageFluid storageFluid;
	private IWirelessGasTermHandler handler = null;
	private IPortableGasStorageCell storageCell = null;
	public boolean hasWirelessTermHandler = false;
	private ECPrivateInventory inventory = new ECPrivateInventory(
			"extracells.item.fluid.storage", 2, 64, this) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return GasUtil.isGasContainer(itemStack);
		}
	};

	private boolean doNextFill = false;

	public ContainerGasStorage(EntityPlayer _player) {
		this(null, _player);
	}

	public ContainerGasStorage(IMEMonitor<IAEFluidStack> _monitor,
							   EntityPlayer _player) {
		this.monitor = _monitor;
		this.player = _player;
		if (!this.player.worldObj.isRemote && this.monitor != null) {
			this.monitor.addListener(this, null);
			this.fluidStackList = this.monitor.getStorageList();
		} else {
			this.fluidStackList = AEApi.instance().storage().createFluidList();
		}

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26,
				92));

		bindPlayerInventory(this.player.inventory);
	}


	public ContainerGasStorage(IMEMonitor<IAEFluidStack> _monitor, EntityPlayer _player, IPortableGasStorageCell _storageCell) {
		this.hasWirelessTermHandler = _storageCell != null;
		this.storageCell = _storageCell;
		this.monitor = _monitor;
		this.player = _player;
		if (!this.player.worldObj.isRemote && this.monitor != null) {
			this.monitor.addListener(this, null);
			this.fluidStackList = this.monitor.getStorageList();
		} else {
			this.fluidStackList = AEApi.instance().storage().createFluidList();
		}

		// Input Slot accepts gas FluidContainers
		addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26,
				92));

		bindPlayerInventory(this.player.inventory);
	}

	public ContainerGasStorage(IMEMonitor<IAEFluidStack> _monitor,
							   EntityPlayer _player, IWirelessGasTermHandler _handler) {
		this.hasWirelessTermHandler = _handler != null;
		this.handler = _handler;
		this.monitor = _monitor;
		this.player = _player;
		if (!this.player.worldObj.isRemote && this.monitor != null) {
			this.monitor.addListener(this, null);
			this.fluidStackList = this.monitor.getStorageList();
		} else {
			this.fluidStackList = AEApi.instance().storage().createFluidList();
		}

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(this.player, this.inventory, 1, 26,
				92));

		bindPlayerInventory(this.player.inventory);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new SlotPlayerInventory(inventoryPlayer,
						this, j + i * 9 + 9, 8 + j * 18, i * 18 + 122));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new SlotPlayerInventory(inventoryPlayer, this,
					i, 8 + i * 18, 180));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void decreaseFirstSlot() {
		ItemStack slot = this.inventory.getStackInSlot(0);
		if (slot == null)
			return;
		slot.stackSize--;
		if (slot.stackSize <= 0)
			this.inventory.setInventorySlotContents(0, null);
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public void doWork() {
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot != null && secondSlot.stackSize > secondSlot.getMaxStackSize())
			return;
		ItemStack container = this.inventory.getStackInSlot(0);
		if (container == null)
			doNextFill = false;
		if (!GasUtil.isGasContainer(container))
			return;
		if (this.monitor == null)
			return;
		GasStack gasStack = GasUtil.getGasFromContainer(container);
		container = container.copy();
		container.stackSize = 1;
		if (GasUtil.isEmpty(container)  || (gasStack.amount < GasUtil.getCapacity(container) && GasUtil.getFluidStack(gasStack).getFluid() == this.selectedFluid && doNextFill)) {
			if (this.selectedFluid == null)
				return;
			int capacity = GasUtil.getCapacity(container);
			//Tries to simulate the extraction of fluid from storage.
			IAEFluidStack result = this.monitor.extractItems(FluidUtil.createAEFluidStack(this.selectedFluid, capacity), Actionable.SIMULATE, new PlayerSource(this.player, null));

			//Calculates the amount of fluid to fill container with.
			int proposedAmount = result == null ? 0 :  gasStack == null ? (int) Math.min(capacity, result.getStackSize()) : (int) Math.min(capacity - gasStack.amount, result.getStackSize());

			//Tries to fill the container with fluid.
			MutablePair<Integer, ItemStack> filledContainer = GasUtil.fillStack(container, GasUtil.getGasStack(new FluidStack(this.selectedFluid, proposedAmount)));

			GasStack gasStack2 = GasUtil.getGasFromContainer(filledContainer.getRight());

			//Moves it to second slot and commits extraction to grid.
			if (container.stackSize == 1 && gasStack2.amount < GasUtil.getCapacity(filledContainer.getRight())) {
				this.inventory.setInventorySlotContents(0, filledContainer.getRight());
				monitor.extractItems(FluidUtil.createAEFluidStack(this.selectedFluid, filledContainer.getLeft()), Actionable.MODULATE, new PlayerSource(this.player, null));
				doNextFill = true;

			}else if (fillSecondSlot(filledContainer.getRight())){
				monitor.extractItems(FluidUtil.createAEFluidStack(this.selectedFluid, filledContainer.getLeft()), Actionable.MODULATE, new PlayerSource(this.player, null));
				decreaseFirstSlot();
				doNextFill = false;
			}

		} else if (GasUtil.isFilled(container)) {
			GasStack containerGas = GasUtil.getGasFromContainer(container);

			MutablePair<Integer, ItemStack> drainedContainer =  GasUtil.drainStack(container.copy(), containerGas);
			GasStack gasStack1 = containerGas.copy();
			gasStack1.amount = drainedContainer.getLeft();

			//Tries to inject fluid to network.
			IAEFluidStack notInjected = this.monitor.injectItems(GasUtil.createAEFluidStack(gasStack1), Actionable.SIMULATE, new PlayerSource(this.player, null));
			if (notInjected != null)
				return;
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return;
				}
				this.handler.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			} else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return;
				}
				this.storageCell.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			}
			ItemStack emptyContainer  = drainedContainer.getRight();
			if(emptyContainer != null && GasUtil.getGasFromContainer(emptyContainer) != null && emptyContainer.stackSize == 1){
				monitor.injectItems(GasUtil.createAEFluidStack(gasStack1), Actionable.MODULATE, new PlayerSource(this.player, null));
				this.inventory.setInventorySlotContents(0, emptyContainer);
			}
			else if (emptyContainer == null || fillSecondSlot(drainedContainer.getRight())) {
				monitor.injectItems(GasUtil.createAEFluidStack(containerGas), Actionable.MODULATE, new PlayerSource(this.player, null));
				decreaseFirstSlot();
			}
		}
	}

	public boolean fillSecondSlot(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot == null) {
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return false;
				}
				this.handler.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			} else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return false;
				}
				this.storageCell.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			}
			this.inventory.setInventorySlotContents(1, itemStack);
			return true;
		} else {
			if (!secondSlot.isItemEqual(itemStack)
					|| !ItemStack.areItemStackTagsEqual(itemStack, secondSlot))
				return false;
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return false;
				}
				this.handler.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			}else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
						this.player.getCurrentEquippedItem())) {
					return false;
				}
				this.storageCell.usePower(this.player, 20.0D,
						this.player.getCurrentEquippedItem());
			}
			this.inventory.incrStackSize(1, itemStack.stackSize);
			return true;
		}
	}

	public void forceFluidUpdate() {
		if (this.monitor != null)
			new PacketFluidStorage(this.player, this.monitor.getStorageList())
					.sendPacketToPlayer(this.player);
		new PacketFluidStorage(this.player, this.hasWirelessTermHandler)
				.sendPacketToPlayer(this.player);
	}

	public IItemList<IAEFluidStack> getFluidStackList() {
		return this.fluidStackList;
	}

	public EntityPlayer getPlayer() {
		return this.player;
	}

	public Fluid getSelectedFluid() {
		return this.selectedFluid;
	}

	public IAEFluidStack getSelectedFluidStack() {
		return this.selectedFluidStack;
	}

	public boolean hasWirelessTermHandler() {
		return this.hasWirelessTermHandler;
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityPlayer) {
		super.onContainerClosed(entityPlayer);
		if (!entityPlayer.worldObj.isRemote) {
			this.monitor.removeListener(this);
			for (int i = 0; i < 2; i++)
				this.player.dropPlayerItemWithRandomChoice(
						((Slot) this.inventorySlots.get(i)).getStack(), false);
		}
	}

	@Override
	public void onInventoryChanged() {

	}

	@Override
	public void onListUpdate() {}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor,
			Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
		this.fluidStackList = ((IMEMonitor<IAEFluidStack>) monitor)
				.getStorageList();
		new PacketFluidStorage(this.player, this.fluidStackList)
				.sendPacketToPlayer(this.player);
		new PacketFluidStorage(this.player, this.hasWirelessTermHandler)
				.sendPacketToPlayer(this.player);
	}

	public void receiveSelectedFluid(Fluid _selectedFluid) {
		this.selectedFluid = _selectedFluid;
		if (this.selectedFluid != null) {
			for (IAEFluidStack stack : this.fluidStackList) {
				if (stack != null && stack.getFluid() == this.selectedFluid) {
					this.selectedFluidStack = stack;
					break;
				}
			}
		} else {
			this.selectedFluidStack = null;
		}
		if (this.guiGasStorage != null)
			this.guiGasStorage.updateSelectedFluid();
	}

	public void removeEnergyTick() {
		if (this.handler != null) {
			if (this.handler.hasPower(this.player, 1.0D,
					this.player.getCurrentEquippedItem())) {
				this.handler.usePower(this.player, 1.0D,
						this.player.getCurrentEquippedItem());
			}
		} else if (this.storageCell != null) {
			if (this.storageCell.hasPower(this.player, 0.5D,
					this.player.getCurrentEquippedItem())) {
				this.storageCell.usePower(this.player, 0.5D,
						this.player.getCurrentEquippedItem());
			}
		}
	}

	public void setGui(GuiGasStorage _guiGasStorage) {
		this.guiGasStorage = _guiGasStorage;
	}

	@Override
	public void setSelectedFluid(Fluid _selectedFluid) {
		new PacketFluidStorage(this.player, _selectedFluid)
				.sendPacketToServer();
		receiveSelectedFluid(_selectedFluid);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (this.inventory.isItemValidForSlot(0, itemstack1)) {
				if (slotnumber == 0 || slotnumber == 1) {
					if (!mergeItemStack(itemstack1, 2, 36, false))
						return null;
				} else if (!mergeItemStack(itemstack1, 0, 1, false)) {
					return null;
				}
				if (itemstack1.stackSize == 0) {
					slot.putStack(null);
				} else {
					slot.onSlotChanged();
				}
			} else {
				return null;
			}
		}
		return itemstack;
	}

	public void updateFluidList(IItemList<IAEFluidStack> _fluidStackList) {
		this.fluidStackList = _fluidStackList;
		if (this.guiGasStorage != null)
			this.guiGasStorage.updateFluids();
	}
}

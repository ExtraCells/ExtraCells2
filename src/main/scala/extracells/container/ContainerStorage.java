package extracells.container;

import appeng.api.networking.security.IActionSource;
import extracells.util.StorageChannels;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.api.IPortableStorageCell;
import extracells.api.IWirelessGasFluidTermHandler;
import extracells.container.slot.SlotOutput;
import extracells.container.slot.SlotPlayerInventory;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiStorage;
import extracells.inventory.IInventoryListener;
import extracells.inventory.InventoryPlain;
import extracells.inventory.cell.HandlerItemStorageFluid;
import extracells.network.packet.part.PacketStorageSelectFluid;
import extracells.network.packet.part.PacketStorageUpdateFluid;
import extracells.network.packet.part.PacketStorageUpdateState;
import extracells.util.GuiUtil;
import extracells.util.NetworkUtil;
import scala.xml.PrettyPrinter;

public abstract class ContainerStorage extends Container implements
	IMEMonitorHandlerReceiver<IAEFluidStack>, IFluidSelectorContainer,
	IInventoryListener, IStorageContainer, ITickContainer {

	protected EnumHand hand = EnumHand.MAIN_HAND;
	protected StorageType storageType;
	protected IItemList<IAEFluidStack> fluidStackList;
	protected Fluid selectedFluid;
	protected IAEFluidStack selectedFluidStack;
	protected EntityPlayer player;
	protected IMEMonitor<IAEFluidStack> monitor;
	protected HandlerItemStorageFluid storageFluid;
	protected IWirelessGasFluidTermHandler handler = null;
	protected IPortableStorageCell storageCell = null;
	public boolean hasWirelessTermHandler = false;
	protected InventoryPlain inventory;

	public ContainerStorage(StorageType storageType, EntityPlayer player, EnumHand hand) {
		this(storageType, null, player, hand);
	}

	public ContainerStorage(StorageType storageType, IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IPortableStorageCell storageCell, EnumHand hand) {
		this(storageType, monitor, player, hand);
		this.hasWirelessTermHandler = storageCell != null;
		this.storageCell = storageCell;
	}

	public ContainerStorage(StorageType storageType, IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IWirelessGasFluidTermHandler handler, EnumHand hand) {
		this(storageType, monitor, player, hand);
		this.hasWirelessTermHandler = handler != null;
		this.handler = handler;
	}

	public ContainerStorage(StorageType storageType, IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, EnumHand hand) {
		this.storageType = storageType;
		this.monitor = monitor;
		this.player = player;
		this.hand = hand;
		if (!this.player.world.isRemote && this.monitor != null) {
			this.monitor.addListener(this, null);
			this.fluidStackList = this.monitor.getStorageList();
		} else {
			this.fluidStackList = StorageChannels.FLUID().createList();
		}

		inventory = new InventoryPlain("extracells.item." + storageType.getName() + ".storage", 2, 64, this) {
			@Override
			public boolean isItemValidForSlot(int i, ItemStack itemStack) {
				return storageType.isContainer(itemStack);
			}
		};

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(this.inventory, 0, 8, 92));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotOutput(this.inventory, 1, 26, 92));

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
		if (slot == null || slot.isEmpty()) {
			return;
		}
		slot.setCount(slot.getCount() - 1);
		if (slot.getCount() <= 0) {
			this.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
		}
	}

	@Override
	public void onTick() {
		forceFluidUpdate();
		doWork();
	}

	public abstract void doWork();

	public boolean fillSecondSlot(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return false;
		}
		ItemStack handItem = player.getHeldItem(hand);
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot == null || secondSlot.isEmpty()) {
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
					handItem)) {
					return false;
				}
				this.handler.usePower(this.player, 20.0D,
					handItem);
			} else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
					handItem)) {
					return false;
				}
				this.storageCell.usePower(this.player, 20.0D,
					handItem);
			}
			this.inventory.setInventorySlotContents(1, itemStack);
			return true;
		} else {
			if (!secondSlot.isItemEqual(itemStack)
				|| !ItemStack.areItemStackTagsEqual(itemStack, secondSlot)) {
				return false;
			}
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
					handItem)) {
					return false;
				}
				this.handler.usePower(this.player, 20.0D,
					handItem);
			} else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
					handItem)) {
					return false;
				}
				this.storageCell.usePower(this.player, 20.0D,
					handItem);
			}
			this.inventory.incrStackSize(1, itemStack.getCount());
			return true;
		}
	}

	public void forceFluidUpdate() {
		if (this.monitor != null) {
			NetworkUtil.sendToPlayer(new PacketStorageUpdateFluid(this.monitor.getStorageList()), player);
		}
		NetworkUtil.sendToPlayer(new PacketStorageUpdateState(hasWirelessTermHandler), player);
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
		if (!entityPlayer.world.isRemote) {
			this.monitor.removeListener(this);
			for (int i = 0; i < 2; i++) {
				this.player.dropItem(this.inventorySlots.get(i).getStack(), false);
			}
		}
	}

	@Override
	public void onInventoryChanged() {
	}

	@Override
	public void onListUpdate() {
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource) {
		this.fluidStackList = ((IMEMonitor<IAEFluidStack>) monitor).getStorageList();
		NetworkUtil.sendToPlayer(new PacketStorageUpdateFluid(fluidStackList), player);
		NetworkUtil.sendToPlayer(new PacketStorageUpdateState(hasWirelessTermHandler), player);
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
		if (player.world.isRemote)
			updateGui();
	}

	@SideOnly(Side.CLIENT)
	private void updateGui() {
		GuiStorage guiStorage = GuiUtil.getGui(GuiStorage.class);
		if (guiStorage == null) {
			return;
		}
		guiStorage.updateSelectedFluid();
	}

	public void removeEnergyTick() {
		ItemStack handItem = player.getHeldItem(hand);
		if (this.handler != null) {
			if (this.handler.hasPower(this.player, 1.0D,
				handItem)) {
				this.handler.usePower(this.player, 1.0D,
					handItem);
			}
		} else if (this.storageCell != null) {
			if (this.storageCell.hasPower(this.player, 0.5D,
				handItem)) {
				this.storageCell.usePower(this.player, 0.5D,
					handItem);
			}
		}
	}

	@Override
	public void setSelectedFluid(Fluid fluid) {
		NetworkUtil.sendToServer(new PacketStorageSelectFluid(fluid));
		receiveSelectedFluid(fluid);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (this.inventory.isItemValidForSlot(0, itemstack1)) {
				if (slotnumber == 0 || slotnumber == 1) {
					if (!mergeItemStack(itemstack1, 2, 36, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!mergeItemStack(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
				if (itemstack1.getCount() == 0) {
					slot.putStack(ItemStack.EMPTY);
				} else {
					slot.onSlotChanged();
				}
			} else {
				return ItemStack.EMPTY;
			}
		}
		return itemstack;
	}

	public void updateFluidList(IItemList<IAEFluidStack> fluidStacks) {
		this.fluidStackList = fluidStacks;
	}

	public StorageType getStorageType() {
		return storageType;
	}
}

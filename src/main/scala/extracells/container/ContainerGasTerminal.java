package extracells.container;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiGasTerminal;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.network.packet.part.PacketFluidTerminal;
import extracells.part.PartFluidTerminal;
import extracells.part.PartGasTerminal;
import extracells.util.GasUtil;
import extracells.util.PermissionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

public class ContainerGasTerminal extends Container implements
		IMEMonitorHandlerReceiver<IAEFluidStack>, IFluidSelectorContainer {

	private PartGasTerminal terminal;
	private IMEMonitor<IAEFluidStack> monitor;
	private IItemList<IAEFluidStack> fluidStackList = AEApi.instance()
			.storage().createFluidList();
	private Fluid selectedFluid;
	private EntityPlayer player;
	private GuiGasTerminal guiGasTerminal;

	public ContainerGasTerminal(PartGasTerminal _terminal, EntityPlayer _player) {
		this.terminal = _terminal;
		this.player = _player;
		if (!this.player.worldObj.isRemote) {
			this.monitor = this.terminal.getGridBlock().getFluidMonitor();
			if (this.monitor != null) {
				this.monitor.addListener(this, null);
				this.fluidStackList = this.monitor.getStorageList();
			}
			this.terminal.addContainer(this);
		}

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(this.terminal.getInventory(), 0,
				8, 92));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(this.player,
				this.terminal.getInventory(), 1, 26, 92));
		bindPlayerInventory(this.player.inventory);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, i * 18 + 122));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 180));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return this.terminal != null && this.terminal.isValid();
	}

	public void forceFluidUpdate() {
		if (this.monitor != null) {
			new PacketFluidTerminal(this.player, this.monitor.getStorageList())
					.sendPacketToPlayer(this.player);
		}
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

	public PartFluidTerminal getTerminal() {
		return this.terminal;
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityPlayer) {
		super.onContainerClosed(entityPlayer);
		if (!entityPlayer.worldObj.isRemote) {
			if (this.monitor != null)
				this.monitor.removeListener(this);
			this.terminal.removeContainer(this);
		}
	}

	@Override
	public void onListUpdate() {

	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor,
			Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
		this.fluidStackList = ((IMEMonitor<IAEFluidStack>) monitor)
				.getStorageList();
		new PacketFluidTerminal(this.player, this.fluidStackList)
				.sendPacketToPlayer(this.player);
	}

	public void receiveSelectedFluid(Fluid _selectedFluid) {
		this.selectedFluid = _selectedFluid;
		if (this.guiGasTerminal != null)
			this.guiGasTerminal.updateSelectedFluid();
	}

	public void setGui(GuiGasTerminal _guiGasTerminal) {
		if (_guiGasTerminal != null)
			this.guiGasTerminal = _guiGasTerminal;
	}

	@Override
	public void setSelectedFluid(Fluid _selectedFluid) {
		new PacketFluidTerminal(this.player, _selectedFluid, this.terminal)
				.sendPacketToServer();
	}

	@Override
	public ItemStack slotClick(int slotNumber, int p_75144_2_, int p_75144_3_, EntityPlayer player) {
		ItemStack returnStack = null;
		boolean hasPermission = true;
		if (slotNumber == 0 || slotNumber == 1) {
			ItemStack stack = player.inventory.getItemStack();
			if (stack == null) {} else {
				if (GasUtil.isEmpty(stack) && PermissionUtil.hasPermission(player, SecurityPermissions.INJECT, (IPart) getTerminal())) {}
				else if (GasUtil.isFilled(stack) && PermissionUtil.hasPermission(player, SecurityPermissions.EXTRACT, (IPart) getTerminal())) {}
				else {
					ItemStack slotStack = ((Slot) this.inventorySlots.get(slotNumber)).getStack();
					if (slotStack == null)
						returnStack = null;
					else
						returnStack = slotStack.copy();
					hasPermission = false;
				}
			}
		}
		if (hasPermission)
			returnStack = super.slotClick(slotNumber, p_75144_2_, p_75144_3_, player);
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP p = (EntityPlayerMP) player;
			p.sendContainerToPlayer(this);
		}
		return returnStack;

	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (this.terminal.getInventory().isItemValidForSlot(0, itemstack1)) {
				if (slotnumber == 1 || slotnumber == 0) {
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
		if (this.guiGasTerminal != null)
			this.guiGasTerminal.updateFluids();
	}
}

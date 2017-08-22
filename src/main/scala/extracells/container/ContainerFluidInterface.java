package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.util.AEPartLocation;
import extracells.api.IFluidInterface;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.part.PacketFluidInterface;
import extracells.part.fluid.PartFluidInterface;
import extracells.tileentity.TileEntityFluidInterface;

public class ContainerFluidInterface extends Container implements
		IContainerListener {
	public IFluidInterface fluidInterface;
	public GuiFluidInterface gui;
	EntityPlayer player;

	public ContainerFluidInterface(EntityPlayer player,
			IFluidInterface fluidInterface) {
		this.player = player;
		this.fluidInterface = fluidInterface;
		for (int j = 0; j < 9; j++) {
			addSlotToContainer(new SlotRespective(
					fluidInterface.getPatternInventory(), j, 8 + j * 18, 115));
		}
		bindPlayerInventory(player.inventory);
		if (fluidInterface instanceof TileEntityFluidInterface) {
			((TileEntityFluidInterface) fluidInterface).registerListener(this);
		} else if (fluidInterface instanceof PartFluidInterface) {
			((PartFluidInterface) fluidInterface).registerListener(this);
		}
		if (fluidInterface instanceof TileEntityFluidInterface) {
			((TileEntityFluidInterface) fluidInterface).doNextUpdate = true;
		} else if (fluidInterface instanceof PartFluidInterface) {
			((PartFluidInterface) fluidInterface).doNextUpdate = true;
		}
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, i * 18 + 149));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 207));// 173
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	private String getFluidName(AEPartLocation side) {
		Fluid fluid = this.fluidInterface.getFilter(side);
		if (fluid == null) {
			return "";
		}
		return fluid.getName();
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (this.fluidInterface instanceof TileEntityFluidInterface) {
			((TileEntityFluidInterface) this.fluidInterface)
					.removeListener(this);
		} else if (this.fluidInterface instanceof PartFluidInterface) {
			((PartFluidInterface) this.fluidInterface).removeListener(this);
		}
	}

	@Override
	protected void retrySlotClick(int p_75133_1_, int p_75133_2_,
			boolean p_75133_3_, EntityPlayer p_75133_4_) {

	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack itemstack = null;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (itemstack.getItem() instanceof ICraftingPatternItem) {
				if (slotnumber < 9) {
					if (!mergeItemStack(itemstack1,
							this.inventorySlots.size() - 9,
							this.inventorySlots.size(), false)) {
						if (!mergeItemStack(itemstack1, 9,
								this.inventorySlots.size() - 9, false))
							return null;
					}
				} else if (!mergeItemStack(itemstack1, 0, 9, false)) {
					return null;
				}
				if (itemstack1.stackSize == 0) {
					slot.putStack(null);
				} else {
					slot.onSlotChanged();
				}
				return itemstack;
			}

			if (slotnumber < this.inventorySlots.size() - 9) {
				if (!mergeItemStack(itemstack1, this.inventorySlots.size() - 9,
						this.inventorySlots.size(), true)) {
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 9,
					this.inventorySlots.size() - 9, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	public void updateContainer() {
		new PacketFluidInterface(new FluidStack[] {
				this.fluidInterface.getFluidTank(
						AEPartLocation.fromOrdinal(0)).getFluid(),
				this.fluidInterface.getFluidTank(
					AEPartLocation.fromOrdinal(1)).getFluid(),
				this.fluidInterface.getFluidTank(
					AEPartLocation.fromOrdinal(2)).getFluid(),
				this.fluidInterface.getFluidTank(
					AEPartLocation.fromOrdinal(3)).getFluid(),
				this.fluidInterface.getFluidTank(
					AEPartLocation.fromOrdinal(4)).getFluid(),
				this.fluidInterface.getFluidTank(
					AEPartLocation.fromOrdinal(5)).getFluid() },
				new String[] { getFluidName(AEPartLocation.fromOrdinal(0)),
						getFluidName(AEPartLocation.fromOrdinal(1)),
						getFluidName(AEPartLocation.fromOrdinal(2)),
						getFluidName(AEPartLocation.fromOrdinal(3)),
						getFluidName(AEPartLocation.fromOrdinal(4)),
						getFluidName(AEPartLocation.fromOrdinal(5)) },
				this.player).sendPacketToPlayer(this.player);

	}

}

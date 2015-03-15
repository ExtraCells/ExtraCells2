package extracells.container;

import appeng.api.implementations.ICraftingPatternItem;
import extracells.api.IFluidInterface;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.part.PacketFluidInterface;
import extracells.part.PartFluidInterface;
import extracells.registries.ItemEnum;
import extracells.tileentity.TileEntityFluidInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

public class ContainerFluidInterface extends Container implements IContainerListener
{
	public IFluidInterface fluidInterface;
	public GuiFluidInterface gui;
	EntityPlayer player;

	public ContainerFluidInterface(EntityPlayer player, IFluidInterface fluidInterface)
	{
		this.player = player;
		this.fluidInterface = fluidInterface;
		for (int j = 0; j < 9; j++){
			addSlotToContainer(new SlotRespective(fluidInterface.getPatternInventory(), j, 8 + j * 18, 115));
		}
		bindPlayerInventory(player.inventory);
		if(fluidInterface instanceof TileEntityFluidInterface){
			((TileEntityFluidInterface) fluidInterface).registerListener(this);
		}else if(fluidInterface instanceof PartFluidInterface){
			((PartFluidInterface) fluidInterface).registerListener(this);
		}
		if(fluidInterface instanceof TileEntityFluidInterface){
			((TileEntityFluidInterface) fluidInterface).doNextUpdate = true;
		}else if(fluidInterface instanceof PartFluidInterface){
			((PartFluidInterface) fluidInterface).doNextUpdate = true;
		}
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 149));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 207));//173
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}
	
	public void onContainerClosed(EntityPlayer player)
    {
		super.onContainerClosed(player);
		if(fluidInterface instanceof TileEntityFluidInterface){
			((TileEntityFluidInterface) fluidInterface).removeListener(this);
		}else if(fluidInterface instanceof PartFluidInterface){
			((PartFluidInterface) fluidInterface).removeListener(this);
		}
    }

	@Override
	public void updateContainer() {
		new PacketFluidInterface(new FluidStack[]{
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(0)).getFluid(),
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(1)).getFluid(),
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(2)).getFluid(),
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(3)).getFluid(),
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(4)).getFluid(),
				fluidInterface.getFluidTank(ForgeDirection.getOrientation(5)).getFluid()
		}, new Integer[]{
				getFluidID(ForgeDirection.getOrientation(0)),
				getFluidID(ForgeDirection.getOrientation(1)),
				getFluidID(ForgeDirection.getOrientation(2)),
				getFluidID(ForgeDirection.getOrientation(3)),
				getFluidID(ForgeDirection.getOrientation(4)),
				getFluidID(ForgeDirection.getOrientation(5))
		}, player).sendPacketToPlayer(player);
		
	}
	
	private int getFluidID(ForgeDirection side){
		Fluid fluid = fluidInterface.getFilter(side);
		if(fluid == null)
			return -1;
		return fluid.getID();
	}
	
	@Override
	protected void retrySlotClick(int p_75133_1_, int p_75133_2_, boolean p_75133_3_, EntityPlayer p_75133_4_)
    {
		
    }
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(slotnumber);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            if(itemstack.getItem() instanceof ICraftingPatternItem){
            	if(slotnumber < 9){
            		if (!mergeItemStack(itemstack1, inventorySlots.size() - 9, inventorySlots.size(), false)) {
            			if(!mergeItemStack(itemstack1, 9, inventorySlots.size() - 9, false))
            				return null;
                    }
            	}else if (!mergeItemStack(itemstack1, 0, 9, false)) {
            		return null;
            	}
            	if (itemstack1.stackSize == 0) {
                    slot.putStack(null);
                } else {
                    slot.onSlotChanged();
                }
            	return itemstack;
            }
            
            if (slotnumber < inventorySlots.size() - 9) {
                if (!mergeItemStack(itemstack1, inventorySlots.size() - 9, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!mergeItemStack(itemstack1, 9, inventorySlots.size() - 9, false)) {
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

}

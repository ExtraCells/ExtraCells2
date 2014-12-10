package extracells.container;

import extracells.api.IFluidInterface;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.part.PacketFluidInterface;
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
		bindPlayerInventory(player.inventory);
		if(fluidInterface instanceof TileEntityFluidInterface){
			((TileEntityFluidInterface) fluidInterface).registerListener(this);
		}
		if(fluidInterface instanceof TileEntityFluidInterface){
			((TileEntityFluidInterface) fluidInterface).doNextUpdate = true;
		}
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 129));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 187));//173
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

}

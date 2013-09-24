package extracells.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import extracells.Extracells;
import extracells.tile.TileEntityCertusTank;

public class BlockCertusTank extends BlockContainer
{

	public BlockCertusTank(int id)
	{
		super(id, Material.glass);
		setCreativeTab(Extracells.ModTab);
		this.setUnlocalizedName("block.certustank");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int blockID, float offsetX, float offsetY, float offsetZ)
	{
		ItemStack currentItem = player.inventory.mainInventory[player.inventory.currentItem];
		if (currentItem != null)
		{
			TileEntityCertusTank tileEntity = (TileEntityCertusTank) worldObj.getBlockTileEntity(x, y, z);

			if (FluidContainerRegistry.isFilledContainer(currentItem))
			{
				if (tileEntity.fill(ForgeDirection.UNKNOWN, FluidContainerRegistry.getFluidForFilledItem(currentItem), false) == FluidContainerRegistry.getFluidForFilledItem(currentItem).amount)
				{
					if (player.inventory.getCurrentItem().stackSize == 1)
					{
						tileEntity.fill(ForgeDirection.UNKNOWN, FluidContainerRegistry.getFluidForFilledItem(currentItem), true);
						currentItem = currentItem.getItem().getContainerItemStack(currentItem);
					} else
					{
						if (player.inventory.addItemStackToInventory(currentItem.getItem().getContainerItemStack(currentItem)))
						{
							tileEntity.fill(ForgeDirection.UNKNOWN, FluidContainerRegistry.getFluidForFilledItem(currentItem), true);
							currentItem.stackSize -= 1;
						}
					}
				}
			} else if (FluidContainerRegistry.isEmptyContainer(currentItem))
			{
				if (tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid != null
						&& tileEntity.drain(ForgeDirection.UNKNOWN, new FluidStack(tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid(), 1000), false) == new FluidStack(tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid(), 1000))
				{
					FluidStack drained = tileEntity.drain(ForgeDirection.UNKNOWN, new FluidStack(tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid(), 1000), false);
					if (player.inventory.getCurrentItem().stackSize == 1)
					{
						tileEntity.drain(ForgeDirection.UNKNOWN, new FluidStack(tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid(), 1000), true);
						currentItem = FluidContainerRegistry.fillFluidContainer(drained, currentItem);
					} else
					{
						if (player.inventory.addItemStackToInventory(FluidContainerRegistry.fillFluidContainer(drained, currentItem)))
						{
							tileEntity.drain(ForgeDirection.UNKNOWN, new FluidStack(tileEntity.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid.getFluid(), 1000), true);
							currentItem.stackSize -= 1;
						}
					}
				}
			} else if (currentItem.getItem() instanceof IFluidContainerItem)
			{
				IFluidContainerItem fluidContainer = (IFluidContainerItem) currentItem.getItem();
				ItemStack currentContainer = currentItem.copy();
				currentContainer.stackSize = 1;

				if (fluidContainer.getFluid(currentItem) != null)
				{
					if (tileEntity.fill(ForgeDirection.UNKNOWN, fluidContainer.drain(currentItem, fluidContainer.getFluid(currentContainer).amount, false), false) == fluidContainer.getFluid(currentContainer).amount)
					{
						if (player.inventory.getCurrentItem().stackSize == 1)
						{
							tileEntity.fill(ForgeDirection.UNKNOWN, fluidContainer.drain(currentItem, fluidContainer.getFluid(currentContainer).amount, true), true);
							currentItem = currentContainer;
						} else
						{
							ItemStack currentFakeContainer = currentContainer.copy();
							fluidContainer.drain(currentItem, fluidContainer.getFluid(currentFakeContainer).amount, true);
							if (player.inventory.addItemStackToInventory(currentItem.getItem().getContainerItemStack(currentFakeContainer)))
							{
								tileEntity.fill(ForgeDirection.UNKNOWN, fluidContainer.drain(currentItem, fluidContainer.getFluid(currentContainer).amount, true), true);
								currentItem.stackSize -= 1;
							}
						}
					}
				} else
				{
					if (tileEntity.fill(ForgeDirection.UNKNOWN, fluidContainer.drain(currentItem, fluidContainer.getCapacity(currentContainer), false), false) == fluidContainer.getCapacity(currentContainer))
					{
						if (player.inventory.getCurrentItem().stackSize == 1)
						{
							fluidContainer.fill(currentContainer, tileEntity.drain(ForgeDirection.UNKNOWN, fluidContainer.getCapacity(currentContainer), true), true);
							currentItem = currentContainer;
						} else
						{
							fluidContainer.fill(currentContainer, tileEntity.drain(ForgeDirection.UNKNOWN, fluidContainer.getCapacity(currentContainer), false), true);
							if (player.inventory.addItemStackToInventory(currentItem.getItem().getContainerItemStack(currentContainer)))
							{
								tileEntity.drain(ForgeDirection.UNKNOWN, fluidContainer.getCapacity(currentContainer), true);
								currentItem.stackSize -= 1;
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCertusTank();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}

package extracells.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import extracells.registries.BlockEnum;
import extracells.Extracells;
import extracells.network.ChannelHandler;
import extracells.render.RenderHandler;
import extracells.tileentity.TileEntityCertusTank;

public class BlockCertusTank extends BlockContainer
{
	IIcon breakIcon;
	IIcon topIcon;
	IIcon bottomIcon;
	IIcon sideIcon;
	IIcon sideMiddleIcon;
	IIcon sideTopIcon;
	IIcon sideBottomIcon;

	public BlockCertusTank()
	{
		super(Material.glass);
		setCreativeTab(Extracells.ModTab);
		setBlockName("extracells.block.certustank");
		setHardness(2.0F);
		setResistance(10.0F);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return getDropWithNBT(world, x, y, z);
	}

	@Override
	public IIcon getIcon(int side, int b)
	{
		if (b == 1)
			return sideTopIcon;
		if (b == 2)
			return sideBottomIcon;
		if (b == 3)
			return sideMiddleIcon;
		return side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconregister)
	{
		breakIcon = iconregister.registerIcon("extracells:certustank");
		topIcon = iconregister.registerIcon("extracells:CTankTop");
		bottomIcon = iconregister.registerIcon("extracells:CTankBottom");
		sideIcon = iconregister.registerIcon("extracells:CTankSide");
		sideMiddleIcon = iconregister.registerIcon("extracells:CTankSideMiddle");
		sideTopIcon = iconregister.registerIcon("extracells:CTankSideTop");
		sideBottomIcon = iconregister.registerIcon("extracells:CTankSideBottom");
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		RenderHandler.renderPass = pass;
		return true;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer entityplayer, int blockID, float offsetX, float offsetY, float offsetZ)
	{
		ItemStack current = entityplayer.inventory.getCurrentItem();

		if (entityplayer.isSneaking() && current == null)
		{
			dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
			worldObj.setBlockToAir(x, y, z);
			return true;
		}
		if (current != null)
		{
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
			TileEntityCertusTank tank = (TileEntityCertusTank) worldObj.getTileEntity(x, y, z);

			if (liquid != null)
			{
				int amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

				if (amountFilled != 0 && !entityplayer.capabilities.isCreativeMode)
				{
					if (current.stackSize > 1)
					{
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
						entityplayer.inventory.addItemStackToInventory(current.getItem().getContainerItem(current));
					} else
					{
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = current.getItem().getContainerItem(current);
					}
				}

				return true;

				// Handle empty containers
			} else
			{

				FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
				if (available != null)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

					liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

					if (liquid != null)
					{
						if (!entityplayer.capabilities.isCreativeMode)
						{
							if (current.stackSize > 1)
							{
								if (!entityplayer.inventory.addItemStackToInventory(filled))
								{
									tank.fill(ForgeDirection.UNKNOWN, new FluidStack(liquid, liquid.amount), true);
									return false;
								} else
								{
									entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
								}
							} else
							{
								entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = filled;
							}
						}
						tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
						return true;
					}
				}
			}
		}
		return false;
	}

	public ItemStack getDropWithNBT(World world, int x, int y, int z)
	{
		NBTTagCompound tileEntity = new NBTTagCompound();
		TileEntity worldTE = world.getTileEntity(x, y, z);
		if (worldTE != null && worldTE instanceof TileEntityCertusTank)
		{
			ItemStack dropStack = new ItemStack(BlockEnum.CERTUSTANK.getBlock(), 1);

			((TileEntityCertusTank) worldTE).writeToNBTWithoutCoords(tileEntity);

			dropStack.setTagCompound(new NBTTagCompound());
			dropStack.stackTagCompound.setTag("tileEntity", tileEntity);
			return dropStack;

		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
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
		return Extracells.renderID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock)
	{
		if (!world.isRemote)
		{

			ChannelHandler.sendPacketToAllPlayers(world.getTileEntity(x, y, z).getDescriptionPacket(), world);
		}
	}

	public String getUnlocalizedName()
	{
		return super.getUnlocalizedName().replace("tile.extracells.", "extracells.block.");
	}
}

package extracells.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.tile.TileEntityCertusTank;

public class ItemBlockCertusTank extends ItemBlock
{

	public ItemBlockCertusTank(int id)
	{
		super(id);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		if (itemstack != null)
		{
			if (itemstack.hasTagCompound())
			{
				try
				{
					FluidStack fluidInTank = FluidStack.loadFluidStackFromNBT(itemstack.getTagCompound().getCompoundTag("tileEntity"));

					if (fluidInTank != null && fluidInTank.getFluid() != null)
					{
						return StatCollector.translateToLocal(this.getUnlocalizedName(itemstack) + ".name") + " - " + fluidInTank.getFluid().getLocalizedName();
					}
				} catch (Throwable e)
				{
				}
			}
			return StatCollector.translateToLocal(this.getUnlocalizedName(itemstack) + ".name");
		}
		return "";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (stack != null && stack.hasTagCompound())
		{
			if (FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")) != null)
				list.add(FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")).amount + "mB");
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if (!world.setBlock(x, y, z, getBlockID(), metadata, 3))
		{
			return false;
		}

		if (world.getBlockId(x, y, z) == getBlockID())
		{
			Block.blocksList[getBlockID()].onBlockPlacedBy(world, x, y, z, player, stack);
			Block.blocksList[getBlockID()].onPostBlockPlaced(world, x, y, z, metadata);
		}

		if (stack != null && stack.hasTagCompound())
		{
			((TileEntityCertusTank) world.getBlockTileEntity(x, y, z)).readFromNBTWithoutCoords(stack.getTagCompound().getCompoundTag("tileEntity"));
		}
		return true;
	}
}

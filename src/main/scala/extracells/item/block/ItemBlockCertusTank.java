package extracells.item.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.tileentity.TileEntityCertusTank;

public class ItemBlockCertusTank extends ItemBlock {

	public static final int CAPACITY = 32000;

	public ItemBlockCertusTank(Block block) {
		super(block);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerItemStack(stack, CAPACITY);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List list, ITooltipFlag advanced) {
		FluidStack fluidStack = FluidUtil.getFluidContained(stack);
		if (fluidStack != null) {
			list.add(fluidStack.amount + "mB");
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack) {
		if (itemStack != null) {
			FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);

			if (fluidStack != null && fluidStack.getFluid() != null) {
				return I18n.translateToLocal(getTranslationKey(itemStack)) + " - " + fluidStack.getFluid().getLocalizedName(fluidStack);
			}
			return I18n.translateToLocal(getTranslationKey(itemStack));
		}
		return "";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
			return false;
		}

		if (stack != null && stack.hasTagCompound()) {
			TileEntityCertusTank certusTank = (TileEntityCertusTank) world.getTileEntity(pos);
			FluidStack fluidStack = FluidUtil.getFluidContained(stack);
			certusTank.setFluid(fluidStack);
		}
		return true;
	}
}
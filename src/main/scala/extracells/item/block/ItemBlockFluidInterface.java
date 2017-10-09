package extracells.item.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.TileUtil;

public class ItemBlockFluidInterface extends ItemBlock {

	public ItemBlockFluidInterface(Block block) {
		super(block);
		setMaxDamage(0);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.extracells.block.fluidinterface";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
			return false;
		}

		NBTTagCompound tagCompound = stack.getTagCompound();
		if (tagCompound != null) {
			TileEntityFluidInterface fluidInterface = TileUtil.getTile(world, pos, TileEntityFluidInterface.class);
			if (fluidInterface != null) {
				fluidInterface.readFilter(tagCompound);
			}
		}
		return true;
	}
}

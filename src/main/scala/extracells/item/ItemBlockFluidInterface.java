package extracells.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import extracells.tileentity.TileEntityFluidInterface;

public class ItemBlockFluidInterface extends ItemBlock {

	public ItemBlockFluidInterface(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("extracells.block.fluidinterface");
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if(!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)){
			return false;
		}

		if (stack.hasTagCompound()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileEntityFluidInterface) {
				((TileEntityFluidInterface) tile).readFilter(stack.getTagCompound());
			}
		}
		return true;
	}
}

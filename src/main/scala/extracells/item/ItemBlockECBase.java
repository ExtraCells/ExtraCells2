package extracells.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.tileentity.TileEntityFluidInterface;

public class ItemBlockECBase extends ItemBlock {

	public ItemBlockECBase(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal(getUnlocalizedName(stack) + ".name");
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item));
		list.add(new ItemStack(item, 1, 1));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack == null)
			return "null";
		switch (stack.getItemDamage()) {
		case 0:
			return "extracells.block.fluidinterface";
		case 1:
			return "extracells.block.fluidfiller";
		default:
			return super.getUnlocalizedName(stack);
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		if(!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)){
			return false;
		}

		if (getMetadata(stack.getItemDamage()) == 0 && stack.hasTagCompound()) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileEntityFluidInterface) {
				((TileEntityFluidInterface) tile).readFilter(stack.getTagCompound());
			}
		}
		return true;
	}
}

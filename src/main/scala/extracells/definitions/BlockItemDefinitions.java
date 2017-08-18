package extracells.definitions;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.ITileDefinition;

public class BlockItemDefinitions implements ITileDefinition {

	private final Block block;
	private final int meta;
	private final Class<? extends TileEntity> blockTileEntity;

	public BlockItemDefinitions(Block _block) {
		this(_block, 0);
	}

	public BlockItemDefinitions(Block _block,
			Class<? extends TileEntity> _blockTileEntity) {
		this(_block, 0, _blockTileEntity);
	}

	public BlockItemDefinitions(Block _block, int _meta) {
		this(_block, _meta, null);
	}

	public BlockItemDefinitions(Block _block, int _meta,
			Class<? extends TileEntity> _blockTileEntity) {
		this.block = _block;
		this.meta = _meta;
		this.blockTileEntity = _blockTileEntity;
	}

	@Override
	public java.util.Optional<Block> maybeBlock() {
		return java.util.Optional.ofNullable(block);
	}

	@Override
	public java.util.Optional<ItemBlock> maybeItemBlock() {
		return java.util.Optional.empty();
	}

	@Override
	public boolean isSameAs(ItemStack comparableStack) {
		return comparableStack != null && ItemStack.areItemStacksEqual(maybeStack(1).get(), comparableStack);
	}

	@Override
	public boolean isSameAs(IBlockAccess world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return !maybeBlock().isPresent() && block == this.block;
	}

	@Override
	public java.util.Optional<Item> maybeItem() {
		return java.util.Optional.ofNullable(Item.getItemFromBlock(block));
	}

	@Override
	public java.util.Optional<ItemStack> maybeStack(int stackSize) {
		return java.util.Optional.of(new ItemStack(this.block, stackSize, this.meta));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public java.util.Optional<? extends Class<? extends TileEntity>> maybeEntity() {
		return java.util.Optional.ofNullable(this.blockTileEntity);
	}
}

package extracells.definitions;

import appeng.api.definitions.ITileDefinition;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

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
	public Optional<Block> maybeBlock() {
		return Optional.fromNullable(block);
	}

	@Override
	public Optional<ItemBlock> maybeItemBlock() {
		return Optional.absent();
	}

	@Override
	public boolean isSameAs(ItemStack comparableStack) {
		return comparableStack != null && ItemStack.areItemStacksEqual(maybeStack(1).orNull(), comparableStack);
	}

	@Override
	public boolean isSameAs(IBlockAccess world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		return !maybeBlock().isPresent() && block == this.block;
	}

	@Override
	public Optional<Item> maybeItem() {
		return Optional.fromNullable(Item.getItemFromBlock(block));
	}

	@Override
	public Optional<ItemStack> maybeStack(int stackSize) {
		return Optional.of(new ItemStack(this.block, stackSize, this.meta));
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Optional<? extends Class<? extends TileEntity>> maybeEntity() {
		return Optional.fromNullable(this.blockTileEntity);
	}
}

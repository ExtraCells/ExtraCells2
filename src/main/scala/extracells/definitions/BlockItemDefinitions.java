package extracells.definitions;

import javax.annotation.Nonnull;
import java.util.Optional;

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

	public BlockItemDefinitions(Block block) {
		this(block, 0);
	}

	public BlockItemDefinitions(Block block,
		Class<? extends TileEntity> blockTileEntity) {
		this(block, 0, blockTileEntity);
	}

	public BlockItemDefinitions(Block block, int meta) {
		this(block, meta, null);
	}

	public BlockItemDefinitions(Block block, int meta,
		Class<? extends TileEntity> blockTileEntity) {
		this.block = block;
		this.meta = meta;
		this.blockTileEntity = blockTileEntity;
	}

	@Override
	public Optional<Block> maybeBlock() {
		return Optional.ofNullable(block);
	}

	@Override
	public Optional<ItemBlock> maybeItemBlock() {
		return Optional.empty();
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
	public Optional<Item> maybeItem() {
		return Optional.ofNullable(Item.getItemFromBlock(block));
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
		return Optional.ofNullable(this.blockTileEntity);
	}

	@Nonnull
	@Override
	public String identifier() {
		return block.getRegistryName().getPath();
	}
}

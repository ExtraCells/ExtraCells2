package extracells.definitions;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.AEItemDefinition;

public class BlockItemDefinitions implements AEItemDefinition {
	
	private final Block block;
	private final int meta;
	private final Class<? extends TileEntity> blockTileEntity;
	
	public BlockItemDefinitions(Block _block){
		this(_block, 0);
	}
	
	public BlockItemDefinitions(Block _block, int _meta){
		this(_block, _meta, null);
	}
	
	public BlockItemDefinitions(Block _block, Class<? extends TileEntity> _blockTileEntity){
		this(_block, 0, _blockTileEntity);
	}
	
	public BlockItemDefinitions(Block _block, int _meta, Class<? extends TileEntity> _blockTileEntity){
		block = _block;
		meta = _meta;
		blockTileEntity = _blockTileEntity;
	}

	@Override
	public Block block() {
		return block;
	}

	@Override
	public Item item() {
		return Item.getItemFromBlock(block);
	}

	@Override
	public Class<? extends TileEntity> entity() {
		return blockTileEntity;
	}

	@Override
	public ItemStack stack(int stackSize) {
		return new ItemStack(block, stackSize, meta);
	}

	@Override
	public boolean sameAsStack(ItemStack comparableItem) {
		if(comparableItem == null)
			return false;
		return ItemStack.areItemStacksEqual(stack(1), comparableItem);
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z) {
		Block _block = world.getBlock(x, y, z);
		if(_block == null || block() == null)
			return false;
		return _block == block();
	}

}

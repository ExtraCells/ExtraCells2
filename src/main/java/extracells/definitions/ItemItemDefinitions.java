package extracells.definitions;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.AEItemDefinition;

public class ItemItemDefinitions implements AEItemDefinition {
	
	public final Item item;
	public final int meta;
	
	public ItemItemDefinitions(Item _item){
		this(_item, 0);
	}
	
	public ItemItemDefinitions(Item _item, int _meta){
		item = _item;
		meta = _meta;
	}

	@Override
	public Block block() {
		return null;
	}

	@Override
	public Item item() {
		return item;
	}

	@Override
	public Class<? extends TileEntity> entity() {
		return null;
	}

	@Override
	public ItemStack stack(int stackSize) {
		return new ItemStack(item, stackSize, meta);
	}

	@Override
	public boolean sameAsStack(ItemStack comparableItem) {
		if(comparableItem == null)
			return false;
		return ItemStack.areItemStacksEqual(stack(1), comparableItem);
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z) {
		return false;
	}

}

package extracells.definitions;

import appeng.api.definitions.IItemDefinition;
import com.google.common.base.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

public class ItemItemDefinitions implements IItemDefinition {

	public final Item item;
	public final int meta;

	public ItemItemDefinitions(Item _item) {
		this(_item, 0);
	}

	public ItemItemDefinitions(Item _item, int _meta) {
		this.item = _item;
		this.meta = _meta;
	}

	@Override
	public Optional<Item> maybeItem() {
		return Optional.fromNullable(this.item);
	}

    @Override
    public Optional<ItemStack> maybeStack(int stackSize) {
        return Optional.of(new ItemStack(this.item, stackSize, this.meta));
    }

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
    public boolean isSameAs(ItemStack comparableStack) {
        return comparableStack != null && comparableStack.isItemEqual(maybeStack(1).get());
    }

    @Override
    public boolean isSameAs(IBlockAccess world, int x, int y, int z) {
        return false;
    }
}

package extracells.definitions;

import javax.annotation.Nonnull;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;

public class ItemItemDefinitions implements IItemDefinition {

	public final Item item;
	public final int meta;

	public ItemItemDefinitions(Item item) {
		this(item, 0);
	}

	public ItemItemDefinitions(Item item, int meta) {
		this.item = item;
		this.meta = meta;

	}

	@Override
	public Optional<Item> maybeItem() {
		return Optional.ofNullable(this.item);
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

	@Nonnull
	@Override
	public String identifier() {
		return item.getRegistryName().getPath();
	}
}

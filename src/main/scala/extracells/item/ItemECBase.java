package extracells.item;

import net.minecraft.item.Item;

import extracells.models.IItemModelRegister;
import extracells.models.ModelManager;
import extracells.util.CreativeTabEC;

public class ItemECBase extends Item implements IItemModelRegister {
	public ItemECBase() {
		setCreativeTab(CreativeTabEC.INSTANCE);
	}

	@Override
	public void registerModel(Item item, ModelManager manager) {
		manager.registerItemModel(item);
	}
}

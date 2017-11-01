package extracells.item.storage;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IStorageComponent;
import extracells.integration.Integration;
import extracells.item.ItemECBase;
import extracells.models.ModelManager;

public class ItemStorageComponent extends ItemECBase implements IStorageComponent {

	public ItemStorageComponent() {
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getBytes(ItemStack itemStack) {
		StorageType type = CellDefinition.components.fromMeta(itemStack.getItemDamage());
		return type.getBytes();
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		StorageType type = CellDefinition.components.fromMeta(itemStack.getItemDamage());
		CellDefinition definition = type.getDefinition();
		return definition.getRarity();
	}

	@Override
	public void getSubItems(CreativeTabs creativeTab, NonNullList itemList) {
		if (!this.isInCreativeTab(creativeTab))
			return;
		for (StorageType type : CellDefinition.components) {
			if (type.getDefinition() == CellDefinition.GAS && !Integration.Mods.MEKANISMGAS.isEnabled()) {
				continue;
			}
			itemList.add(new ItemStack(this, 1, type.getMeta()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		StorageType type = CellDefinition.components.fromMeta(itemStack.getItemDamage());
		return "extracells.item.storage.component." + type.getIdentifier();
	}

	@Override
	public boolean isStorageComponent(ItemStack itemStack) {
		return itemStack.getItem() == this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (StorageType type : CellDefinition.components) {
			if (type.getDefinition() == CellDefinition.GAS && !Integration.Mods.MEKANISMGAS.isEnabled()) {
				continue;
			}
			manager.registerItemModel(item, type.getMeta(), type.getModelName());
		}
	}
}

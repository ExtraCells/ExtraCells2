package extracells.registries;

import extracells.Extracells;
import extracells.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public enum ItemEnum {
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL( "terminal.fluid.wireless", new ItemWirelessTerminalFluid()),
	STORAGECOMPONET( "storage.component", new ItemStorageComponent()),
	STORAGECASING( "storage.casing", new ItemStorageCasing()),
	FLUIDITEM("fluid.item", new ItemFluid()), // Internal EC Item
	FLUIDSTORAGEPORTABLE("storage.fluid.portable", new ItemStoragePortableCell()),
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern());// Internal EC Item

	private final String internalName;
	private Item item;

	ItemEnum(String _internalName, Item _item) {
		this.internalName = _internalName;
		this.item = _item;
		this.item.setUnlocalizedName("extracells." + this.internalName);
		if (!this.internalName.equals("fluid.item"))
			this.item.setCreativeTab(Extracells.ModTab());
	}

	public ItemStack getDamagedStack(int damage) {
		return new ItemStack(this.item, 1, damage);
	}

	public String getInternalName() {
		return this.internalName;
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getSizedStack(int size) {
		return new ItemStack(this.item, size);
	}

	public String getStatName() {
		return StatCollector.translateToLocal(this.item.getUnlocalizedName());
	}
}

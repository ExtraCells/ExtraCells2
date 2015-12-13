package extracells.registries;

import extracells.Extracells;
import extracells.integration.Integration;
import extracells.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public enum ItemEnum {
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	GASSTORAGE("storage.gas", new ItemStorageGas(), Integration.Mods.MEKANISMGAS),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL("terminal.fluid.wireless", ItemWirelessTerminalFluid.THIS()),
	STORAGECOMPONET("storage.component", new ItemStorageComponent()),
	STORAGECASING("storage.casing", new ItemStorageCasing()),
	FLUIDITEM("fluid.item", new ItemFluid()), // Internal EC Item
	FLUIDSTORAGEPORTABLE("storage.fluid.portable", ItemStoragePortableCell.THIS()),
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern()),// Internal EC Item
	UNIVERSALTERMINAL("terminal.universal.wireless", ItemWirelessTerminalUniversal.THIS()),
	GASWIRELESSTERMINAL("terminal.gas.wireless", ItemWirelessTerminalGas.THIS(), Integration.Mods.MEKANISMGAS);

	private final String internalName;
	private Item item;
	private Integration.Mods mod;

	ItemEnum(String _internalName, Item _item) {
		this(_internalName, _item, null);
	}

	ItemEnum(String _internalName, Item _item, Integration.Mods _mod) {
		this.internalName = _internalName;
		this.item = _item;
		this.item.setUnlocalizedName("extracells." + this.internalName);
		this.mod = _mod;
		if ((!(this.internalName.equals("fluid.item") || this.internalName.equals("pattern.crafting"))) && (_mod == null || _mod.isEnabled()))
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

	public Integration.Mods getMod(){
		return mod;
	}
}

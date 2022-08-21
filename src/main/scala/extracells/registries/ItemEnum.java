package extracells.registries;

import extracells.Extracells;
import extracells.integration.Integration;
import extracells.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.lang.Long;

public enum ItemEnum {
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	PHYSICALSTORAGESINGULARITY("storage.physical.advanced.singularity", new ItemAdvancedStorageCell(Long.MAX_VALUE / 16, 1, 4096, 15000.0, "singularity")),
	PHYSICALSTORAGEQUANTUM("storage.physical.advanced.quantum", new ItemAdvancedStorageCell(Integer.MAX_VALUE / 16, 1, 1, 1000.0, "quantum")),
	PHYSICALSTORAGEVOID("storage.physical.void", new ItemVoidStorageCell()),
	GASSTORAGE("storage.gas", new ItemStorageGas(), Integration.Mods.MEKANISMGAS),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL("terminal.fluid.wireless", ItemWirelessTerminalFluid.THIS()),
	STORAGECOMPONENT("storage.component", new ItemStorageComponent()),
	STORAGECASING("storage.casing", new ItemStorageCasing()),
	FLUIDITEM("fluid.item", new ItemFluid(), null, null), // Internal EC Item
	FLUIDSTORAGEPORTABLE("storage.fluid.portable", ItemStoragePortableFluidCell.THIS()),
	GASSTORAGEPORTABLE("storage.gas.portable", ItemStoragePortableGasCell.THIS(), Integration.Mods.MEKANISMGAS),
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern(), null, null),// Internal EC Item
	UNIVERSALTERMINAL("terminal.universal.wireless", ItemWirelessTerminalUniversal.THIS()),
	GASWIRELESSTERMINAL("terminal.gas.wireless", ItemWirelessTerminalGas.THIS(), Integration.Mods.MEKANISMGAS),
	OCUPGRADE("oc.upgrade", ItemOCUpgrade.THIS(), Integration.Mods.OPENCOMPUTERS);

	private final String internalName;
	private Item item;
	private Integration.Mods mod;

	ItemEnum(String _internalName, Item _item) {
		this(_internalName, _item, null);
	}

	ItemEnum(String _internalName, Item _item, Integration.Mods _mod){
		this(_internalName, _item, _mod, Extracells.ModTab());
	}

	ItemEnum(String _internalName, Item _item, Integration.Mods _mod, CreativeTabs creativeTab) {
		this.internalName = _internalName;
		this.item = _item;
		this.item.setUnlocalizedName("extracells." + this.internalName);
		this.mod = _mod;
		if ((creativeTab != null) && (_mod == null || _mod.isEnabled()))
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

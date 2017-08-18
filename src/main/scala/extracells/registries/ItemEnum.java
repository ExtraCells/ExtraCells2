package extracells.registries;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import extracells.integration.Integration;
import extracells.item.ItemFluid;
import extracells.item.ItemFluidPattern;
import extracells.item.ItemInternalCraftingPattern;
import extracells.item.ItemOCUpgrade;
import extracells.item.ItemPartECBase;
import extracells.item.ItemStorageCasing;
import extracells.item.ItemStorageComponent;
import extracells.item.ItemStorageFluid;
import extracells.item.ItemStorageGas;
import extracells.item.ItemStoragePhysical;
import extracells.item.ItemStoragePortableFluidCell;
import extracells.item.ItemStoragePortableGasCell;
import extracells.item.ItemWirelessTerminalFluid;
import extracells.item.ItemWirelessTerminalGas;
import extracells.item.ItemWirelessTerminalUniversal;
import extracells.util.CreativeTabEC;

public enum ItemEnum {
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	GASSTORAGE("storage.gas", new ItemStorageGas(), Integration.Mods.MEKANISMGAS),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL("terminal.fluid.wireless", ItemWirelessTerminalFluid.THIS()),
	STORAGECOMPONET("storage.component", new ItemStorageComponent()),
	STORAGECASING("storage.casing", new ItemStorageCasing()),
	FLUIDITEM("fluid.item", new ItemFluid(), null, null), // Internal EC Item
	FLUIDSTORAGEPORTABLE("storage.fluid.portable", ItemStoragePortableFluidCell.THIS()),
	GASSTORAGEPORTABLE("storage.gas.portable", ItemStoragePortableGasCell.THIS(), Integration.Mods.MEKANISMGAS),
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern(), null, null),// Internal EC Item
	UNIVERSALTERMINAL("terminal.universal.wireless", ItemWirelessTerminalUniversal.THIS()),
	GASWIRELESSTERMINAL("terminal.gas.wireless", ItemWirelessTerminalGas.THIS(), Integration.Mods.MEKANISMGAS),
	OCUPGRADE("oc.upgrade", new ItemOCUpgrade(), Integration.Mods.OPENCOMPUTERS);

	private final String internalName;
	private Item item;
	private Integration.Mods mod;

	ItemEnum(String internalName, Item item) {
		this(internalName, item, null);
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod){
		this(internalName, item, mod, CreativeTabEC.INSTANCE);
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod, CreativeTabs creativeTab) {
		this.internalName = internalName;
		this.item = item;
		this.item.setUnlocalizedName("extracells." + this.internalName);
		this.item.setRegistryName(this.internalName);
		this.mod = mod;
		if ((creativeTab != null) && (mod == null || mod.isEnabled()))
			this.item.setCreativeTab(CreativeTabEC.INSTANCE);
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
		return I18n.translateToLocal(this.item.getUnlocalizedName());
	}

	public Integration.Mods getMod(){
		return mod;
	}
}

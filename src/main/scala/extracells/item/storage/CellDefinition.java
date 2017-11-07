package extracells.item.storage;

import java.util.Locale;

import net.minecraft.item.EnumRarity;

public enum CellDefinition {
	PHYSICAL(EnumRarity.EPIC) {
		@Override
		protected void create(StorageRegistry.Builder components, StorageRegistry.Builder cells) {
			components.add(this, 256, 1024, 4096, 16384);
			cells.add(this, 256, 1024, 4096, 16384, 65536);
		}
	},
	FLUID(EnumRarity.RARE),
	GAS(EnumRarity.UNCOMMON);

	public static StorageRegistry components;
	public int componentMetaStart;
	public StorageRegistry cells;

	EnumRarity rarity;

	CellDefinition(EnumRarity rarity) {
		this.rarity = rarity;
	}

	public static void create() {
		StorageRegistry.Builder componentBuilder = new StorageRegistry.Builder("components");
		for (CellDefinition definition : values()) {
			definition.componentMetaStart = componentBuilder.size();
			StorageRegistry.Builder cellBuilder = new StorageRegistry.Builder("cells");
			definition.create(componentBuilder, cellBuilder);
			definition.cells = cellBuilder.build();
		}
		components = componentBuilder.build();
	}

	protected void create(StorageRegistry.Builder components, StorageRegistry.Builder cells) {
		components.add(this, 1, 4, 16, 64, 256, 1024, 4096);
		cells.add(this, 1, 4, 16, 64, 256, 1024, 4096);
	}

	public static CellDefinition get(int index) {
		if (values().length <= index) {
			return values()[0];
		}
		return values()[index];
	}

	public EnumRarity getRarity() {
		return rarity;
	}

	@Override
	public String toString() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}

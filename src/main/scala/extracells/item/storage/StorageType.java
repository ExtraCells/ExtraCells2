package extracells.item.storage;

public class StorageType {
	private int meta;
	private int size;
	private int bytes;
	private String identifier;
	private CellDefinition definition;
	private String storageName;

	public StorageType(CellDefinition definition, int meta, int size, String storageName) {
		this.size = size;
		this.bytes = size * 1024;
		this.identifier = definition + "." + size + "k";
		this.definition = definition;
		this.meta = meta;
		this.storageName = storageName;
	}

	public int getBytes() {
		return bytes;
	}

	public int getSize() {
		return size;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getMeta() {
		return meta;
	}

	public CellDefinition getDefinition() {
		return definition;
	}

	public String getModelName() {
		return "storage/" + definition + "/" + storageName + "/" + size + "k";
	}
}

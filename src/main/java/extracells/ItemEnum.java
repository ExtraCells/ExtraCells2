package extracells;

import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;
import extracells.items.*;

public enum ItemEnum
{
	STORAGEPHYSICAL("item.storage.physical", 4140, ItemStoragePhysical.class, "ID for the physical storage cells", "StoragePhysical"),
	STORAGECOMPONENT("item.storagecomponent", 4141, ItemStorageComponent.class, "ID for the storage clusters (crafting)", "Cluster"),
	STORAGEPHYSICALENCRYPTED("item.storage.physical_secure.encrypted", 4142, ItemSecureStoragePhysicalEncrypted.class, "ID for the encrypted storage cell", "Cell_Encrypted"),
	STORAGEPHYSICALDECRYPTED("item.storage.physical_secure.decrypted", 4143, ItemSecureStoragePhysicalDecrypted.class, "ID for the decrypted storage cell", "Cell_Decrypted"),
	STORAGECASING("item.casing.advanced", 4144, ItemCasing.class, "ID for the advanced storage casing", "AdvancedStorageCasing"),
	STORAGEFLUID("item.storage.fluid", 4145, ItemStorageFluid.class, "ID for the fluid storage cells", "StorageFluid"),
	FLUIDDISPLAY("item.fluiddisplay", 4146, ItemFluidDisplay.class, "ID for the item used for displaying fluids in the terminal and storing them in the ME Network", "FluidDisplayItem");

	private final String internalName;
	private String description, IDName;
	private int ID;
	private Item item;
	private Class<? extends Item> itemClass;

	ItemEnum(String internalName, int ID, Class<? extends Item> itemClass, String description, String IDName)
	{
		this.internalName = internalName;
		this.ID = ID;
		this.itemClass = itemClass;
		this.description = description;
		this.IDName = IDName;
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal(internalName);
	}

	public void setID(int ID)
	{
		this.ID = ID;
	}

	public int getID()
	{
		return ID;
	}

	public void setItemInstance(Item item)
	{
		this.item = item;
	}

	public Item getItemInstance()
	{
		return item;
	}

	public String getDescription()
	{
		return description;
	}

	public String getIDName()
	{
		return IDName;
	}

	public Class<? extends Item> getItemClass()
	{
		return itemClass;
	}
}

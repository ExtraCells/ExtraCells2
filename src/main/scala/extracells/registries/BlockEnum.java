package extracells.registries;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.text.translation.I18n;

import extracells.ExtraCells;
import extracells.block.BlockCertusTank;
import extracells.block.BlockFluidCrafter;
import extracells.block.BlockFluidInterface;
import extracells.block.BlockHardMEDrive;
import extracells.block.BlockVibrationChamberFluid;
import extracells.block.BlockWalrus;
import extracells.integration.Integration;
import extracells.item.ItemBlockCertusTank;
import extracells.item.ItemBlockECBase;

public enum BlockEnum {
	CERTUSTANK("certustank", new BlockCertusTank(), (block)-> new ItemBlockCertusTank(block)),
	WALRUS("walrus", new BlockWalrus()),
	FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter()),
	ECBASEBLOCK("ecbaseblock", new BlockFluidInterface(), (block)-> new ItemBlockECBase(block)),
	BLASTRESISTANTMEDRIVE("hardmedrive", BlockHardMEDrive.instance()),
	VIBRANTCHAMBERFLUID("vibrantchamberfluid", new BlockVibrationChamberFluid());

	private final String internalName;
	private Block block;
	private ItemBlock item;
	private Integration.Mods mod;

	BlockEnum(String internalName, Block block, Integration.Mods mod) {
		this(internalName, block, (b)->new ItemBlock(b), mod);
	}

	BlockEnum(String internalName, Block block) {
		this(internalName, block,(b)-> new ItemBlock(b));
	}

	BlockEnum(String internalName, Block block, ItemFactory itemFactory){
		this(internalName, block, itemFactory, null);
	}

	BlockEnum(String internalName, Block block, ItemFactory factory, Integration.Mods mod) {
		this.internalName = internalName;
		this.block = block;
		this.block.setUnlocalizedName("extracells.block." + this.internalName);
		this.block.setRegistryName(internalName);
		this.item = factory.createItem(block);
		this.item.setRegistryName(block.getRegistryName());
		this.mod = mod;
		if(mod == null || mod.isEnabled())
			this.block.setCreativeTab(ExtraCells.ModTab());
	}

	public Block getBlock() {
		return this.block;
	}

	public String getInternalName() {
		return this.internalName;
	}

	public ItemBlock getItem() {
		return item;
	}

	public String getStatName() {
		return I18n.translateToLocal(this.block.getUnlocalizedName() + ".name");
	}

	public Integration.Mods getMod(){
		return mod;
	}

	protected interface ItemFactory{
		ItemBlock createItem(Block block);
	}
}

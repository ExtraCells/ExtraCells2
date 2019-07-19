package extracells.registries;

import extracells.Extracells;
import extracells.block.*;
import extracells.integration.Integration;
import extracells.item.ItemBlockCertusTank;
import extracells.item.ItemBlockECBase;
import extracells.item.ItemCraftingStorage;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;

public enum BlockEnum {
	CERTUSTANK("certustank", new BlockCertusTank(), ItemBlockCertusTank.class),
	WALRUS("walrus", new BlockWalrus()),
	FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter()),
	ECBASEBLOCK("ecbaseblock", new ECBaseBlock(), ItemBlockECBase.class),
	BLASTRESISTANTMEDRIVE("hardmedrive", BlockHardMEDrive.instance()),
	VIBRANTCHAMBERFLUID("vibrantchamberfluid", new BlockVibrationChamberFluid()),
	CRAFTINGSTORAGE("craftingstorage", new BlockCraftingStorage(), ItemCraftingStorage.class);

	private final String internalName;
	private Block block;
	private Class<? extends ItemBlock> itemBlockClass;
	private Integration.Mods mod;

	BlockEnum(String _internalName, Block _block, Integration.Mods _mod) {
		this(_internalName, _block, ItemBlock.class, _mod);
	}

	BlockEnum(String _internalName, Block _block) {
		this(_internalName, _block, ItemBlock.class);
	}

	BlockEnum(String _internalName, Block _block, Class<? extends ItemBlock> _itemBlockClass){
		this(_internalName, _block, _itemBlockClass, null);
	}

	BlockEnum(String _internalName, Block _block, Class<? extends ItemBlock> _itemBlockClass, Integration.Mods _mod) {
		this.internalName = _internalName;
		this.block = _block;
		this.block.setBlockName(String.format("extracells.block.%s", this.internalName));
		this.itemBlockClass = _itemBlockClass;
		this.mod = _mod;
		if(_mod == null || _mod.isEnabled())
			this.block.setCreativeTab(Extracells.ModTab());
	}

	public Block getBlock() {
		return this.block;
	}

	public String getInternalName() {
		return this.internalName;
	}

	public Class<? extends ItemBlock> getItemBlockClass() {
		return this.itemBlockClass;
	}

	public String getStatName() {
		return StatCollector.translateToLocal(this.block.getUnlocalizedName() + ".name");
	}

	public Integration.Mods getMod(){
		return mod;
	}
}

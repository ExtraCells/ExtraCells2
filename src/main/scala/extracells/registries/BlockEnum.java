package extracells.registries;

import extracells.block.*;
import extracells.tileentity.TileEntityHardMeDrive;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;
import extracells.item.ItemBlockCertusTank;
import extracells.item.ItemBlockECBase;

public enum BlockEnum {
	CERTUSTANK("certustank", new BlockCertusTank(), ItemBlockCertusTank.class),
	WALRUS("walrus", new BlockWalrus()),
	FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter()),
	ECBASEBLOCK("ecbaseblock", new ECBaseBlock(), ItemBlockECBase.class),
	BLASTRESISTANTMEDRIVE("hardmedrive", BlockHardMEDrive.instance()),
	VIBRANTCHAMBERFLUID("vibrantchamberfluid", new BlockVibrationChamberFluid());

	private final String internalName;
	private Block block;
	private Class<? extends ItemBlock> itemBlockClass;

	BlockEnum(String _internalName, Block _block) {
		this(_internalName, _block, ItemBlock.class);
	}

	BlockEnum(String _internalName, Block _block,
			Class<? extends ItemBlock> _itemBlockClass) {
		this.internalName = _internalName;
		this.block = _block;
		this.block.setBlockName("extracells.block." + this.internalName);
		this.itemBlockClass = _itemBlockClass;
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
}

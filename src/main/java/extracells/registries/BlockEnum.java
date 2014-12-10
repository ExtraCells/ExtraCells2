package extracells.registries;

import extracells.block.BlockCertusTank;
import extracells.block.BlockFluidCrafter;
import extracells.block.BlockWalrus;
import extracells.block.ECBaseBlock;
import extracells.item.ItemBlockCertusTank;
import extracells.item.ItemBlockECBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;

public enum BlockEnum {
    CERTUSTANK("certustank", new BlockCertusTank(), ItemBlockCertusTank.class),
    WALRUS("walrus", new BlockWalrus()),
    FLUIDCRAFTER("fluidcrafter", new BlockFluidCrafter()),
    ECBASEBLOCK("ecbaseblock", new ECBaseBlock(), ItemBlockECBase.class);

    private final String internalName;
    private Block block;
    private Class<? extends ItemBlock> itemBlockClass;

    BlockEnum(String _internalName, Block _block) {
        this(_internalName, _block, ItemBlock.class);
    }

    BlockEnum(String _internalName, Block _block, Class<? extends ItemBlock> _itemBlockClass) {
        internalName = _internalName;
        block = _block;
        block.setBlockName("extracells.block." + internalName);
        itemBlockClass = _itemBlockClass;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getStatName() {
        return StatCollector.translateToLocal(block.getUnlocalizedName()+".name");
    }

    public Block getBlock() {
        return block;
    }

    public Class<? extends ItemBlock> getItemBlockClass() {
        return itemBlockClass;
    }
}

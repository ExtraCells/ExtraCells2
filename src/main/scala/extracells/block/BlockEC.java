package extracells.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

import extracells.util.CreativeTabEC;


public abstract class BlockEC extends BlockContainer {

    protected BlockEC(Material material, float hardness, float resistance) {
        super(material);
        setHardness(hardness);
        setResistance(resistance);
        setCreativeTab(CreativeTabEC.INSTANCE);
    }

    protected BlockEC(Material material) {
        super(material);
    }

}

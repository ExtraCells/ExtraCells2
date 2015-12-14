package extracells.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;


public abstract class BlockEC extends BlockContainer {

    protected BlockEC(Material material, float hardness, float resistance) {
        super(material);
        setHardness(hardness);
        setResistance(resistance);
    }

    protected BlockEC(Material material) {
        super(material);
    }

}

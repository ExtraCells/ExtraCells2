package extracells.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockFluidFiller extends ItemBlock {

	public ItemBlockFluidFiller(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
}

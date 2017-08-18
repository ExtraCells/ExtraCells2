package extracells.models;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

public class BlockModelEntry {

	public final ModelResourceLocation blockModelLocation;
	@Nullable
	public final ModelResourceLocation itemModelLocation;
	public final IBakedModel model;
	public final boolean addStateMapper;
	public final Block block;

	public BlockModelEntry(ModelResourceLocation blockModelLocation, ModelResourceLocation itemModelLocation, IBakedModel model, Block block) {
		this(blockModelLocation, itemModelLocation, model, block, true);
	}

	public BlockModelEntry(ModelResourceLocation blockModelLocation, @Nullable ModelResourceLocation itemModelLocation, IBakedModel model, Block block, boolean addStateMapper) {
		this.blockModelLocation = blockModelLocation;
		this.itemModelLocation = itemModelLocation;
		this.model = model;
		this.block = block;
		this.addStateMapper = addStateMapper;
	}

}

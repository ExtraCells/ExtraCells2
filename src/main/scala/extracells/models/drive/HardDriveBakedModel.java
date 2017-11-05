package extracells.models.drive;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import extracells.block.BlockHardMEDrive;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

import extracells.block.properties.PropertyDrive;

//This file is orientated on the applied energetics 2 drive model file.
public class HardDriveBakedModel implements IBakedModel {
	private final IBakedModel bakedBase;
	private final Map<DriveSlotState, IBakedModel> bakedCells;

	public HardDriveBakedModel(IBakedModel bakedBase, Map<DriveSlotState, IBakedModel> bakedCells) {
		this.bakedBase = bakedBase;
		this.bakedCells = bakedCells;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		List<BakedQuad> result = new ArrayList<>();

		result.addAll(bakedBase.getQuads(state, side, rand));

		if (side == null && state instanceof IExtendedBlockState) {
			IExtendedBlockState extState = (IExtendedBlockState) state;
			DriveSlotsState slotsState = extState.getValue(PropertyDrive.INSTANCE);
			EnumFacing direction = extState.getValue(BlockHardMEDrive.FACING());

			if (slotsState == null)
				return result;

			for (int row = 0; row < 3; row++) {
				DriveSlotState slotState = slotsState.getState(row);

				IBakedModel bakedCell = bakedCells.get(slotState);

				Matrix4f transform = new Matrix4f();
				transform.setIdentity();
				float scale = 1 / 16.0F;

				// Position this drive model copy at the correct slot. The transform is based on the
				// cell-model being in slot 0,0 at the top left of the drive.
				float yOffset = -3 * scale + -row * 3 * scale;


				switch (direction){
					case SOUTH:
						transform.setTranslation(new Vector3f(4 * scale, yOffset, 0));
						break;
					case EAST:
						transform.setTranslation(new Vector3f(0, yOffset, -4 * scale));
						break;
					case WEST:
						transform.setTranslation(new Vector3f(0, yOffset, 4 * scale));
						break;
					default:
						transform.setTranslation(new Vector3f(-4 * scale, yOffset, 0));
				}


				MatrixVertexTransformer transformer = new MatrixVertexTransformer(transform);
				for (BakedQuad bakedQuad : bakedCell.getQuads(state, null, rand)) {
					UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(bakedQuad.getFormat());
					transformer.setParent(builder);
					transformer.setVertexFormat(builder.getVertexFormat());
					bakedQuad.pipe(transformer);
					result.add(builder.build());
				}
			}
		}

		return result;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return bakedBase.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return bakedBase.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return bakedBase.isGui3d();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return bakedBase.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return bakedBase.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return bakedBase.getOverrides();
	}
}

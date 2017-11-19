package extracells.models.drive;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import extracells.part.PartDrive;

//This file is orientated on the applied energetics 2 drive model file.
public class PartDriveBakedModel implements IBakedModel {
	private final IBakedModel bakedBase;
	private final Map<DriveSlotState, IBakedModel> bakedCells;

	public PartDriveBakedModel(IBakedModel bakedBase, Map<DriveSlotState, IBakedModel> bakedCells) {
		this.bakedBase = bakedBase;
		this.bakedCells = bakedCells;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		List<BakedQuad> result = new ArrayList<>();

		result.addAll(bakedBase.getQuads(state, side, rand));

		if(!PartDrive.tempDriveStates.isEmpty()){
			DriveSlotsState slotsState = PartDrive.tempDriveStates.remove();
			if (side == null && slotsState != null) {


				for (int row = 0; row < 3; row++) {
					for (int col = 0; col < 2; col++) {
						DriveSlotState slotState = slotsState.getState(row + 3 + col * -3);

						IBakedModel bakedCell = bakedCells.get(slotState);

						Matrix4f transform = new Matrix4f();
						transform.setIdentity();
						float scale = 1 / 16.0F;

						// Position this drive model copy at the correct slot. The transform is based on the
						// cell-model being in slot 0,0 at the top left of the drive.
						float xOffset = -col * 5 * scale;
						float yOffset = -row * 3 * scale;

						transform.setTranslation(new Vector3f(xOffset, -3 * scale + yOffset, 0));

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


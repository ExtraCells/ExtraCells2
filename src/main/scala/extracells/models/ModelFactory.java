package extracells.models;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import org.lwjgl.util.vector.Vector3f;

import java.util.List;

public class ModelFactory {
	private static final FaceBakery bakery = new FaceBakery();

	public static ImmutableList<BakedQuad> createCube(Vector3f from, Vector3f to, TextureAtlasSprite sprite) {
		ImmutableList.Builder builder = new ImmutableList.Builder();
		for (EnumFacing facing : EnumFacing.VALUES) {
			builder.add(createQuad(facing, from, to, sprite));
		}
		return builder.build();
	}

	public static ImmutableList<BakedQuad> createCubeOpen(Vector3f from, Vector3f to, TextureAtlasSprite sprite, List<EnumFacing> openSides) {
		ImmutableList.Builder builder = new ImmutableList.Builder();
		for (EnumFacing facing : EnumFacing.VALUES) {
			if(openSides.contains(facing))
				continue;
			builder.add(createQuad(facing, from, to, sprite));
		}
		return builder.build();
	}

	public static BakedQuad createQuad(EnumFacing facing, Vector3f from, Vector3f to, TextureAtlasSprite sprite) {
		float[] uvs = getFaceUvs(facing, to, from);
		BlockFaceUV uv = new BlockFaceUV(uvs, 0);
		BlockPartFace bpf = new BlockPartFace(facing, 0, "", uv);
		return bakery.makeBakedQuad(from, to, bpf, sprite, facing, ModelRotation.X0_Y0, null, true, true);
	}

	protected static float[] getFaceUvs(EnumFacing face, Vector3f to, Vector3f from) {
		float minU;
		float minV;
		float maxU;
		float maxV;
		switch (face) {
			case SOUTH: {
				minU = from.x;
				minV = from.y;
				maxU = to.x;
				maxV = to.y;
				break;
			}
			case NORTH: {
				minU = from.x;
				minV = from.y;
				maxU = to.x;
				maxV = to.y;
				break;
			}
			case WEST: {
				minU = from.z;
				minV = from.y;
				maxU = to.z;
				maxV = to.y;
				break;
			}
			case EAST: {
				minU = from.z;
				minV = from.y;
				maxU = to.z;
				maxV = to.y;
				break;
			}
			case UP: {
				minU = from.x;
				minV = from.z;
				maxU = to.x;
				maxV = to.z;
				break;
			}
			case DOWN: {
				minU = from.x;
				minV = from.z;
				maxU = to.x;
				maxV = to.z;
				break;
			}
			default: {
				minU = 0;
				minV = 0;
				maxU = 16;
				maxV = 16;
				break;
			}
		}
		if (minU < 0 || maxU > 16) {
			minU = 0;
			maxU = 16;
		}
		if (minV < 0 || maxV > 16) {
			minV = 0;
			maxV = 16;
		}
		minU = 16 - minU;
		minV = 16 - minV;
		maxU = 16 - maxU;
		maxV = 16 - maxV;
		return new float[]{minU, minV, maxU, maxV};
	}
}

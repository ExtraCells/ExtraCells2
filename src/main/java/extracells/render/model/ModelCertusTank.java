package extracells.render.model;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import extracells.tileentity.TileEntityCertusTank;

public class ModelCertusTank extends ModelBase
{

	ModelRenderer Shape1;

	public ModelCertusTank()
	{
		textureWidth = 64;
		textureHeight = 64;

		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 14, 16, 14);
		Shape1.setRotationPoint(-7F, -8F, -7F);
		Shape1.setTextureSize(textureWidth, textureHeight);
		Shape1.mirror = true;
		setRotation(Shape1, 0F, 0F, 0F);

	}

	public void render(float f)
	{
		Shape1.render(f);
	}

	public void renderOuterBlock(Block block, int x, int y, int z, RenderBlocks renderer, IBlockAccess world)
	{

		Tessellator tessellator = Tessellator.instance;
		boolean tankUp = world.getBlockTileEntity(x, y + 1, z) instanceof TileEntityCertusTank;
		boolean tankDown = world.getBlockTileEntity(x, y - 1, z) instanceof TileEntityCertusTank;
		int meta = 0;
		if (tankUp && tankDown)
			meta = 3;
		else if (tankUp)
			meta = 2;
		else if (tankDown)
			meta = 1;
		if (!tankDown)
		{
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderer.renderFaceYNeg(block, x, y, z, block.getIcon(0, 0));
		}
		if (!(tankUp))
		{
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderer.renderFaceYPos(block, x, y, z, block.getIcon(1, 0));
		}

		Icon sideIcon = block.getIcon(3, meta);
		tessellator.setNormal(0.0F, 0.0F, -1F);
		renderer.renderFaceZNeg(block, x, y, z, sideIcon);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, x, y, z, sideIcon);
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, x, y, z, sideIcon);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, x, y, z, sideIcon);

	}

	public void renderInnerBlock(Block block, int x, int y, int z, RenderBlocks renderer, IBlockAccess world)
	{
		Tessellator tessellator = Tessellator.instance;
		boolean tankUp = world.getBlockTileEntity(x, y + 1, z) instanceof TileEntityCertusTank;
		boolean tankDown = world.getBlockTileEntity(x, y - 1, z) instanceof TileEntityCertusTank;
		int meta = 0;
		if (tankUp && tankDown)
			meta = 3;
		else if (tankUp)
			meta = 2;
		else if (tankDown)
			meta = 1;
		if (!tankDown)
		{
			tessellator.setNormal(0.0F, -1F, 0.0F);
			renderer.renderFaceYNeg(block, x, y + 0.001D, z, block.getIcon(0, 0));
		}
		if (!(tankUp))
		{
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			renderer.renderFaceYPos(block, x, y - 0.001D, z, block.getIcon(1, 0));
		}
		Icon sideIcon = block.getIcon(3, meta);
		tessellator.setNormal(0.0F, 0.0F, -1F);
		renderer.renderFaceZNeg(block, x, y, z + 0.001D, sideIcon);
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		renderer.renderFaceZPos(block, x, y, z - 0.001D, sideIcon);
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		renderer.renderFaceXNeg(block, x + 0.001D, y, z, sideIcon);
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, x - 0.001D, y, z, sideIcon);

	}

	public void renderFluid(TileEntity tileEntity, double x, double y, double z, RenderBlocks renderer)
	{
		Tessellator tessellator = Tessellator.instance;
		tessellator.setColorOpaque(255, 255, 255);
		if (tileEntity != null && ((TileEntityCertusTank) tileEntity).getTankInfo(ForgeDirection.UNKNOWN)[0].fluid != null)
		{
			Fluid storedFluid = ((TileEntityCertusTank) tileEntity).getRenderFluid();
			float scale = ((TileEntityCertusTank) tileEntity).getRenderScale();
			if (storedFluid != null && scale > 0)
			{
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				Block id = Block.blocksList[FluidRegistry.WATER.getBlockID()];
				Icon fluidIcon = storedFluid.getIcon();
				if (fluidIcon == null)
					fluidIcon = FluidRegistry.LAVA.getIcon();
				renderer.setRenderBounds(0.08F, 0.001F, 0.08F, 0.92, scale * 0.999F, 0.92F);
				tessellator.setNormal(0.0F, -1F, 0.0F);
				renderer.renderFaceYNeg(id, x, y, z, fluidIcon);
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				renderer.renderFaceYPos(id, x, y, z, fluidIcon);
				tessellator.setNormal(0.0F, 0.0F, -1F);
				renderer.renderFaceZNeg(id, x, y, z, fluidIcon);
				tessellator.setNormal(0.0F, 0.0F, 1.0F);
				renderer.renderFaceZPos(id, x, y, z, fluidIcon);
				tessellator.setNormal(-1F, 0.0F, 0.0F);
				renderer.renderFaceXNeg(id, x, y, z, fluidIcon);
				tessellator.setNormal(1.0F, 0.0F, 0.0F);
				renderer.renderFaceXPos(id, x, y, z, fluidIcon);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}

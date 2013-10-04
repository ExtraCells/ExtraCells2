package extracells.model;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import extracells.tile.TileEntityCertusTank;

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

	public void render(TileEntity tileEntity, double x, double y, double z)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/blocks/texmap_tank.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.51F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		Shape1.render(0.0625F);

		// render same cube again, but inside out, so it has inner textures :D
		GL11.glScalef(1.0F, 1F, -1F);
		Shape1.render(0.0625F);

		if (tileEntity != null && ((TileEntityCertusTank) tileEntity).getTankInfo(ForgeDirection.UNKNOWN)[0].fluid != null)
		{
			FluidStack storedFluid = ((TileEntityCertusTank) tileEntity).getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
			int tankCapacity = ((TileEntityCertusTank) tileEntity).getTankInfo(ForgeDirection.UNKNOWN)[0].capacity;

			if (storedFluid != null && storedFluid.getFluid() != null)
			{
				Icon fluidIcon = storedFluid.getFluid().getIcon();

				Tessellator tessellator = Tessellator.instance;
				RenderBlocks renderer = new RenderBlocks();

				GL11.glScalef(1.0F, -1.0F, 1.0F);
				renderer.setRenderBounds(0.08F, 0.001F, 0.08F, 0.92, (float) ((float) storedFluid.amount / (float) tankCapacity) * 0.999F, 0.92F);
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
				GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
				
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, -1F, 0.0F);
				renderer.renderFaceYNeg(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				renderer.renderFaceYPos(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 0.0F, -1F);
				renderer.renderFaceZNeg(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 0.0F, 1.0F);
				renderer.renderFaceZPos(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();
				tessellator.startDrawingQuads();
				tessellator.setNormal(-1F, 0.0F, 0.0F);
				renderer.renderFaceXNeg(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();
				tessellator.startDrawingQuads();
				tessellator.setNormal(1.0F, 0.0F, 0.0F);
				renderer.renderFaceXPos(Block.blocksList[FluidRegistry.WATER.getBlockID()], 0.0D, 0.0D, 0.0D, fluidIcon);
				tessellator.draw();

				GL11.glPopAttrib();
			}
		}

		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
	{
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
}

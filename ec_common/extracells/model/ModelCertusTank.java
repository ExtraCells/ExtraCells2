package extracells.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class ModelCertusTank extends ModelBase
{
	ModelRenderer Shape1;
	ModelRenderer Shape2;

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

		Shape2 = new ModelRenderer(this, 0, 30);
		Shape2.addBox(0F, 0F, 0F, 12, 14, 12);
		Shape2.setRotationPoint(-6F, -7F, -6F);
		Shape2.setTextureSize(textureWidth, textureHeight);
		Shape2.mirror = true;
		setRotation(Shape2, 0F, 0F, 0F);
	}

	public void render(float f)
	{
		Shape1.render(f);
		Shape2.render(f);
	}

	public void render(TileEntity tileEntity, double x, double y, double z)
	{
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/blocks/texmap_tank.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		Shape1.render(0.0625F);
		Shape2.render(0.0625F);
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

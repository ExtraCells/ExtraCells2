package extracells.render.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class ModelBusFluidImport extends ModelBase
{
	ModelRenderer Shape1;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
	ModelRenderer Shape4;

	public ModelBusFluidImport()
	{
		textureWidth = 64;
		textureHeight = 64;

		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 10, 2, 10);
		Shape1.setRotationPoint(-5F, 22F, -5F);
		Shape1.setTextureSize(64, 64);
		Shape1.mirror = true;
		setRotation(Shape1, 0F, 0F, 0F);
		Shape2 = new ModelRenderer(this, 0, 12);
		Shape2.addBox(0F, 0F, 0F, 9, 2, 9);
		Shape2.setRotationPoint(-4.5F, 20.5F, -4.5F);
		Shape2.setTextureSize(64, 64);
		Shape2.mirror = true;
		setRotation(Shape2, 0F, 0F, 0F);
		Shape3 = new ModelRenderer(this, 0, 23);
		Shape3.addBox(0F, 0F, 0F, 8, 2, 8);
		Shape3.setRotationPoint(-4F, 19.2F, -4F);
		Shape3.setTextureSize(64, 64);
		Shape3.mirror = true;
		setRotation(Shape3, 0F, 0F, 0F);
		Shape4 = new ModelRenderer(this, 0, 33);
		Shape4.addBox(0F, 0F, 0F, 6, 2, 6);
		Shape4.setRotationPoint(-3F, 18F, -3F);
		Shape4.setTextureSize(64, 64);
		Shape4.mirror = true;
		setRotation(Shape4, 0F, 0F, 0F);
	}

	public void render(float f5)
	{
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
	}

	public void render(TileEntity tileEntity, double x, double y, double z)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/blocks/texmap_import_bus.png"));
		GL11.glPushMatrix();

		switch (ForgeDirection.getOrientation(tileEntity.getBlockMetadata()))
		{
		case UP:
			GL11.glTranslatef((float) x + 0.5F, (float) y + -0.5F, (float) z + 0.5F);
			GL11.glRotatef(180F, 1F, 0F, 0F);
			break;
		case DOWN:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glRotatef(0F, 0F, 0F, 0F);
			break;

		case SOUTH:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + -0.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(180F, 0F, 0F, 1F);
			break;
		case NORTH:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 1.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(0F, 0F, 0F, 1F);
			break;
		case WEST:
			GL11.glTranslatef((float) x + 1.5F, (float) y + 0.5F, (float) z + 0.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(-90F, 0F, 0F, 1F);
			break;
		case EAST:
			GL11.glTranslatef((float) x + -0.5F, (float) y + 0.5F, (float) z + 0.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(90F, 0F, 0F, 1F);
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		GL11.glScalef(0.88F, -1F, -0.88F);
		this.render(0.0625f);
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

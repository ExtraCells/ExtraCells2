package extracells.model;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class ModelCable extends ModelBase
{
	ModelRenderer Shape1;

	public ModelCable()
	{
		textureWidth = 64;
		textureHeight = 32;

		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 4, 6, 4);
		Shape1.setRotationPoint(-2F, 18F, -2F);
		Shape1.setTextureSize(64, 32);
		Shape1.mirror = true;
		setRotation(Shape1, 0F, 0F, 0F);
	}

	public void render(double x, double y, double z, ForgeDirection orientation)
	{
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/blocks/texmap_cable.png"));
		GL11.glPushMatrix();
		
		switch (orientation)
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

		GL11.glScalef(0.88F, -1.001F, -0.88F);
		this.render(0.0625f);
		GL11.glPopMatrix();
	}

	public void render(float f)
	{
		Shape1.render(f);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}

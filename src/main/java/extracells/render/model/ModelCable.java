package extracells.render.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class ModelCable extends ModelBase
{
	ModelRenderer CableExtend;
	ModelRenderer CableBase;

	public ModelCable()
	{
		textureWidth = 64;
		textureHeight = 32;

		CableExtend = new ModelRenderer(this, 0, 4);
		CableExtend.addBox(0F, 0F, 0F, 4, 6, 4);
		CableExtend.setRotationPoint(-2F, 18F, -2F);
		CableExtend.setTextureSize(64, 32);
		CableExtend.mirror = true;
		setRotation(CableExtend, 0F, 0F, 0F);
		CableBase = new ModelRenderer(this, 0, 0);
		CableBase.addBox(0F, 0F, 0F, 4, 4, 4);
		CableBase.setRotationPoint(-2F, 14F, -2F);
		CableBase.setTextureSize(64, 32);
		CableBase.mirror = true;
		setRotation(CableBase, 0F, 0F, 0F);
	}

	public void renderExtend(double x, double y, double z, ForgeDirection orientation, Colors color)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(color.getTexture());
		GL11.glPushMatrix();

		switch (orientation)
		{
		case UP:
			GL11.glTranslatef((float) x + 0.5F, (float) y + -0.57F, (float) z + 0.5F);
			GL11.glRotatef(180F, 1F, 0F, 0F);
			break;
		case DOWN:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.57F, (float) z + 0.5F);
			GL11.glRotatef(0F, 0F, 0F, 0F);
			break;

		case SOUTH:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + -0.57F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(180F, 0F, 0F, 1F);
			break;
		case NORTH:
			GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 1.57F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(0F, 0F, 0F, 1F);
			break;
		case WEST:
			GL11.glTranslatef((float) x + 1.57F, (float) y + 0.5F, (float) z + 0.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(-90F, 0F, 0F, 1F);
			break;
		case EAST:
			GL11.glTranslatef((float) x + -0.57F, (float) y + 0.5F, (float) z + 0.5F);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glRotatef(90F, 0F, 0F, 1F);
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		GL11.glScalef(0.8799F, -1.05F, -0.8799F);
		CableExtend.render(0.0625f);
		GL11.glPopMatrix();
	}

	public void renderBase(double x, double y, double z, Colors color)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(color.getTexture());
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + -0.5F, (float) z + 0.5F);
		GL11.glScalef(0.8799F, 1, 0.8799F);
		CableBase.render(0.0625f);
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public enum Colors
	{
		CLEAR("blank.png"),
		BLACK("black.png"),
		BLUE("blue.png"),
		BROWN("brown.png"),
		GREEN("green.png"),
		RED("red.png"),
		WHITE("white.png"),
		YELLOW("yellow.png");

		private final String path;

		Colors(String path)
		{
			this.path = path;
		}

		ResourceLocation getTexture()
		{
			return new ResourceLocation("extracells", "textures/blocks/cable_textures/" + path);
		}

		public static Colors getColorByID(int ID)
		{
			switch (ID)
			{
			case 0:
				return BLUE;
			case 1:
				return BLACK;
			case 2:
				return WHITE;
			case 3:
				return BROWN;
			case 4:
				return RED;
			case 5:
				return YELLOW;
			case 6:
				return GREEN;
			default:
				return CLEAR;
			}
		}
	}
}

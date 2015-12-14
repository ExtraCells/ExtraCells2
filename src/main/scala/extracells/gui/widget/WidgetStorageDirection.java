package extracells.gui.widget;

import appeng.api.config.AccessRestriction;
import com.google.common.base.Splitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WidgetStorageDirection extends GuiButton {

	private AccessRestriction access;

	public WidgetStorageDirection(int ID, int xPos, int yPos, int width,
			int height, AccessRestriction mode) {
		super(ID, xPos, yPos, width, height, "");
		this.access = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mouseDragged(minecraftInstance, x, y);

		minecraftInstance.getTextureManager().bindTexture(
				new ResourceLocation("appliedenergistics2",
						"textures/guis/states.png"));
		drawTexturedModalRect(this.xPosition, this.yPosition, 240, 240, 16, 16);

		switch (this.access) {
		case NO_ACCESS:
			drawTexturedModalRect(this.xPosition, this.yPosition, 96, 0, 16, 16);
			break;
		case READ:
			drawTexturedModalRect(this.xPosition, this.yPosition, 16, 144, 16,
					16);
			break;
		case READ_WRITE:
			drawTexturedModalRect(this.xPosition, this.yPosition, 32, 144, 16,
					16);
			break;
		case WRITE:
			drawTexturedModalRect(this.xPosition, this.yPosition, 0, 144, 16,
					16);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("rawtypes")
	protected void drawHoveringText(List list, int x, int y,
			FontRenderer fontrenderer) {
		if (!list.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			Iterator iterator = list.iterator();

			while (iterator.hasNext()) {
				String s = (String) iterator.next();
				int l = fontrenderer.getStringWidth(s);

				if (l > k) {
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if (list.size() > 1) {
				k1 += 2 + (list.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4,
					l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1,
					l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3,
					l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3
					- 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1
					+ 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2,
					i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3,
					j2, j2);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				String s1 = (String) list.get(k2);
				fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public void drawTooltip(int mouseX, int mouseY, int guiXPos, int guiYPos) {
		List<String> description = new ArrayList<String>();
		description.add(StatCollector
				.translateToLocal("gui.tooltips.appliedenergistics2.IOMode"));
		String explanation = "";
		switch (this.access) {
		case NO_ACCESS:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.Disabled");
			break;
		case READ:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.Read");
			break;
		case READ_WRITE:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.ReadWrite");
			break;
		case WRITE:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.Write");
			break;
		default:
			break;
		}

		for (String current : Splitter.fixedLength(30).split(explanation)) {
			description.add(EnumChatFormatting.GRAY + current);
		}

		Minecraft mc = Minecraft.getMinecraft();

		if (mouseX >= this.xPosition && mouseX <= this.xPosition + this.width
				&& mouseY >= this.yPosition
				&& mouseY <= this.yPosition + this.height) {
			drawHoveringText(description, mouseX - guiXPos, mouseY - guiYPos,
					mc.fontRenderer);
		}
	}

	public AccessRestriction getAccessRestriction() {
		return this.access;
	}

	public void setAccessRestriction(AccessRestriction mode) {
		this.access = mode;
	}
}

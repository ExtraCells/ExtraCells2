package extracells.gui.widget;

import appeng.api.config.RedstoneMode;
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

public class WidgetRedstoneModes extends GuiButton {

	private RedstoneMode redstoneMode;
	private boolean emitter = false;

	public WidgetRedstoneModes(int ID, int xPos, int yPos, int width,
			int height, RedstoneMode mode) {
		super(ID, xPos, yPos, width, height, "ScrewStrings :D");
		this.emitter = false;
		this.redstoneMode = mode;
	}

	public WidgetRedstoneModes(int ID, int xPos, int yPos, int width,
			int height, RedstoneMode mode, boolean emitter) {
		super(ID, xPos, yPos, width, height, "ScrewStrings :D");
		this.emitter = emitter;
		this.redstoneMode = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mouseDragged(minecraftInstance, x, y);

		minecraftInstance.getTextureManager().bindTexture(
				new ResourceLocation("extracells",
						"textures/gui/redstonemodes.png"));
		drawTexturedModalRect(this.xPosition, this.yPosition, 0, 16, 16, 16);

		switch (this.redstoneMode) {
		case HIGH_SIGNAL:
			drawTexturedModalRect(this.xPosition, this.yPosition, 16, 0, 16, 16);
			break;
		case LOW_SIGNAL:
			drawTexturedModalRect(this.xPosition, this.yPosition, 0, 0, 16, 16);
			break;
		case SIGNAL_PULSE:
			drawTexturedModalRect(this.xPosition, this.yPosition, 32, 0, 16, 16);
			break;
		case IGNORE:
			drawTexturedModalRect(this.xPosition, this.yPosition, 48, 0, 16, 16);
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
		description
				.add(StatCollector
						.translateToLocal("gui.tooltips.appliedenergistics2.RedstoneMode"));
		String explanation = "";
		switch (this.redstoneMode) {
		case HIGH_SIGNAL:
			explanation = StatCollector
					.translateToLocal(this.emitter ? "gui.tooltips.appliedenergistics2.EmitLevelAbove"
							: "gui.tooltips.appliedenergistics2.ActiveWithSignal");
			break;
		case LOW_SIGNAL:
			explanation = StatCollector
					.translateToLocal(this.emitter ? "gui.tooltips.appliedenergistics2.EmitLevelsBelow"
							: "gui.tooltips.appliedenergistics2.ActiveWithoutSignal");
			break;
		case SIGNAL_PULSE:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.ActiveOnPulse");
			break;
		case IGNORE:
			explanation = StatCollector
					.translateToLocal("gui.tooltips.appliedenergistics2.AlwaysActive");
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

	public void setRedstoneMode(RedstoneMode mode) {
		this.redstoneMode = mode;
	}
}

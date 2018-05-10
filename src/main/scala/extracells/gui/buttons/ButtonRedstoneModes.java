package extracells.gui.buttons;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import extracells.gui.IToolTipProvider;

import appeng.api.config.RedstoneMode;

public class ButtonRedstoneModes extends GuiButton implements IToolTipProvider {

	private RedstoneMode redstoneMode;
	private boolean emitter = false;

	public ButtonRedstoneModes(int ID, int xPos, int yPos, int width, int height, RedstoneMode mode) {
		super(ID, xPos, yPos, width, height, "ScrewStrings :D");
		this.emitter = false;
		this.redstoneMode = mode;
	}

	public ButtonRedstoneModes(int ID, int xPos, int yPos, int width, int height, RedstoneMode mode, boolean emitter) {
		super(ID, xPos, yPos, width, height, "ScrewStrings :D");
		this.emitter = emitter;
		this.redstoneMode = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y, float partialTicks) {
		this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mouseDragged(minecraftInstance, x, y);

		minecraftInstance.getTextureManager().bindTexture(new ResourceLocation("extracells", "textures/gui/redstonemodes.png"));
		drawTexturedModalRect(this.x, this.y, 0, 16, 16, 16);

		switch (this.redstoneMode) {
			case HIGH_SIGNAL:
				drawTexturedModalRect(this.x, this.y, 16, 0, 16, 16);
				break;
			case LOW_SIGNAL:
				drawTexturedModalRect(this.x, this.y, 0, 0, 16, 16);
				break;
			case SIGNAL_PULSE:
				drawTexturedModalRect(this.x, this.y, 32, 0, 16, 16);
				break;
			case IGNORE:
				drawTexturedModalRect(this.x, this.y, 48, 0, 16, 16);
				break;
			default:
				break;
		}
	}

	@Override
	public List<String> getToolTip(int mouseX, int mouseY) {
		List<String> description = new ArrayList<String>();
		description.add(I18n.translateToLocal("gui.tooltips.appliedenergistics2.RedstoneMode"));
		String explanation = "";
		switch (this.redstoneMode) {
			case HIGH_SIGNAL:
				explanation = I18n.translateToLocal(this.emitter ? "gui.tooltips.appliedenergistics2.EmitLevelAbove" : "gui.tooltips.appliedenergistics2.ActiveWithSignal");
				break;
			case LOW_SIGNAL:
				explanation = I18n.translateToLocal(this.emitter ? "gui.tooltips.appliedenergistics2.EmitLevelsBelow" : "gui.tooltips.appliedenergistics2.ActiveWithoutSignal");
				break;
			case SIGNAL_PULSE:
				explanation = I18n.translateToLocal("gui.tooltips.appliedenergistics2.ActiveOnPulse");
				break;
			case IGNORE:
				explanation = I18n.translateToLocal("gui.tooltips.appliedenergistics2.AlwaysActive");
				break;
			default:
				break;
		}
		for (String current : Splitter.fixedLength(30).split(explanation)) {
			description.add(TextFormatting.GRAY + current);
		}
		return description;
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return isMouseOver();
	}

	public void setRedstoneMode(RedstoneMode mode) {
		this.redstoneMode = mode;
	}
}

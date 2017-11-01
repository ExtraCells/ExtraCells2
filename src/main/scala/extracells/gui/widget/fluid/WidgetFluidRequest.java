package extracells.gui.widget.fluid;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import extracells.gui.GuiTerminal;

public class WidgetFluidRequest extends AbstractFluidWidget {

	public WidgetFluidRequest(GuiTerminal guiFluidTerminal, Fluid fluid) {
		super(guiFluidTerminal, 18, 18, fluid);
	}

	@Override
	public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
		if (this.fluid == null
			|| !isPointInRegion(posX, posY, this.height, this.width,
			mouseX, mouseY)) {
			return false;
		}

		List<String> description = new ArrayList<String>();
		description
			.add(I18n
				.translateToLocal("gui.tooltips.appliedenergistics2.Craftable"));
		description.add(this.fluid.getLocalizedName(new FluidStack(this.fluid,
			1)));
		drawHoveringText(description, mouseX - this.guiFluidTerminal.guiLeft(),
			mouseY - this.guiFluidTerminal.guiTop() + 18,
			Minecraft.getMinecraft().fontRenderer);
		return true;
	}

	@Override
	public void drawWidget(int posX, int posY) {
		Minecraft.getMinecraft().renderEngine
			.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor3f(1, 1, 1);
		if (this.fluid != null) {
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
			drawTexturedModalRect(posX + 1, posY + 1, sprite, this.height - 2, this.width - 2);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			String str = I18n.translateToLocal("extracells.gui.craft");
			str = WordUtils.capitalize(str.toLowerCase());
			Minecraft.getMinecraft().fontRenderer.drawString(
				TextFormatting.WHITE + str, 52 + posX - str.length(),
				posY + 24, 0);
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	public void mouseClicked(int posX, int posY, int mouseX, int mouseY) {
		// TODO
	}
}

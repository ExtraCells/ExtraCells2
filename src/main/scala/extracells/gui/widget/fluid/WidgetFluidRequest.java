package extracells.gui.widget.fluid;

import extracells.gui.GuiFluidTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WidgetFluidRequest extends AbstractFluidWidget {

	public WidgetFluidRequest(GuiFluidTerminal guiFluidTerminal, Fluid fluid) {
		super(guiFluidTerminal, 18, 18, fluid);
	}

	@Override
	public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
		if (this.fluid == null
				|| !isPointInRegion(posX, posY, this.height, this.width,
						mouseX, mouseY))
			return false;

		List<String> description = new ArrayList<String>();
		description
				.add(StatCollector
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
				.bindTexture(TextureMap.locationBlocksTexture);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor3f(1, 1, 1);
		if (this.fluid != null && this.fluid.getIcon() != null) {
			GL11.glColor3f((this.fluid.getColor() >> 16 & 0xFF) / 255.0F, (this.fluid.getColor() >> 8 & 0xFF) / 255.0F, (this.fluid.getColor() & 0xFF) / 255.0F);
			drawTexturedModelRectFromIcon(posX + 1, posY + 1,
					this.fluid.getIcon(), this.height - 2, this.width - 2);
			GL11.glColor3f(1.0F, 1.0F, 1.0F);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			String str = StatCollector.translateToLocal("extracells.gui.craft");
			str = WordUtils.capitalize(str.toLowerCase());
			Minecraft.getMinecraft().fontRenderer.drawString(
					EnumChatFormatting.WHITE + str, 52 + posX - str.length(),
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

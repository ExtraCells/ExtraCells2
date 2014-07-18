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
    public void drawWidget(int posX, int posY) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(1, 1, 1);
        if (fluid != null && fluid.getIcon() != null) {
            drawTexturedModelRectFromIcon(posX + 1, posY + 1, fluid.getIcon(), height - 2, width - 2);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            String str = StatCollector.translateToLocal("AppEng.Terminal.Craft");
            str = WordUtils.capitalize(str.toLowerCase());
            Minecraft.getMinecraft().fontRenderer.drawString(EnumChatFormatting.WHITE + str, 52 + posX - str.length(), posY + 24, 0);
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void mouseClicked(int posX, int posY, int mouseX, int mouseY) {
        // TODO
    }

    @Override
    public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
        if (fluid == null || !isPointInRegion(posX, posY, height, width, mouseX, mouseY))
            return false;

        List<String> description = new ArrayList<String>();
        description.add(StatCollector.translateToLocal("AppEng.GuiITooltip.Craftable"));
        description.add(fluid.getLocalizedName(new FluidStack(fluid, 1)));
        drawHoveringText(description, mouseX - guiFluidTerminal.guiLeft(), mouseY - guiFluidTerminal.guiTop(), Minecraft.getMinecraft().fontRenderer);
        return true;
    }
}

package extracells.gui.widget.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import appeng.api.storage.data.IAEFluidStack;
import extracells.util.FluidUtil;

public class WidgetFluidSelector extends AbstractFluidWidget {

    private long amount = 0;
    private int color;
    private int borderThickness;

    public WidgetFluidSelector(IFluidSelectorGui guiFluidTerminal, IAEFluidStack stack) {
        super(guiFluidTerminal, 18, 18, stack.getFluidStack().getFluid());
        this.amount = stack.getStackSize();
        this.color = 0xFF00FFFF;
        this.borderThickness = 1;
    }

    private void drawHollowRectWithCorners(int posX, int posY, int height, int width, int color, int thickness) {
        drawRect(posX, posY, posX + height, posY + thickness, color);
        drawRect(posX, posY + width - thickness, posX + height, posY + width, color);
        drawRect(posX, posY, posX + thickness, posY + width, color);
        drawRect(posX + height - thickness, posY, posX + height, posY + width, color);

        drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color);
        drawRect(posX + height, posY + width, posX + height - thickness - 1, posY + width - thickness - 1, color);
        drawRect(posX + height, posY, posX + height - thickness - 1, posY + thickness + 1, color);
        drawRect(posX, posY + width, posX + thickness + 1, posY + width - thickness - 1, color);
    }

    @Override
    public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
        if (this.fluid == null || this.amount <= 0
                || !isPointInRegion(posX, posY, this.height, this.width, mouseX, mouseY))
            return false;

        String amountToText = FluidUtil.formatFluidAmount(this.amount, true);

        List<String> description = new ArrayList<String>();
        description.add(this.fluid.getLocalizedName(new FluidStack(this.fluid, 0)));
        description.add(amountToText);
        drawHoveringText(
                description,
                mouseX - this.guiFluidTerminal.guiLeft(),
                mouseY - this.guiFluidTerminal.guiTop() + 18,
                Minecraft.getMinecraft().fontRenderer);
        return true;
    }

    @Override
    public void drawWidget(int posX, int posY) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(1F, 1F, 1F);

        IAEFluidStack terminalFluid = ((IFluidSelectorGui) this.guiFluidTerminal).getCurrentFluid();
        Fluid currentFluid = terminalFluid != null ? terminalFluid.getFluid() : null;

        if (this.fluid != null && this.fluid.getIcon() != null) {
            GL11.glColor3f(
                    (this.fluid.getColor() >> 16 & 0xFF) / 255.0F,
                    (this.fluid.getColor() >> 8 & 0xFF) / 255.0F,
                    (this.fluid.getColor() & 0xFF) / 255.0F);
            drawTexturedModelRectFromIcon(posX + 1, posY + 1, this.fluid.getIcon(), this.height - 2, this.width - 2);
        }
        GL11.glColor3f(1F, 1F, 1F);

        final float scaleFactor = 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final float offset = -1.0f;
        final String stackSize = FluidUtil.formatFluidAmount(this.amount);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        GL11.glScaled(scaleFactor, scaleFactor, scaleFactor);

        final int X = (int) (((float) posX + offset + 16.0f - fontRenderer.getStringWidth(stackSize) * scaleFactor)
                * inverseScaleFactor);
        final int Y = (int) (((float) posY + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        if (this.fluid == currentFluid)
            drawHollowRectWithCorners(posX, posY, this.height, this.width, this.color, this.borderThickness);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public long getAmount() {
        return this.amount;
    }

    @Override
    public void mouseClicked(int posX, int posY, int mouseX, int mouseY) {
        if (this.fluid != null && isPointInRegion(posX, posY, this.height, this.width, mouseX, mouseY)) {
            ((IFluidSelectorGui) this.guiFluidTerminal).getContainer().setSelectedFluid(this.fluid);
        }
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}

package extracells.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;
import extracells.Extracells;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WidgetFluidSelector extends AbstractFluidWidget {

	private final static int SELECTED_RECT_COLOR = 0xFF00FFFF;
	private final static int SELECTED_BORDER_THICKNESS = 1;
	private final static float FONT_SCALE = 0.5f;

	public final static int SIZE = 18;

	private long amount;

	public WidgetFluidSelector(IFluidSelectorGui guiFluidTerminal,
			IAEFluidStack stack) {
		super(guiFluidTerminal, SIZE, SIZE, stack.getFluidStack().getFluid());
		this.amount = stack.getStackSize();
	}

	private void drawHollowRectWithCorners(int posX, int posY, int height,
			int width, int color, int thickness) {
		drawRect(posX, posY, posX + height, posY + thickness, color);
		drawRect(posX, posY + width - thickness, posX + height, posY + width,
				color);
		drawRect(posX, posY, posX + thickness, posY + width, color);
		drawRect(posX + height - thickness, posY, posX + height, posY + width,
				color);

		drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color);
		drawRect(posX + height, posY + width, posX + height - thickness - 1,
				posY + width - thickness - 1, color);
		drawRect(posX + height, posY, posX + height - thickness - 1, posY
				+ thickness + 1, color);
		drawRect(posX, posY + width, posX + thickness + 1, posY + width
				- thickness - 1, color);
	}

	@Override
	public boolean drawTooltip(int posX, int posY, int mouseX, int mouseY) {
		if (this.fluid == null
				|| this.amount <= 0
				|| !isPointInRegion(posX, posY, this.height, this.width,
						mouseX, mouseY))
			return false;

		String amountToText = FluidUtil.getAmountAsPrettyString(this.amount);

		List<String> description = new ArrayList<String>();
		description.add(this.fluid.getLocalizedName(new FluidStack(this.fluid, 0)));
		description.add(amountToText);
		drawHoveringText(description, mouseX - this.guiFluidTerminal.guiLeft(),
				mouseY - this.guiFluidTerminal.guiTop() + 18,
				Minecraft.getMinecraft().fontRenderer);
		return true;
	}

	@Override
	public void drawWidget(int posX, int posY) {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Apply fluid color on top of fluid texture
		// Same behaviour as in GT5-Unofficial item rendering
		Color color = new Color(this.fluid.getColor());
		GL11.glColor3f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);

		if (this.fluid != null && this.fluid.getIcon() != null) {
			drawTexturedModelRectFromIcon(
					posX + 1,
					posY + 1,
					this.fluid.getIcon(),
					this.height - 2,
					this.width - 2
			);
		}

		// Reset color
		GL11.glColor3f(1.0F, 1.0F, 1.0F);

		IAEFluidStack currentFluidStack = ((IFluidSelectorGui)this.guiFluidTerminal).getCurrentFluid();
		Fluid currentFluid = currentFluidStack != null ? currentFluidStack.getFluid() : null;

		if (this.fluid == currentFluid) {
			drawHollowRectWithCorners(
					posX,
					posY,
					this.height,
					this.width,
					this.SELECTED_RECT_COLOR,
					this.SELECTED_BORDER_THICKNESS
			);
		}

		drawAmount(posX, posY);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawAmount(int posX, int posY) {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

		String amountString = FluidUtil.getAmountAsPrettyString(amount);

		float x = posX + this.width - 2 - (fontRenderer.getStringWidth(amountString) * FONT_SCALE);
		float y = posY + this.height - 2 - (fontRenderer.FONT_HEIGHT * FONT_SCALE);

		// Using hack to draw downscaled text.
		GL11.glPushMatrix();
		GL11.glScalef(FONT_SCALE, FONT_SCALE, FONT_SCALE);
		fontRenderer.drawStringWithShadow(
				amountString,
				(int)(x / FONT_SCALE),
				(int)(y / FONT_SCALE),
				0xFFFFFF
		);
		GL11.glPopMatrix();
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

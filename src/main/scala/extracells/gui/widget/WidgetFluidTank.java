package extracells.gui.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.util.AEPartLocation;
import extracells.Constants;

public class WidgetFluidTank extends AbstractWidget {

	public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(Constants.MOD_ID, "textures/gui/fluidtank.png");

	IFluidTank tank;
	AEPartLocation direction;

	public WidgetFluidTank(WidgetManager widgetManager, IFluidTank tank, int posX, int yPos) {
		this(widgetManager, tank, posX, yPos, AEPartLocation.INTERNAL);
	}

	public WidgetFluidTank(WidgetManager widgetManager, IFluidTank tank, int posX, int yPos, AEPartLocation location) {
		super(widgetManager, posX, yPos);
		width = 18;
		height = 73;
		this.tank = tank;
		this.direction = location;
	}

	public void draw(int mouseX, int mouseY) {
		if (this.tank == null) {
			return;
		}
		TextureManager textureManager = manager.mc.getTextureManager();

		GlStateManager.disableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F);

		textureManager.bindTexture(TEXTURE_FILE);
		manager.gui.drawTexturedModalRect(xPos, yPos, 0, 0, 18, 73);

		int iconHeightRemainder = (73 - 4) % 16;

		FluidStack fluid = this.tank.getFluid();
		if (fluid != null && fluid.amount > 0) {
			textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill().toString());

			if (iconHeightRemainder > 0) {
				manager.gui.drawTexturedModalRect(this.xPos + 1, this.yPos + 2, sprite, 16, iconHeightRemainder);
			}
			for (int i = 0; i < (73 - 6) / 16; i++) {
				manager.gui.drawTexturedModalRect(this.xPos + 1, this.yPos + 2 + i * 16 + iconHeightRemainder, sprite, 16, 16);
			}

			textureManager.bindTexture(TEXTURE_FILE);
			manager.gui.drawTexturedModalRect(this.xPos + 2, this.yPos + 1, 1, 1, 15, 72 - (int) (73 * ((float) fluid.amount / this.tank.getCapacity())));
		}

		textureManager.bindTexture(TEXTURE_FILE);
		manager.gui.drawTexturedModalRect(this.xPos + 1, this.yPos + 1, 19, 1, 16, 73);

		GlStateManager.enableLighting();
	}

	@Override
	public List<String> getToolTip(int mouseX, int mouseY) {
		List<String> description = new ArrayList<String>();
		if (direction != AEPartLocation.INTERNAL) {
			description.add(I18n.translateToLocal("extracells.tooltip.direction." + this.direction.ordinal()));
		}

		if (this.tank == null || this.tank.getFluid() == null) {
			description.add(I18n.translateToLocal("extracells.tooltip.empty1"));
		} else {
			if (this.tank.getFluid().amount > 0
				&& this.tank.getFluid().getFluid() != null) {
				String amountToText = this.tank.getFluid().amount + "mB";

				description.add(this.tank.getFluid().getFluid().getLocalizedName(this.tank.getFluid()));
				description.add(amountToText);
			}
		}
		return description;
	}
}

package extracells.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import extracells.container.ContainerVibrationChamberFluid;
import extracells.gui.widget.WidgetFluidTank;
import extracells.tileentity.TileEntityVibrationChamberFluid;

public class GuiVibrationChamberFluid extends GuiBase<ContainerVibrationChamberFluid> {

	public WidgetFluidTank widgetFluidTank;
	private TileEntityVibrationChamberFluid tileEntity;

	public GuiVibrationChamberFluid(EntityPlayer player, TileEntityVibrationChamberFluid tileEntity) {
		super(new ResourceLocation("extracells", "textures/gui/vibrationchamberfluid.png"), new ContainerVibrationChamberFluid(player.inventory, tileEntity));
		this.tileEntity = tileEntity;
		widgetManager.add(new WidgetFluidTank(widgetManager, this.tileEntity.getTank(), 79, 6));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawBackground() {
		super.drawBackground();
		//int burnTime = tileEntity.getBurntTimeScaled(52);
		//drawTexturedModalRect(posX + 105, posY + 17 + 54 - burnTime, 176, 0 + 54 - burnTime, 3, burnTime);
	}
}

package extracells.gui.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import extracells.container.fluid.ContainerFluidFiller;
import extracells.gui.GuiBase;
import extracells.gui.widget.WidgetSlotFluidContainer;
import extracells.tileentity.TileEntityFluidFiller;

public class GuiFluidFiller extends GuiBase<ContainerFluidFiller> {
	public static final int xSize = 176;
	public static final int ySize = 166;

	public GuiFluidFiller(EntityPlayer player, TileEntityFluidFiller tileentity) {
		super(new ResourceLocation("extracells", "textures/gui/fluidfiller.png"), new ContainerFluidFiller(player.inventory, tileentity));
		widgetManager.add(new WidgetSlotFluidContainer(tileentity, widgetManager, 80, 35));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.translateToLocal("tile.extracells.block.fluidfiller.name").replace("ME ", ""), 5, 5, 0x000000);
	}
}

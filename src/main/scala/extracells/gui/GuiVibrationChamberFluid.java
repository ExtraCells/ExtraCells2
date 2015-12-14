package extracells.gui;

import extracells.container.ContainerVibrationChamberFluid;
import extracells.gui.widget.WidgetFluidTank;
import extracells.tileentity.TileEntityVibrationChamberFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiVibrationChamberFluid extends GuiContainer {


    private ResourceLocation guiTexture = new ResourceLocation("extracells",
            "textures/gui/vibrationchamberfluid.png");
    private EntityPlayer player;
    public WidgetFluidTank widgetFluidTank;
    private TileEntityVibrationChamberFluid tileEntity;

    public GuiVibrationChamberFluid(EntityPlayer player, TileEntityVibrationChamberFluid tileentity) {
        super(new ContainerVibrationChamberFluid(player.inventory, tileentity));
        this.player = player;
        this.tileEntity = tileentity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if(widgetFluidTank != null)
        widgetFluidTank.draw(widgetFluidTank.posX, widgetFluidTank.posY, mouseX, mouseY);
        if (widgetFluidTank != null)
            if (func_146978_c(widgetFluidTank.posX, widgetFluidTank.posY, 18, 73, mouseX, mouseY)) {
                widgetFluidTank.drawTooltip(mouseX - this.guiLeft, mouseY
                        - this.guiTop);
            }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);

        int posX = (this.width - xSize) / 2;
        int posY = (this.height - ySize) / 2;
        drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
        //int burnTime = tileEntity.getBurntTimeScaled(52);
        //drawTexturedModalRect(posX + 105, posY + 17 + 54 - burnTime, 176, 0 + 54 - burnTime, 3, burnTime);
    }



    @Override
    public void initGui() {
        super.initGui();
        widgetFluidTank = new WidgetFluidTank(this.tileEntity.getTank(), 79, 6);
    }
}

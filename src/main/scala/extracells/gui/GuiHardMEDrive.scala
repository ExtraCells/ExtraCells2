package extracells.gui

import extracells.container.ContainerHardMEDrive
import extracells.registries.BlockEnum
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiHardMEDrive(inventory: InventoryPlayer, tile: TileEntityHardMeDrive)
  extends GuiContainer(new ContainerHardMEDrive(inventory, tile)){


  xSize = 176
  ySize = 166
  private val guiTexture = new ResourceLocation("extracells", "textures/gui/hardmedrive.png")

  override def drawGuiContainerBackgroundLayer(f : Float, i : Int, j : Int) = {
    drawDefaultBackground();
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
    val posX = (width - xSize) / 2;
    val posY = (height - ySize) / 2;
    drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
  }

  override  def drawGuiContainerForegroundLayer(i: Int, j: Int) =
    fontRendererObj.drawString(BlockEnum.BLASTRESISTANTMEDRIVE.getStatName, 5, 5, 0x000000)

}

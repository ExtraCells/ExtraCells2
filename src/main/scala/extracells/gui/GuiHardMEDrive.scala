package extracells.gui

import extracells.container.ContainerHardMEDrive
import extracells.registries.BlockEnum
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Slot
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
    import scala.collection.JavaConversions._
    for (s <- this.inventorySlots.inventorySlots) {
      renderBackground(s.asInstanceOf[Slot])
    }
  }

  override  def drawGuiContainerForegroundLayer(i: Int, j: Int) =
    fontRendererObj.drawString(BlockEnum.BLASTRESISTANTMEDRIVE.getStatName, 5, 5, 0x000000)

  private def renderBackground(slot: Slot) {
    if (slot.getStack == null && slot.slotNumber < 3) {
      GL11.glDisable(GL11.GL_LIGHTING)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F)
      this.mc.getTextureManager.bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"))
      this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, 240, 0, 16, 16)
      GL11.glDisable(GL11.GL_BLEND)
      GL11.glEnable(GL11.GL_LIGHTING)
    }
  }

}

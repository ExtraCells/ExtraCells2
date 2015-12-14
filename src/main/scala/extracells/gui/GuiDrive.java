package extracells.gui;

import extracells.container.ContainerDrive;
import extracells.network.packet.part.PacketFluidStorage;
import extracells.part.PartDrive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiDrive extends GuiContainer {

	private EntityPlayer player;
	private ResourceLocation guiTexture = new ResourceLocation("extracells",
			"textures/gui/drive.png");

	public GuiDrive(PartDrive _part, EntityPlayer _player) {
		super(new ContainerDrive(_part, _player));
		this.player = _player;
		this.xSize = 176;
		this.ySize = 163;
		new PacketFluidStorage(this.player).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX,
			int sizeY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop - 18, 0, 0, this.xSize,
				this.ySize);
		for (Object s : this.inventorySlots.inventorySlots) {
			renderBackground((Slot) s);
		}
	}

	private void renderBackground(Slot slot) {
		if (slot.getStack() == null && slot.slotNumber < 6) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(
					new ResourceLocation("appliedenergistics2",
							"textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition,
					this.guiTop + slot.yDisplayPosition, 240, 0, 16, 16);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);

		}
	}
}

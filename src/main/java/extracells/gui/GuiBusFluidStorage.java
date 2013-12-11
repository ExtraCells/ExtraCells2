package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.BlockEnum;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.widget.DigitTextField;
import extracells.network.packet.PacketBusFluidStorage;
import extracells.tile.TileEntityBusFluidStorage;

@SideOnly(Side.CLIENT)
public class GuiBusFluidStorage extends GuiContainer
{

	public static final int xSize = 176;
	public static final int ySize = 222;
	World world;
	TileEntityBusFluidStorage tileentity;
	Boolean editMode = false;
	DigitTextField textFieldPriority;

	public GuiBusFluidStorage(World world, IInventory inventory, TileEntityBusFluidStorage tileentity)
	{
		super(new ContainerBusFluidStorage(inventory, tileentity.getInventory()));
		this.world = world;
		this.tileentity = tileentity;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/storagebusfluid.png"));
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);

		textFieldPriority.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		this.fontRenderer.drawString(BlockEnum.FLUIDSTORAGE.getStatName(), 5, -23, 0x000000);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		textFieldPriority = new DigitTextField(fontRenderer, guiLeft + 60, guiTop + 99, 57, 10);
		textFieldPriority.setText(Integer.toString(tileentity.getPriority()));
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton)
	{
		super.mouseClicked(x, y, mouseButton);
		textFieldPriority.mouseClicked(x, y, mouseButton);
	}

	@Override
	protected void keyTyped(char key, int par2)
	{
		if (textFieldPriority.isFocused())
		{
			textFieldPriority.textboxKeyTyped(key, par2);
			if (!textFieldPriority.getText().isEmpty())
			{
				try
				{
					int priority = Integer.valueOf(textFieldPriority.getText());
					PacketDispatcher.sendPacketToServer(new PacketBusFluidStorage(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, priority).makePacket());
				} catch (NumberFormatException e)
				{
				}

			}
		} else
		{
			super.keyTyped(key, par2);
		}
	}
}

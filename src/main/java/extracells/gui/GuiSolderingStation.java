package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import appeng.api.Util;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.network.packet.PacketSolderingStation;

@SideOnly(Side.CLIENT)
public class GuiSolderingStation extends GuiScreen
{

	private int tileX, tileY, tileZ;

	public final int xSize = 176;
	public final int ySize = 88;
	private GuiTextField textfield_size;
	private GuiTextField textfield_types;
	private boolean rightItem;
	EntityPlayer player;

	public GuiSolderingStation(EntityPlayer player, int x, int y, int z, boolean rightItem)
	{
		super();
		this.player = player;
		this.tileX = x;
		this.tileY = y;
		this.tileZ = z;
		this.rightItem = rightItem;
	}

	@Override
	public void drawScreen(int x, int y, float f)
	{
		drawDefaultBackground();
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/solderingstation.png"));

		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);

		if (rightItem)
		{
			textfield_size.drawTextBox();
			textfield_types.drawTextBox();
		} else
		{
			this.fontRenderer.drawSplitString(StatCollector.translateToLocal("tooltip.solderingwarning.tutorial"), posX + 3, posY + 25, 170, 0x000064);
		}
		super.drawScreen(x, y, f);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		if (rightItem)
		{
			this.buttonList.clear();
			this.buttonList.add(new GuiButton(0, posX + 5, posY + 17, 40, 20, "- 2048"));
			this.buttonList.add(new GuiButton(1, posX + 130, posY + 17, 40, 20, "+ 2048"));
			this.buttonList.add(new GuiButton(2, posX + 5, posY + 47, 40, 20, "- 1"));
			this.buttonList.add(new GuiButton(3, posX + 130, posY + 47, 40, 20, "+ 1"));

			textfield_size = new GuiTextField(fontRenderer, posX + 40, posY + 20, 90, 15);
			textfield_size.setFocused(false);
			textfield_size.setMaxStringLength(12);

			textfield_types = new GuiTextField(fontRenderer, posX + 40, posY + 50, 90, 15);
			textfield_types.setFocused(false);
			textfield_types.setMaxStringLength(2);
		}
	}

	@Override
	public void updateScreen()
	{
		ItemStack itemstack = player.inventory.getCurrentItem();
		if (itemstack != null && itemstack.hasTagCompound())
		{
			textfield_size.setText(Integer.toString(itemstack.getTagCompound().getInteger("custom_size")));
			textfield_types.setText(Integer.toString(itemstack.getTagCompound().getInteger("custom_types")));
		}
	}

	public void keyTyped(char keyChar, int keyID)
	{
		super.keyTyped(keyChar, keyID);
		if (keyID == Keyboard.KEY_ESCAPE || keyID == mc.gameSettings.keyBindInventory.keyCode)
		{
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	public void actionPerformed(GuiButton button)
	{
		int slotID = mc.thePlayer.inventory.currentItem;
		switch (button.id)
		{
		case 0:
			// -2048
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				PacketDispatcher.sendPacketToServer(PacketSolderingStation.changeSize(mc.thePlayer, tileX, tileY, tileZ, -2048, slotID).makePacket());
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 1:
			// +2048
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				PacketDispatcher.sendPacketToServer(PacketSolderingStation.changeSize(mc.thePlayer, tileX, tileY, tileZ, 2048, slotID).makePacket());
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 2:
			// -1
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				PacketDispatcher.sendPacketToServer(PacketSolderingStation.changeTypes(mc.thePlayer, tileX, tileY, tileZ, -1, slotID).makePacket());
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 3:
			// +1
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				PacketDispatcher.sendPacketToServer(PacketSolderingStation.changeTypes(mc.thePlayer, tileX, tileY, tileZ, 1, slotID).makePacket());
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		default:
			break;
		}
	}
}
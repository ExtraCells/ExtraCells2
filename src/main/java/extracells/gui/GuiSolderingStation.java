package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import appeng.api.Util;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.ItemEnum;
import extracells.network.packet.PacketSolderingStation;

@SideOnly(Side.CLIENT)
public class GuiSolderingStation extends GuiScreen
{

	private int tileX, tileY, tileZ;

	public final int xSize = 176;
	public final int ySize = 88;
	private GuiTextField textfield_size;
	private GuiTextField textfield_types;
	private int int_size;
	private int int_types;
	private boolean rightItem;

	public GuiSolderingStation(int x, int y, int z, boolean rightItem)
	{
		super();
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

			ItemStack itemstack = this.mc.thePlayer.getHeldItem();

			textfield_size.setText(Integer.toString(itemstack.getTagCompound().getInteger("costum_size")));
			textfield_types.setText(Integer.toString(itemstack.getTagCompound().getInteger("costum_types")));

			int_size = itemstack.getTagCompound().getInteger("costum_size");
			int_types = itemstack.getTagCompound().getInteger("costum_types");
		}

	}

	public void updateScreen()
	{
		if (this.mc.thePlayer.getHeldItem() != null)
		{
			if (this.mc.thePlayer.getHeldItem().getItem() == ItemEnum.STORAGEPHYSICAL.getItemEntry() && this.mc.thePlayer.getHeldItem().getItemDamage() == 5)
			{
				if (this.mc.thePlayer.getHeldItem().hasTagCompound())
				{
					PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, '\0', '\0').makePacket());
					ItemStack itemstack = this.mc.thePlayer.getHeldItem();
					itemstack.getTagCompound().setInteger("costum_size", int_size);
					itemstack.getTagCompound().setInteger("costum_types", int_types);

					if (textfield_size.getText() != Integer.toString(itemstack.getTagCompound().getInteger("costum_size")) || textfield_types.getText() != Integer.toString(itemstack.getTagCompound().getInteger("costum_types")))
					{
						PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, '\0', '\0').makePacket());
						textfield_size.setText(Integer.toString(int_size));
						textfield_types.setText(Integer.toString(int_types));
					}
				}
			}
		}
	}

	public void keyTyped(char keyChar, int keyID)
	{
		super.keyTyped(keyChar, keyID);
		if (keyID == Keyboard.KEY_ESCAPE || keyID == mc.gameSettings.keyBindInventory.keyCode)
		{
			PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, true, '\0', '\0').makePacket());
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	public void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case 0:
			// -64
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				if (!((int_size - 2048) < 4096))
				{
					if (isSpaceInInventory(appeng.api.Materials.matStorageCell.copy()))
					{
						int_size = int_size - 2048;
						PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, '\0', 's').makePacket());
					} else
					{
						this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.nospacestoragecell"));
					}
				}
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 1:
			// +64
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				if (doesInvContain(appeng.api.Materials.matStorageCell.copy()))
				{
					int_size = int_size + 2048;
					PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, 's', '\0').makePacket());
				} else
				{
					this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.needstoragecell"));
				}
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 2:
			// -1
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				if (!((int_types - 1) < 27))
				{
					if (isSpaceInInventory(appeng.api.Materials.matConversionMatrix.copy()))
					{
						int_types = int_types - 1;
						PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, '\0', 't').makePacket());
					} else
					{
						this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.nospaceconversionmatrix"));
					}
				}
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		case 3:
			// +1
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				if (!((int_types + 1) > 63))
				{
					if (doesInvContain(appeng.api.Materials.matConversionMatrix))
					{
						int_types = int_types + 1;
						PacketDispatcher.sendPacketToServer(new PacketSolderingStation(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false, 't', '\0').makePacket());
					} else
					{
						this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.needconversionmatrix"));
					}
				}
			} else
			{
				this.mc.thePlayer.addChatMessage(StatCollector.translateToLocal("tooltip.solderingwarning.cellnotempty"));
			}
			break;

		default:
			break;
		}
	}

	// Returns if inventory contains ItemStack itemstack
	public boolean doesInvContain(ItemStack itemstack)
	{
		for (int i = 0; i < this.mc.thePlayer.inventory.mainInventory.length; i++)
		{
			if (this.mc.thePlayer.inventory.mainInventory[i] != null)
			{
				if (this.mc.thePlayer.inventory.mainInventory[i].getItem() == itemstack.getItem() && this.mc.thePlayer.inventory.mainInventory[i].getItemDamage() == itemstack.getItemDamage())
				{
					return true;
				}
			}
		}
		return false;
	}

	// Returns if there is space for ItemStack itemstack in inventory
	public boolean isSpaceInInventory(ItemStack itemstack)
	{
		for (int i = 0; i < this.mc.thePlayer.inventory.mainInventory.length; i++)
		{
			if (this.mc.thePlayer.inventory.mainInventory[i] != null)
			{
				if (this.mc.thePlayer.inventory.mainInventory[i].getItem() == itemstack.getItem() && this.mc.thePlayer.inventory.mainInventory[i].getItemDamage() == itemstack.getItemDamage() && this.mc.thePlayer.inventory.mainInventory[i].stackSize <= 63)
				{
					return true;
				}
			} else
			{
				return true;
			}
		}
		return false;
	}
}
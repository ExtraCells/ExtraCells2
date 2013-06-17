package extracells.gui;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;

import appeng.api.Util;

import extracells.extracells;
import extracells.items.ItemCell;
import extracells.network.packet.SolderingPacket;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;

public class GUISolderingStation extends GuiScreen
{

	private int tileX, tileY, tileZ;

	public final int xSizeOfTexture = 176;
	public final int ySizeOfTexture = 88;
	private GuiTextField textfield_size;
	private GuiTextField textfield_types;
	private int int_size;
	private int int_types;
	private boolean rightItem;

	public GUISolderingStation(int x, int y, int z, boolean rightItem)
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
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture("/mods/extracells/textures/gui/guiSolderingStation.png");
		int posX = (this.width - xSizeOfTexture) / 2;
		int posY = (this.height - ySizeOfTexture) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSizeOfTexture, ySizeOfTexture);

		if (rightItem)
		{
			textfield_size.drawTextBox();
			textfield_types.drawTextBox();
		} else
		{
			this.drawCenteredString(fontRenderer, "Put a ME Adjustable Storage", posX + 90, posY + 25, 0xFF00FF);
			this.drawCenteredString(fontRenderer, "into your hotbar and", posX + 90, posY + 35, 0xFF00FF);
			this.drawCenteredString(fontRenderer, "hold it while", posX + 90, posY + 45, 0xFF00FF);
			this.drawCenteredString(fontRenderer, "using the SolderingStation!", posX + 90, posY + 55, 0xFF00FF);
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
		int posX = (this.width - xSizeOfTexture) / 2;
		int posY = (this.height - ySizeOfTexture) / 2;

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
			if (this.mc.thePlayer.getHeldItem().getItem() == extracells.Cell && this.mc.thePlayer.getHeldItem().getItemDamage() == 5)
			{
				if (this.mc.thePlayer.getHeldItem().hasTagCompound())
				{
					PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
					ItemStack itemstack = this.mc.thePlayer.getHeldItem();
					itemstack.getTagCompound().setInteger("costum_size", int_size);
					itemstack.getTagCompound().setInteger("costum_types", int_types);

					if (textfield_size.getText() != Integer.toString(itemstack.getTagCompound().getInteger("costum_size")) || textfield_types.getText() != Integer.toString(itemstack.getTagCompound().getInteger("costum_types")))
					{
						PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
						textfield_size.setText(Integer.toString(int_size));
						textfield_types.setText(Integer.toString(int_types));
					}
				}
			}
		}
	}

	public void keyTyped(char par1, int par2)
	{
		if (par1 == 'e' || par1 == '')
		{
			PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, true).makePacket());
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
						PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
						this.mc.thePlayer.inventory.addItemStackToInventory(new ItemStack(appeng.api.Materials.matStorageCell.copy().getItem(), 1, appeng.api.Materials.matStorageCell.getItemDamage()));
						int_size = int_size - 2048;
					} else
					{
						this.mc.thePlayer.sendChatToPlayer("Make place in your inventory to regain the ME Storage Cell!");
					}
				}
			} else
			{
				this.mc.thePlayer.sendChatToPlayer("This Cell is not Empty!");
			}
			break;

		case 1:
			// +64
			if (Util.getCellRegistry().getHandlerForCell(this.mc.thePlayer.getCurrentEquippedItem()).storedItemCount() == 0)
			{
				if (doesInvContain(appeng.api.Materials.matStorageCell.copy()))
				{
					PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
					decreaseStackInInv(appeng.api.Materials.matStorageCell.copy(), 1);
					int_size = int_size + 2048;
				} else
				{
					this.mc.thePlayer.sendChatToPlayer("To upgrade the size of your storage, you need one ME Storage Cell for each 2048 KBytes! Thats double the size per cell you'd get with 1K Storages :D");
				}
			} else
			{
				this.mc.thePlayer.sendChatToPlayer("This Cell is not Empty!");
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
						PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
						this.mc.thePlayer.inventory.addItemStackToInventory(new ItemStack(appeng.api.Materials.matConversionMatrix.copy().getItem(), 1, appeng.api.Materials.matConversionMatrix.getItemDamage()));
						int_types = int_types - 1;
					} else
					{
						this.mc.thePlayer.sendChatToPlayer("To upgrade the number of types of your storage, you need one ME Conversion Matrices for each type!");
					}
				}
			} else
			{
				this.mc.thePlayer.sendChatToPlayer("This Cell is not Empty!");
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
						PacketDispatcher.sendPacketToServer(new SolderingPacket(mc.thePlayer.username, tileX, tileY, tileZ, int_size, int_types, false).makePacket());
						decreaseStackInInv(appeng.api.Materials.matConversionMatrix.copy(), 1);
						int_types = int_types + 1;
					} else
					{
						this.mc.thePlayer.sendChatToPlayer("To upgrade the number of types of your Storage, you need one ME Conversion Matrices for each type!");
					}
				}
			} else
			{
				this.mc.thePlayer.sendChatToPlayer("This Cell is not Empty!");
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

	// Find ItemStack in inventory are remove int number items from it
	public void decreaseStackInInv(ItemStack itemstack, int number)
	{
		int left = number;
		for (int i = 0; i < this.mc.thePlayer.inventory.mainInventory.length; i++)
		{
			if (this.mc.thePlayer.inventory.mainInventory[i] != null)
			{
				if (this.mc.thePlayer.inventory.mainInventory[i].getItem() == itemstack.getItem() && this.mc.thePlayer.inventory.mainInventory[i].getItemDamage() == itemstack.getItemDamage())
				{
					if (left != 0)
					{
						if (this.mc.thePlayer.inventory.mainInventory[i].stackSize == 1)
						{
							this.mc.thePlayer.inventory.mainInventory[i] = null;
							left = left - 1;
						} else
						{
							this.mc.thePlayer.inventory.mainInventory[i] = new ItemStack(this.mc.thePlayer.inventory.mainInventory[i].getItem(), this.mc.thePlayer.inventory.mainInventory[i].stackSize - 1, this.mc.thePlayer.inventory.mainInventory[i].getItemDamage());
							left = left - 1;
						}
					}
				}
			}
		}
	}
}
package extracells.gui;

import appeng.api.config.RedstoneMode;
import extracells.container.ContainerFluidEmitter;
import extracells.gui.widget.DigitTextField;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.part.PartFluidLevelEmitter;
import extracells.registries.PartEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiFluidEmitter extends GuiContainer
{

	public static final int xSize = 176;
	public static final int ySize = 166;
	private DigitTextField amountField;
	private PartFluidLevelEmitter part;
	private EntityPlayer player;
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/levelemitterfluid.png");

	public GuiFluidEmitter(PartFluidLevelEmitter _part, EntityPlayer _player)
	{
		super(new ContainerFluidEmitter(_part, _player));
		player = _player;
		part = _part;
		new PacketFluidEmitter(false, part, player).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void keyTyped(char key, int keyID)
	{
		super.keyTyped(key, keyID);
		if ("0123456789".contains(String.valueOf(key)) || keyID == Keyboard.KEY_BACK)
		{
			amountField.textboxKeyTyped(key, keyID);
			new PacketFluidEmitter(amountField.getText(), part, player).sendPacketToServer();
		}
	}

	public void drawScreen(int x, int y, float f)
	{
		drawDefaultBackground();

		String[] buttonNames =
		{ "-1", "-10", "-100", "+1", "+10", "+100" };
		String[] shiftNames =
		{ "-100", "-1000", "-10000", "+100", "+1000", "+10000" };

		for (int i = 0; i < buttonList.size(); i++)
		{
			if (i == 6)
				break;
			GuiButton currentButton = (GuiButton) buttonList.get(i);

			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				currentButton.displayString = shiftNames[i] + "mB";
			} else
			{
				currentButton.displayString = buttonNames[i] + "mB";
			}
		}

		super.drawScreen(x, y, f);
		amountField.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j)
	{
		fontRendererObj.drawString(PartEnum.FLUIDLEVELEMITTER.getStatName(), 5, 5, 0x000000);
	}

	@SuppressWarnings("unchecked")
	public void initGui()
	{
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		amountField = new DigitTextField(fontRendererObj, posX + 10, posY + 40, 59, 10);
		amountField.setFocused(true);
		amountField.setEnableBackgroundDrawing(false);
		amountField.setTextColor(0xFFFFFF);

		buttonList.clear();
		buttonList.add(new GuiButton(0, posX + 65 - 46, posY + 8 + 6, 42, 20, "-1"));
		buttonList.add(new GuiButton(1, posX + 115 - 46, posY + 8 + 6, 42, 20, "-10"));
		buttonList.add(new GuiButton(2, posX + 165 - 46, posY + 8 + 6, 42, 20, "-100"));
		buttonList.add(new GuiButton(3, posX + 65 - 46, posY + 58 - 2, 42, 20, "+1"));
		buttonList.add(new GuiButton(4, posX + 115 - 46, posY + 58 - 2, 42, 20, "+10"));
		buttonList.add(new GuiButton(5, posX + 165 - 46, posY + 58 - 2, 42, 20, "+100"));
		buttonList.add(new WidgetRedstoneModes(6, posX + 120, posY + 36, 16, 16, RedstoneMode.LOW_SIGNAL, true));

		super.initGui();
	}

	public void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case 0:
			modifyAmount(-1);
			break;
		case 1:
			modifyAmount(-10);
			break;
		case 2:
			modifyAmount(-100);
			break;
		case 3:
			modifyAmount(+1);
			break;
		case 4:
			modifyAmount(+10);
			break;
		case 5:
			modifyAmount(+100);
			break;
		case 6:
			new PacketFluidEmitter(true, part, player).sendPacketToServer();
			break;

		}
	}

	private void modifyAmount(int amount)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			amount *= 100;
		new PacketFluidEmitter(amount, part, player).sendPacketToServer();
	}

	public void setAmountField(long amount)
	{
		amountField.setText(Long.toString(amount));
	}

	public void setRedstoneMode(RedstoneMode mode)
	{
		((WidgetRedstoneModes) buttonList.get(6)).setRedstoneMode(mode);
	}
}

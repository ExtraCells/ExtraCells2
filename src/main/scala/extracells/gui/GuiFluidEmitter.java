package extracells.gui;

import appeng.api.config.RedstoneMode;
import extracells.container.ContainerFluidEmitter;
import extracells.gui.widget.DigitTextField;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.integration.Integration;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.part.PartFluidLevelEmitter;
import extracells.part.PartGasLevelEmitter;
import extracells.registries.PartEnum;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiFluidEmitter extends ECGuiContainer implements IFluidSlotGui {

	public static final int xSize = 176;
	public static final int ySize = 166;
	private DigitTextField amountField;
	private PartFluidLevelEmitter part;
	private EntityPlayer player;
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/levelemitterfluid.png");

	public GuiFluidEmitter(PartFluidLevelEmitter _part, EntityPlayer _player) {
		super(new ContainerFluidEmitter(_part, _player));
		this.player = _player;
		this.part = _part;
		this.fluidSlot = new WidgetFluidSlot(this.player, this.part, 79, 36);
		new PacketFluidEmitter(false, this.part, this.player).sendPacketToServer();
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
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
			new PacketFluidEmitter(true, this.part, this.player).sendPacketToServer();
			break;

		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(PartEnum.FLUIDLEVELEMITTER.getStatName(), 5, 5, 0x000000);
		this.fluidSlot.drawWidget();
		((WidgetRedstoneModes) this.buttonList.get(6)).drawTooltip(mouseX, mouseY, (this.width - xSize) / 2, (this.height - ySize) / 2);
		GuiUtil.renderOverlay(this.zLevel, this.guiLeft, this.guiTop, this.fluidSlot, mouseX, mouseY);
		showTooltip(mouseX, mouseY);
	}

	@Override
	public void drawScreen(int x, int y, float f) {

		String[] buttonNames = { "-1", "-10", "-100", "+1", "+10", "+100" };
		String[] shiftNames = { "-100", "-1000", "-10000", "+100", "+1000", "+10000" };

		for (int i = 0; i < this.buttonList.size(); i++) {
			if (i == 6)
				break;
			GuiButton currentButton = (GuiButton) this.buttonList.get(i);

			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				currentButton.displayString = shiftNames[i] + "mB";
			} else {
				currentButton.displayString = buttonNames[i] + "mB";
			}
		}

		super.drawScreen(x, y, f);
		this.amountField.drawTextBox();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		this.amountField = new DigitTextField(this.fontRendererObj, posX + 10, posY + 40, 59, 10);
		this.amountField.setFocused(true);
		this.amountField.setEnableBackgroundDrawing(false);
		this.amountField.setTextColor(0xFFFFFF);

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, posX + 65 - 46, posY + 8 + 6, 42, 20, "-1"));
		this.buttonList.add(new GuiButton(1, posX + 115 - 46, posY + 8 + 6, 42, 20, "-10"));
		this.buttonList.add(new GuiButton(2, posX + 165 - 46, posY + 8 + 6, 42, 20, "-100"));
		this.buttonList.add(new GuiButton(3, posX + 65 - 46, posY + 58 - 2, 42, 20, "+1"));
		this.buttonList.add(new GuiButton(4, posX + 115 - 46, posY + 58 - 2, 42, 20, "+10"));
		this.buttonList.add(new GuiButton(5, posX + 165 - 46, posY + 58 - 2, 42, 20, "+100"));
		this.buttonList.add(new WidgetRedstoneModes(6, posX + 120, posY + 36, 16, 16, RedstoneMode.LOW_SIGNAL, true));

		super.initGui();
	}

	@Override
	protected void keyTyped(char key, int keyID) {
		super.keyTyped(key, keyID);
		if ("0123456789".contains(String.valueOf(key)) || keyID == Keyboard.KEY_BACK) {
			this.amountField.textboxKeyTyped(key, keyID);
			new PacketFluidEmitter(this.amountField.getText(), this.part, this.player).sendPacketToServer();
		}
	}

	private void modifyAmount(int amount) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			amount *= 100;
		new PacketFluidEmitter(amount, this.part, this.player).sendPacketToServer();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		if (GuiUtil.isPointInRegion(this.guiLeft, this.guiTop, this.fluidSlot.getPosX(), this.fluidSlot.getPosY(), 18, 18, mouseX, mouseY)){
			if(part instanceof PartGasLevelEmitter && Integration.Mods.MEKANISMGAS.isEnabled())
				this.fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
			else
				this.fluidSlot.mouseClicked(this.player.inventory.getItemStack());
		}


	}

	public void setAmountField(long amount) {
		this.amountField.setText(Long.toString(amount));
	}

	public void setRedstoneMode(RedstoneMode mode) {
		((WidgetRedstoneModes) this.buttonList.get(6)).setRedstoneMode(mode);
	}

	@Override
	public void updateFluids(List<Fluid> _fluids) {
		if (_fluids == null || _fluids.isEmpty()) {
			this.fluidSlot.setFluid(null);
			return;
		}
		this.fluidSlot.setFluid(_fluids.get(0));
	}
}

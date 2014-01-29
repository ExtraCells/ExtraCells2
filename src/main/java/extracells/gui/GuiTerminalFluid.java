package extracells.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.BlockEnum;
import extracells.Extracells;
import extracells.container.ContainerTerminalFluid;
import extracells.gui.widget.AbstractFluidWidget;
import extracells.gui.widget.FluidWidgetComparator;
import extracells.gui.widget.WidgetFluidRequest;
import extracells.gui.widget.WidgetFluidSelector;
import extracells.tileentity.TileEntityTerminalFluid;
import extracells.util.SpecialFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiTerminalFluid extends GuiContainer
{
	public static final int xSize = 175;
	public static final int ySize = 203;
	public String currentFluidName;
	public long currentFluidAmount;
	private int currentScroll = 0;
	public TileEntityTerminalFluid tileEntity;
	private GuiTextField searchbar;
	private Fluid oldSelected;
	private List<Fluid> oldCraftables;
	private List<SpecialFluidStack> oldFluids;
	private List<AbstractFluidWidget> fluidWidgets = new ArrayList<AbstractFluidWidget>();
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/terminalfluid.png");

	public GuiTerminalFluid(TileEntityTerminalFluid _tileEntity, EntityPlayer player)
	{
		super(new ContainerTerminalFluid(player, _tileEntity.getInventory()));
		if (_tileEntity != null)
		{
			tileEntity = _tileEntity;
			oldSelected = _tileEntity.getCurrentFluid();
			oldFluids = _tileEntity.getFluids();
			oldCraftables = _tileEntity.getCurrentCraftables();
			currentFluidName = _tileEntity.getCurrentFluid() != null ? _tileEntity.getCurrentFluid().getLocalizedName() : "-";
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		fluidWidgets = new ArrayList<AbstractFluidWidget>();
		Mouse.getDWheel();

		List<Fluid> selectorFluids = new ArrayList<Fluid>();
		for (SpecialFluidStack stack : oldFluids)
		{
			fluidWidgets.add(new WidgetFluidSelector(this, stack));
			selectorFluids.add(stack.getFluidStack().getFluid());
		}
		for (Fluid fluid : oldCraftables)
			if (!selectorFluids.contains(fluid))
				fluidWidgets.add(new WidgetFluidRequest(this, fluid));

		for (AbstractFluidWidget widget : fluidWidgets)
		{
			if (widget instanceof WidgetFluidSelector && widget.getFluid() == oldSelected)
			{
				WidgetFluidSelector selector = (WidgetFluidSelector) widget;
				selector.setSelected(true);
				updateSelected(selector);
			}
		}
		Collections.sort(fluidWidgets, new FluidWidgetComparator());
		searchbar = new GuiTextField(fontRenderer, guiLeft + 81, guiTop - 12, 88, 10)
		{
			private int xPos = 0;
			private int yPos = 0;
			private int width = 0;
			private int height = 0;

			public void mouseClicked(int x, int y, int mouseBtn)
			{
				boolean flag = x >= xPos && x < xPos + width && y >= yPos && y < yPos + height;
				if (flag && mouseBtn == 3)
					setText("");
			}
		};
		searchbar.setEnableBackgroundDrawing(false);
		searchbar.setFocused(true);
		searchbar.setMaxStringLength(15);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		if (tileEntity != null && !tileEntity.getFluids().isEmpty())
		{
			Fluid currentSelected = tileEntity.getCurrentFluid();
			List<SpecialFluidStack> currentFluids = tileEntity.getFluids();
			List<Fluid> currentCraftables = tileEntity.getCurrentCraftables();
			if (oldSelected != currentSelected || oldFluids != currentFluids || oldCraftables != currentCraftables)
			{
				oldSelected = currentSelected;
				oldFluids = currentFluids;
				oldCraftables = currentCraftables;
				initGui();
			}
		} else
		{
			oldFluids = new ArrayList<SpecialFluidStack>();
			oldCraftables = new ArrayList<Fluid>();
			oldSelected = null;
			currentFluidName = "-";
			currentFluidAmount = 0;
			initGui();
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(guiLeft, guiTop - 18, 0, 0, xSize, ySize);
		searchbar.drawTextBox();
	}

	public void updateSelected(WidgetFluidSelector selector)
	{
		currentFluidName = selector.getFluid().getLocalizedName();
		currentFluidAmount = selector.getAmount();
		for (AbstractFluidWidget currentSelector : fluidWidgets)
			if (currentSelector instanceof WidgetFluidSelector)
				((WidgetFluidSelector) currentSelector).setSelected(selector == currentSelector);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRenderer.drawString(BlockEnum.FLUIDTERMINAL.getStatName().replace("ME ", ""), 5, -12, 0x000000);

		drawWidgets(mouseX, mouseY);

		String amountToText = Long.toString(currentFluidAmount) + "mB";
		if (Extracells.shortenedBuckets)
		{
			if (currentFluidAmount > 1000000000L)
				amountToText = Long.toString(currentFluidAmount / 1000000000L) + "MegaB";
			else if (currentFluidAmount > 1000000L)
				amountToText = Long.toString(currentFluidAmount / 1000000L) + "KiloB";
			else if (currentFluidAmount > 9999L)
			{
				amountToText = Long.toString(currentFluidAmount / 1000L) + "B";
			}
		}

		fontRenderer.drawString(StatCollector.translateToLocal("tooltip.amount") + ": " + amountToText, 45, 73, 0x000000);
		fontRenderer.drawString(StatCollector.translateToLocal("tooltip.fluid") + ": " + currentFluidName, 45, 83, 0x000000);
	}

	public void drawWidgets(int mouseX, int mouseY)
	{
		int listSize = fluidWidgets.size();
		if (tileEntity != null && !tileEntity.getFluids().isEmpty())
		{
			outerLoop: for (int y = 0; y < 4; y++)
			{
				for (int x = 0; x < 9; x++)
				{
					int widgetIndex = y * 9 + x + currentScroll * 9;
					if (0 <= widgetIndex && widgetIndex < listSize)
					{
						AbstractFluidWidget widget = fluidWidgets.get(widgetIndex);
						widget.drawWidget(x * 18 + 7, y * 18 - 1);
					} else
					{
						break outerLoop;
					}
				}
			}

			for (int x = 0; x < 9; x++)
			{
				for (int y = 0; y < 4; y++)
				{
					int widgetIndex = y * 9 + x;
					if (0 <= widgetIndex && widgetIndex < listSize)
					{
						fluidWidgets.get(widgetIndex).drawTooltip(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
					} else
					{
						break;
					}
				}
			}

			int deltaWheel = Mouse.getDWheel();
			if (deltaWheel > 0)
			{
				currentScroll++;
			} else if (deltaWheel < 0)
			{
				currentScroll--;
			}

			if (currentScroll < 0)
				currentScroll = 0;
			if (listSize / 9 < 4 && currentScroll < listSize / 9 + 4)
				currentScroll = 0;
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn)
	{
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		searchbar.mouseClicked(mouseX, mouseY, mouseBtn);
		int listSize = fluidWidgets.size();
		for (int x = 0; x < 9; x++)
		{
			for (int y = 0; y < 4; y++)
			{
				int index = y * 9 + x;
				if (0 <= index && index < listSize)
				{
					AbstractFluidWidget widget = fluidWidgets.get(index);
					widget.mouseClicked(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
				}
			}
		}
	}

	@Override
	protected void keyTyped(char key, int keyID)
	{
		if (keyID == Keyboard.KEY_ESCAPE)
			mc.thePlayer.closeScreen();
		searchbar.textboxKeyTyped(key, keyID);
	}

	public int guiLeft()
	{
		return guiLeft;
	}

	public int guiTop()
	{
		return guiTop;
	}
}

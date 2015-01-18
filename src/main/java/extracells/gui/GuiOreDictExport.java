package extracells.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import appeng.api.AEApi;
import extracells.api.IFluidInterface;
import extracells.container.ContainerFluidInterface;
import extracells.container.ContainerOreDictExport;
import extracells.gui.widget.WidgetFluidTank;
import extracells.gui.widget.fluid.AbstractFluidWidget;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.part.PacketOreDictExport;
import extracells.part.PartOreDictExporter;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiOreDictExport extends GuiContainer
{
	
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/oredictexport.png");
	private EntityPlayer player;
	private static String filter = "";
	private GuiTextField searchbar;

	public GuiOreDictExport(EntityPlayer player, PartOreDictExporter _part)
	{
		super(new ContainerOreDictExport(player, _part));
		this.player = player;
	}
	
	public static void updateFilter(String _filter){
		if(filter != null){
			filter = _filter;
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if(gui != null && gui instanceof  GuiOreDictExport){
				GuiOreDictExport g = (GuiOreDictExport) gui;
				if(g.searchbar != null)
					g.searchbar.setText(filter);
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		buttonList.add(new GuiButton(1, guiLeft + (xSize / 2) - 44, guiTop + 35, 88, 20, StatCollector.translateToLocal("extracells.tooltip.save")));
		searchbar = new GuiTextField(fontRendererObj, guiLeft + (xSize / 2) - 44, guiTop + 20, 88, 10) {

            private int xPos = 0;
            private int yPos = 0;
            private int width = 0;
            private int height = 0;

            public void mouseClicked(int x, int y, int mouseBtn) {
                boolean flag = x >= xPos && x < xPos + width && y >= yPos && y < yPos + height;
                if (flag && mouseBtn == 3)
                    setText("");
            }
        };
        searchbar.setEnableBackgroundDrawing(true);
        searchbar.setFocused(true);
        searchbar.setMaxStringLength(15);
        searchbar.setText(filter);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		searchbar.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRendererObj.drawString(StatCollector.translateToLocal("extracells.part.oredict.export.name").replace("ME ", ""), 8, 5, 0x000000);
		this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 94, 0x000000);
		
	}
	
	@Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
        super.mouseClicked(mouseX, mouseY, mouseBtn);
        searchbar.mouseClicked(mouseX, mouseY, mouseBtn);
    }
	
	@Override
    protected void keyTyped(char key, int keyID) {
        if (keyID == Keyboard.KEY_ESCAPE)
            mc.thePlayer.closeScreen();
        searchbar.textboxKeyTyped(key, keyID);
        filter = searchbar.getText();
    }
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
       new PacketOreDictExport(player, filter, Side.SERVER).sendPacketToServer();
	}
}
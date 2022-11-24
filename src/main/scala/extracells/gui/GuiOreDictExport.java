package extracells.gui;

import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import extracells.container.ContainerOreDictExport;
import extracells.network.packet.part.PacketOreDictExport;
import extracells.part.PartOreDictExporter;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiOreDictExport extends GuiContainer {

    public ContainerOreDictExport _containerOreDictExport;
    private int currentScroll = 0;

    public static void updateFilter(String _filter) {
        if (filter != null) {
            filter = _filter;
            Gui gui = Minecraft.getMinecraft().currentScreen;
            if (gui != null && gui instanceof GuiOreDictExport) {
                GuiOreDictExport g = (GuiOreDictExport) gui;
                if (g.searchbar != null) g.searchbar.setText(filter);
            }
        }
    }

    private final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/oredictexport.png");
    private final EntityPlayer player;
    private static String filter = "";

    private GuiTextField searchbar;

    public GuiOreDictExport(EntityPlayer player, PartOreDictExporter _part) {
        super(new ContainerOreDictExport(player, _part));
        this._containerOreDictExport = new ContainerOreDictExport(player, _part);
        this.player = player;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        new PacketOreDictExport(this.player, filter, Side.SERVER).sendPacketToServer();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int deltaWheel = Mouse.getEventDWheel();
        if (deltaWheel < 0) {
            currentScroll++;
        } else if (deltaWheel > 0) {
            currentScroll--;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchbar.drawTextBox();
    }

    public class RenderToolTip {
        private ItemStack item;
        private int x;
        private int y;
        private boolean render = false;

        public RenderToolTip() {}

        public void setValue(ItemStack _item, int _x, int _y) {
            this.item = _item;
            this.x = _x;
            this.y = _y;
            this.render = true;
        }

        public void renderToolTip() {
            if (render) {
                GuiOreDictExport.this.renderToolTip(this.item, this.x, this.y);
                this.render = false;
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("extracells.part.oredict.export.name")
                        .replace("ME ", ""),
                8,
                5,
                0x000000);
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"), 8, this.ySize - 94, 0x000000);
        if (!filter.isEmpty() && !filter.startsWith("*") && !filter.startsWith("^") && !filter.startsWith("$")) {
            this._containerOreDictExport.part.setFilter(filter, false);
            Iterator<AEItemStack> items =
                    this._containerOreDictExport.part.getOres().iterator();
            int size = this._containerOreDictExport.part.getOres().size();
            if (this.currentScroll < 0) this.currentScroll = 0;
            int maxPage = size / (4 * 10);
            if (this.currentScroll > maxPage) {
                this.currentScroll = maxPage;
            }
            for (int i = 0; i < 10 * 4 * currentScroll; i++) {
                if (items.hasNext()) {
                    items.next();
                } else {
                    break;
                }
            }
            RenderToolTip toolTip = new RenderToolTip();
            outerLoop:
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 4; x++) {
                    if (items.hasNext()) {
                        AEItemStack item = items.next();
                        item.getItem().addInformation(item.getItemStack(), player, item.getToolTip(), true);
                        int posX = -70 + x * 16;
                        int posY = y * 16;
                        itemRender.renderItemAndEffectIntoGUI(
                                this.fontRendererObj,
                                Minecraft.getMinecraft().renderEngine,
                                item.getItemStack(),
                                posX,
                                posY);
                        if (mouseX - this.guiLeft > posX
                                && mouseX - this.guiLeft < posX + 16
                                && mouseY - this.guiTop > posY
                                && mouseY - this.guiTop < posY + 16) {
                            toolTip.setValue(item.getItemStack(), posX, posY);
                        }
                    } else {
                        break outerLoop;
                    }
                }
            }
            toolTip.renderToolTip();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.buttonList.add(new GuiButton(
                1,
                this.guiLeft + this.xSize / 2 - 44,
                this.guiTop + 35,
                88,
                20,
                StatCollector.translateToLocal("extracells.tooltip.save")));
        this.searchbar =
                new GuiTextField(this.fontRendererObj, this.guiLeft + this.xSize / 2 - 75, this.guiTop + 20, 150, 10) {

                    private final int xPos = 0;
                    private final int yPos = 0;
                    private final int width = 0;
                    private final int height = 0;

                    @Override
                    public void mouseClicked(int x, int y, int mouseBtn) {
                        boolean flag = x >= this.xPos
                                && x < this.xPos + this.width
                                && y >= this.yPos
                                && y < this.yPos + this.height;
                        if (flag && mouseBtn == 3) setText("");
                    }
                };
        this.searchbar.setEnableBackgroundDrawing(true);
        this.searchbar.setFocused(true);
        this.searchbar.setMaxStringLength(128);
        this.searchbar.setText(filter);
    }

    @Override
    protected void keyTyped(char key, int keyID) {
        if (keyID == Keyboard.KEY_ESCAPE) this.mc.thePlayer.closeScreen();
        this.searchbar.textboxKeyTyped(key, keyID);
        filter = this.searchbar.getText();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
        super.mouseClicked(mouseX, mouseY, mouseBtn);
        this.searchbar.mouseClicked(mouseX, mouseY, mouseBtn);
    }
}

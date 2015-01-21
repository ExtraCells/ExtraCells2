package extracells.gui.widget;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import extracells.gui.widget.fluid.WidgetFluidSlot.IConfigurable;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class WidgetSlotFluidContainer extends Gui {

    private int posX, posY;
    private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
    private TileEntityFluidFiller fluidFiller;
    private EntityPlayer player;
    private IConfigurable configurable;
    private byte configOption;

    public WidgetSlotFluidContainer(EntityPlayer _player, TileEntityFluidFiller _fluidFiller, int _posX, int _posY) {
        player = _player;
        fluidFiller = _fluidFiller;
        posX = _posX;
        posY = _posY;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void drawWidget() {
    	ItemStack container = fluidFiller.containerItem;
    	GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        RenderItem itemRender = RenderItem.getInstance();
        itemRender.zLevel = 200.0F;
        FontRenderer font = null;
        if (container != null) font = container.getItem().getFontRenderer(container);
        if (font == null) font = Minecraft.getMinecraft().fontRenderer;
        itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), container, posX, posY);
        //itemRender.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), container, posX + 1, posY - 7, null);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    public void drawTooltip() {
        if (canRender()) {

        }
    }

    public boolean canRender() {
        return configurable == null || configurable.getConfigState() >= configOption;
    }

    public void mouseClicked(ItemStack stack) {
    	if(stack != null && stack.getItem() != null && FluidUtil.isEmpty(stack))
    		new PacketFluidContainerSlot(fluidFiller, stack, player).sendPacketToServer();
    }
}

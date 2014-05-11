package extracells.gui.widget.fluid;

import extracells.network.packet.other.IFluidSlotPart;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class WidgetFluidSlot extends Gui {

    private int id;
    private int posX, posY;
    private Fluid fluid;
    private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
    private IFluidSlotPart part;
    private EntityPlayer player;
    private IConfigurable configurable;
    private byte configOption;

    public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPart _part, int _id, int _posX, int _posY, IConfigurable _configurable, byte _configOption) {
        player = _player;
        part = _part;
        id = _id;
        posX = _posX;
        posY = _posY;
        configurable = _configurable;
        configOption = _configOption;
    }

    public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPart _part, int _id, int _posX, int _posY) {
        this(_player, _part, _id, _posX, _posY, null, (byte) 0);
    }

    public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPart _part, int _posX, int _posY) {
        this(_player, _part, 0, _posX, _posY, null, (byte) 0);
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setFluid(Fluid _fluid) {
        fluid = _fluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public void drawWidget() {
        if (!canRender())
            return;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(posX, posY, 79, 39, 18, 18);
        GL11.glEnable(GL11.GL_LIGHTING);

        if (fluid == null || fluid.getIcon() == null)
            return;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        drawTexturedModelRectFromIcon(posX + 1, posY + 1, fluid.getIcon(), 16, 16);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawTooltip() {
        if (canRender()) {

        }
    }

    public boolean canRender() {
        return configurable == null || configurable.getConfigState() >= configOption;
    }

    public void mouseClicked(ItemStack stack) {
        FluidStack fluidStack = FluidUtil.getFluidFromContainer(stack);
        fluid = fluidStack == null ? null : fluidStack.getFluid();
        new PacketFluidSlot(part, id, fluid, player).sendPacketToServer();
    }

    @SuppressWarnings("rawtypes")
    protected void drawHoveringText(List list, int x, int y, FontRenderer fontrenderer) {
        boolean lighting_enabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        if (!list.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;

            for (Object string : list) {
                String s = (String) string;
                int l = fontrenderer.getStringWidth(s);

                if (l > k) {
                    k = l;
                }
            }

            int i1 = x + 12;
            int j1 = y - 12;
            int k1 = 8;

            if (list.size() > 1) {
                k1 += 2 + (list.size() - 1) * 10;
            }

            this.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < list.size(); ++k2) {
                String s1 = (String) list.get(k2);
                fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0) {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            if (lighting_enabled)
                GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    public interface IConfigurable {

        public byte getConfigState();
    }
}

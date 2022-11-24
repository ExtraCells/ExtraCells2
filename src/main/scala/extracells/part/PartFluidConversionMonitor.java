package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor {

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        if (super.onActivate(player, pos)) return true;
        if (player == null || player.worldObj == null) return true;
        if (player.worldObj.isRemote) return true;
        ItemStack heldItem = player.getCurrentEquippedItem();
        IMEMonitor<IAEFluidStack> mon = getFluidStorage();
        if (this.locked && heldItem != null && mon != null) {
            ItemStack container = heldItem.copy();
            container.stackSize = 1;
            MachineSource src = new MachineSource(this);
            ItemStack result = null;
            if (FluidUtil.isFilled(container)) {
                FluidStack f = FluidUtil.getFluidFromContainer(container);
                if (f == null) {
                    return true;
                }
                ItemStack simulation = FluidUtil.drainItemIntoAe(container, mon, Actionable.SIMULATE, src);
                if (simulation == null) {
                    return true;
                }
                result = FluidUtil.drainItemIntoAe(container, mon, Actionable.MODULATE, src);
            } else if (FluidUtil.isEmpty(container)) {
                if (this.fluid == null) return true;
                MutablePair<ItemStack, FluidStack> simulation = FluidUtil.fillItemFromAe(
                        container, new FluidStack(this.fluid, Integer.MAX_VALUE), mon, Actionable.SIMULATE, src);
                if (simulation == null || simulation.getLeft() == null) {
                    return true;
                }
                result = FluidUtil.fillItemFromAe(
                                container, new FluidStack(this.fluid, Integer.MAX_VALUE), mon, Actionable.MODULATE, src)
                        .getLeft();
            }
            if (result == null) {
                return true;
            }
            TileEntity tile = this.getHost().getTile();
            ForgeDirection side = this.getSide();
            this.dropItems(
                    tile.getWorldObj(),
                    tile.xCoord + side.offsetX,
                    tile.yCoord + side.offsetY,
                    tile.zCoord + side.offsetZ,
                    result);
            ItemStack newHeldItem = heldItem.copy();
            newHeldItem.stackSize--;
            player.inventory.setInventorySlotContents(
                    player.inventory.currentItem, newHeldItem.stackSize <= 0 ? null : newHeldItem);
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
        rh.setTexture(side);
        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderInventoryBox(renderer);
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        ts.setBrightness(13 << 20 | 13 << 4);

        rh.setInvColor(0xFFFFFF);
        rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

        rh.setBounds(3, 3, 15, 13, 13, 16);
        rh.setInvColor(AEColor.Transparent.blackVariant);
        rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.mediumVariant);
        rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.whiteVariant);
        rh.renderInventoryFace(TextureManager.CONVERSION_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        renderInventoryBusLights(rh, renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        final IPartHost host = getHost();

        // Render Front Screen
        rh.setBounds(3, 3, 15, 13, 13, 16);
        Tessellator.instance.setColorOpaque_I(host.getColor().mediumVariant);
        rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
        Tessellator.instance.setColorOpaque_I(host.getColor().whiteVariant);
        rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        Tessellator.instance.setColorOpaque_I(host.getColor().blackVariant);
        rh.renderFace(x, y, z, TextureManager.CONVERSION_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

        renderFrontPanel(x, y, z, rh, renderer);
        renderBackPanel(x, y, z, rh, renderer);
        renderPowerStatus(x, y, z, rh, renderer);
    }
}

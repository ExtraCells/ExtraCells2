package extracells.part;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.network.AbstractPacket;
import extracells.render.TextureManager;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import java.io.IOException;

public class PartBattery extends PartECBase implements IAEPowerStorage, IInventoryUpdateReceiver {

    private IIcon batteryIcon = TextureManager.BATTERY_FRONT.getTexture();
    private ItemStack battery;
    IAEItemPowerStorage handler;
    private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.battery", 1, 1) {

        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return itemStack != null && itemStack.getItem() instanceof IAEItemPowerStorage;
        }
    };

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.BATTERY_FRONT.getTextures()[0], side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderInventoryBusLights(rh, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(side, side, side, batteryIcon, side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderStaticBusLights(x, y, z, rh, renderer);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.writeToNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.readFromNBT(data.getTagList("inventory", 10));
        onInventoryChanged();
    }

    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        AbstractPacket.writeString(battery != null ? battery.getItem().getIconIndex(battery).getIconName() : "none", data);
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        String iconName = AbstractPacket.readString(data);
        if (!iconName.equals("none")) {
            batteryIcon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite(iconName);
        } else {
            batteryIcon = TextureManager.BATTERY_FRONT.getTexture();
        }
        return true;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 2;
    }

    @Override
    public void onInventoryChanged() {
        battery = inventory.getStackInSlot(0);
        if (battery != null && battery.getItem() instanceof IAEItemPowerStorage) {
            batteryIcon = battery.getIconIndex();
            handler = (IAEItemPowerStorage) battery.getItem();
        } else {
            batteryIcon = null;
            handler = null;
        }
        IGridNode node = getGridNode();
        if (node != null) {
            IGrid grid = node.getGrid();
            if (grid != null) {
                grid.postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
            }
            getHost().markForUpdate();
        }
    }

    @Override
    public double injectAEPower(double amt, Actionable mode) {
        if (handler == null || battery == null)
            return 0;
        return handler.injectAEPower(mode == Actionable.MODULATE ? battery : battery.copy(), amt);
    }

    @Override
    public double getAEMaxPower() {
        if (handler == null || battery == null)
            return 0;
        return handler.getAEMaxPower(battery);
    }

    @Override
    public double getAECurrentPower() {
        if (handler == null || battery == null)
            return 0;
        return handler.getAECurrentPower(battery);
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        if (handler == null || battery == null)
            return AccessRestriction.NO_ACCESS;
        return handler.getPowerFlow(battery);
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        if (handler == null || battery == null)
            return 0;
        return handler.extractAEPower(mode == Actionable.MODULATE ? battery : battery.copy(), usePowerMultiplier.multiply(amt));
    }
}

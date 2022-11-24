package extracells.util;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.*;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import extracells.inventory.VoidCellInventory;
import extracells.inventory.VoidCellInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class VoidCellHandler implements ICellHandler {

    @Override
    public boolean isCell(final ItemStack is) {
        return VoidCellInventory.isCell(is);
    }

    @Override
    public IMEInventoryHandler getCellInventory(
            final ItemStack is, final ISaveProvider container, final StorageChannel channel) {
        if (channel == StorageChannel.ITEMS) {
            return VoidCellInventory.getCell(is, container);
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(
            final EntityPlayer player,
            final IChestOrDrive chest,
            final ICellHandler cellHandler,
            final IMEInventoryHandler inv,
            final ItemStack is,
            final StorageChannel chan) {
        Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
    }

    @Override
    public int getStatusForCell(final ItemStack is, final IMEInventory handler) {
        if (handler instanceof VoidCellInventoryHandler) {
            final VoidCellInventoryHandler ci = (VoidCellInventoryHandler) handler;
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(final ItemStack is, final IMEInventory handler) {
        return 10.0;
    }
}

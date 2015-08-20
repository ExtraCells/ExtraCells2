package extracells.container;

import extracells.tileentity.TileEntityVibrationChamberFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerVibrationChamberFluid extends Container {

    public TileEntityVibrationChamberFluid tileentity;

    public ContainerVibrationChamberFluid(InventoryPlayer player,
                                TileEntityVibrationChamberFluid tileentity) {
        this.tileentity = tileentity;

        bindPlayerInventory(player);
    }

    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, i * 18 + 84));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return tileentity.hasWorldObj() ?
                tileentity.getWorldObj().getTileEntity(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord) == this.tileentity :
                false;
    }

    @Override
    protected void retrySlotClick(int par1, int par2, boolean par3,
                                  EntityPlayer par4EntityPlayer) {
        // DON'T DO ANYTHING, YOU SHITTY METHOD!
    }
}

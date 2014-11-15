package extracells;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.WorldCoord;
import extracells.api.ExtraCellsApi;
import extracells.network.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ExtraCellsApiInstance implements ExtraCellsApi {

    public static final ExtraCellsApi instance = new ExtraCellsApiInstance();

    @Override
    public String getVerion() {
        return Extracells.VERSION;
    }

    @Override
    public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack itemStack, World world, int x, int y, int z, Long key) {
        if (world.isRemote)
            return itemStack;
        IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable().findLocatableBySerial(key);
        if (securityTerminal == null)
            return itemStack;
        IGridNode gridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
        if (gridNode == null)
            return itemStack;
        IGrid grid = gridNode.getGrid();
        if (grid == null)
            return itemStack;
        for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) AEApi.instance().blocks().blockWireless.entity())) {
            IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
            WorldCoord distance = accessPoint.getLocation().subtract(x, y, z);
            int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
            if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
                IStorageGrid gridCache = grid.getCache(IStorageGrid.class);
                if (gridCache != null) {
                    IMEMonitor<IAEFluidStack> fluidInventory = gridCache.getFluidInventory();
                    if (fluidInventory != null) {
                        GuiHandler.launchGui(GuiHandler.getGuiId(1), player, fluidInventory);
                    }
                }
            }
        }
        return itemStack;
    }

}

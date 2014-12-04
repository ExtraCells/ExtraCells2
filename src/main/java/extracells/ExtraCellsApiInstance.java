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
import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import extracells.api.ExtraCellsApi;
import extracells.api.IWirelessFluidTermHandler;
import extracells.api.definitions.IBlockDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;
import extracells.definitions.BlockDefinition;
import extracells.definitions.ItemDefinition;
import extracells.definitions.PartDefinition;
import extracells.network.GuiHandler;
import extracells.wireless.WirelessTermRegistry;

public class ExtraCellsApiInstance implements ExtraCellsApi {
	
	public static final ExtraCellsApi instance = new ExtraCellsApiInstance();
	
	@Override
	public String getVerion() {
		return Extracells.VERSION;
	}

	@Override
	public void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler) {
    	WirelessTermRegistry.registerWirelesFluidTermHandler(handler);
	}
    
    @Override
	public IWirelessFluidTermHandler getWirelessFluidTermHandler(ItemStack is) {
		return WirelessTermRegistry.getWirelessTermHandler(is);
	}

	@Override
	public boolean isWirelessFluidTerminal(ItemStack is) {
		return WirelessTermRegistry.isWirelessItem(is);
	}

	@Override
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world) {
		if(world.isRemote)
			return stack;
		if(!isWirelessFluidTerminal(stack))
			return stack;
		IWirelessFluidTermHandler handler = getWirelessFluidTermHandler(stack);
		if(!handler.hasPower(player, 1.0D, stack))
			return stack;
		Long key;
		try {
            key = Long.parseLong(handler.getEncryptionKey(stack));
        } catch (Throwable ignored) {
            return stack;
        }
		return openWirelessTerminal(player, stack, world, (int) player.posX, (int) player.posY, (int) player.posZ, key);
	}

	@Deprecated
	@Override
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack itemStack,
			World world, int x, int y, int z, Long key) {
		if(world.isRemote)
			return itemStack;
		IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locateable().findLocateableBySerial(key);
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
                        GuiHandler.launchGui(GuiHandler.getGuiId(1), player, fluidInventory, getWirelessFluidTermHandler(itemStack));
                    }
                }
            }
        }
		return itemStack;
	}
	
	@Override
	public IItemDefinition items() {
		return ItemDefinition.instance;
	}

	@Override
	public IBlockDefinition blocks() {
		return BlockDefinition.instance;
	}

	@Override
	public IPartDefinition parts() {
		return PartDefinition.instance;
	}

}

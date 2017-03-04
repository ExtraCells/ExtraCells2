package extracells.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.AEPartLocation;
import extracells.api.IECTileEntity;
import extracells.container.ContainerFluidStorage;
import extracells.container.ContainerGasStorage;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class ExtraCellsEventHandler {

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		TileEntity tile = event.getWorld().getTileEntity(event.getPos());
		if (tile instanceof IECTileEntity) {
			if (!PermissionUtil.hasPermission(event.getPlayer(),
					SecurityPermissions.BUILD,
					((IECTileEntity) tile).getGridNode(AEPartLocation.INTERNAL)))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER
				&& event.player != null) {
			if (event.player.openContainer != null) {
				Container con = event.player.openContainer;
				if (con instanceof ContainerFluidStorage) {
					((ContainerFluidStorage) con).removeEnergyTick();
				}else if (con instanceof ContainerGasStorage) {
					((ContainerGasStorage) con).removeEnergyTick();
				}
			}
		}
	}
}

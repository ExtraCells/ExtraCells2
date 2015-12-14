package extracells.util;

import appeng.api.config.SecurityPermissions;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import extracells.api.IECTileEntity;
import extracells.container.ContainerFluidStorage;
import extracells.container.ContainerGasStorage;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent;

public class ExtraCellsEventHandler {

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
		if (tile instanceof IECTileEntity) {
			if (!PermissionUtil.hasPermission(event.getPlayer(),
					SecurityPermissions.BUILD,
					((IECTileEntity) tile).getGridNode(ForgeDirection.UNKNOWN)))
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

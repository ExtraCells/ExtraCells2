package extracells.util;

import net.minecraft.inventory.Container;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import extracells.container.ContainerFluidStorage;

public class TickHandler {
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase == TickEvent.Phase.START && event.side == Side.SERVER && event.player != null){
			if(event.player.openContainer != null){
				Container con = event.player.openContainer;
				if(con instanceof ContainerFluidStorage){
					((ContainerFluidStorage) con).removeEnergyTick();
				}
			}
		}
	}

}

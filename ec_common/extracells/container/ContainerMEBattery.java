package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerMEBattery extends Container
{

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

}

package extracells.tile;

import net.minecraft.tileentity.TileEntity;

public class TileEntitySolderingStation extends TileEntity{
	public static float isActive = 0.0F;
	
	public TileEntitySolderingStation() {}
	
	public boolean isNightTime() {
		if(!worldObj.isRemote) {
			if(worldObj.isDaytime() && !worldObj.canBlockSeeTheSky(xCoord, yCoord+1, zCoord)){
				return true;
			} else
				return false;
		}
		return false;
	} 
	
	public boolean isWorking() {
		if(!isNightTime()) {
			isActive = 1.1F;
			return true;
		} else
			isActive = 0.0F;
			return false;
	}

}

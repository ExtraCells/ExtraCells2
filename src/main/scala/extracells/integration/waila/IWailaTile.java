package extracells.integration.waila;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public interface IWailaTile {

	public List<String> getWailaBody(List<String> list, NBTTagCompound tag,
			ForgeDirection side);

	public NBTTagCompound getWailaTag(NBTTagCompound tag);

}

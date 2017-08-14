package extracells.integration.waila;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface IWailaTile {

	List<String> getWailaBody(List<String> list, NBTTagCompound tag,
		EnumFacing side);

	NBTTagCompound getWailaTag(NBTTagCompound tag);

}

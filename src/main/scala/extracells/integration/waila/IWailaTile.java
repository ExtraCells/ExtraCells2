package extracells.integration.waila;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public interface IWailaTile {

	List<String> getWailaBody(List<String> list, NBTTagCompound tag,
		EnumFacing side);

	NBTTagCompound getWailaTag(NBTTagCompound tag);

}

package extracells.integration.waila;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface IWailaTile {

	public List<String> getWailaBody(List<String> list, NBTTagCompound tag,
			EnumFacing side);

	public NBTTagCompound getWailaTag(NBTTagCompound tag);

}

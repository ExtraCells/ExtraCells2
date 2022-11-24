package extracells.integration.waila;

import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public interface IWailaTile {

    public List<String> getWailaBody(List<String> list, NBTTagCompound tag, ForgeDirection side);

    public NBTTagCompound getWailaTag(NBTTagCompound tag);
}

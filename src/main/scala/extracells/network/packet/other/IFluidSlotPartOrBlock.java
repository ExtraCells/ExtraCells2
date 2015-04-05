package extracells.network.packet.other;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;

public interface IFluidSlotPartOrBlock {

	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player);
}

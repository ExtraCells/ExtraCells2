package extracells.network.packet.other;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.Fluid;

public interface IFluidSlotPartOrBlock {

	void setFluid(int index, Fluid fluid, EntityPlayer player);
}

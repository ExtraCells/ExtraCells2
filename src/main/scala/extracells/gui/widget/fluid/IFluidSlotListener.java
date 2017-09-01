package extracells.gui.widget.fluid;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.Fluid;

public interface IFluidSlotListener {

	void setFluid(int index, Fluid fluid, EntityPlayer player);
}

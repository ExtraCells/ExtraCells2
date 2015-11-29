package thaumicenergistics.api;

import net.minecraftforge.fluids.Fluid;
import thaumcraft.api.aspects.Aspect;

public interface IThEEssentiaGas
{
	/**
	 * Get the aspect this gas is based off of.
	 * 
	 * @return
	 */
	public Aspect getAspect();

	/**
	 * Gets the fluid form of the gas.
	 * 
	 * @return
	 */
	public Fluid getFluid();
}

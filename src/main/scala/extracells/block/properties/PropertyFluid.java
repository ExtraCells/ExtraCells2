package extracells.block.properties;

import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidStack;

public class PropertyFluid implements IUnlistedProperty<FluidStack> {
	private final String name;

	public PropertyFluid(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Class<FluidStack> getType() {
		return FluidStack.class;
	}

	@Override
	public boolean isValid(FluidStack value) {
		return value != null && value.amount > 0 && value.getFluid() != null;
	}

	@Override
	public String valueToString(FluidStack value) {
		return value.toString();
	}
}

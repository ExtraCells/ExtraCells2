package extracells.api;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;

public interface IECTileEntity extends IGridHost {

	DimensionalCoord getLocation();

	double getPowerUsage();

}

package extracells.gridblock;

import appeng.api.networking.IGridHost;

public class GBFluidExport extends ECBaseGridBlock
{

	public GBFluidExport(IGridHost _host)
	{
		super(_host);
	}

	@Override
	public double getIdlePowerUsage() {
		return 0;
	}
}

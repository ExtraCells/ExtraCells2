package extracells.integration.WAILA;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import extracells.Extracells;
import extracells.tileentity.TileEntityMonitorStorageFluid;

public class WailaDataProvider implements IWailaDataProvider
{

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		List<String> list = currenttip;
		Class clazz = accessor.getClass();
		TileEntity tileEntity = accessor.getTileEntity();
		if (tileEntity instanceof TileEntityMonitorStorageFluid)
		{
			TileEntityMonitorStorageFluid fluidMonitor = (TileEntityMonitorStorageFluid) tileEntity;
			Fluid fluid = fluidMonitor.getFluid();
			String fluidName = fluid != null ? fluid.getLocalizedName() : "-";
			long fluidAmount = fluid != null ? fluidMonitor.getAmount() : 0;

			String amountToText = Long.toString(fluidAmount) + "mB";
			if (Extracells.shortenedBuckets)
			{
				if (fluidAmount > 1000000000L)
					amountToText = Long.toString(fluidAmount / 1000000000L) + "MegaB";
				else if (fluidAmount > 1000000L)
					amountToText = Long.toString(fluidAmount / 1000000L) + "KiloB";
				else if (fluidAmount > 9999L)
				{
					amountToText = Long.toString(fluidAmount / 1000L) + "B";
				}
			}

			list.add(StatCollector.translateToLocal("tooltip.fluid") + ": " + fluidName);
			list.add(StatCollector.translateToLocal("tooltip.amount") + ": " + amountToText);
		}
		return list;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@SuppressWarnings("UnusedDeclaration")
	public static void callbackRegister(IWailaRegistrar registrar)
	{
		IWailaDataProvider provider = new WailaDataProvider();
		registrar.registerBodyProvider(provider, TileEntityMonitorStorageFluid.class);
	}
}

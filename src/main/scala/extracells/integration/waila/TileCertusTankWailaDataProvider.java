package extracells.integration.waila;

import extracells.tileentity.TileEntityCertusTank;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class TileCertusTankWailaDataProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile,
			NBTTagCompound tag, World world, int x, int y, int z) {
		if (tile instanceof TileEntityCertusTank) {
			if (((TileEntityCertusTank) tile).tank.getFluid() == null)
				tag.setInteger("fluidID", -1);
			else {
				tag.setInteger("fluidID",
						((TileEntityCertusTank) tile).tank.getFluid().getFluidID());
				tag.setInteger("currentFluid",
						((TileEntityCertusTank) tile).tank.getFluidAmount());
			}
			tag.setInteger("maxFluid",
					((TileEntityCertusTank) tile).tank.getCapacity());
		}
		return tag;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> list,
			IWailaDataAccessor accessor, IWailaConfigHandler config) {
		NBTTagCompound tag = accessor.getNBTData();
		if (tag == null)
			return list;
		if (tag.hasKey("fluidID")) {
			int fluidID = tag.getInteger("fluidID");
			if (fluidID == -1) {
				list.add(StatCollector
						.translateToLocal("extracells.tooltip.fluid")
						+ ": "
						+ StatCollector
								.translateToLocal("extracells.tooltip.empty1"));
				list.add(StatCollector
						.translateToLocal("extracells.tooltip.amount")
						+ ": 0mB / " + tag.getInteger("maxFluid") + "mB");
				return list;
			} else {
				Fluid fluid = FluidRegistry.getFluid(tag.getInteger("fluidID"));
				list.add(StatCollector
						.translateToLocal("extracells.tooltip.fluid")
						+ ": "
						+ fluid.getLocalizedName(new FluidStack(fluid,
								FluidContainerRegistry.BUCKET_VOLUME)));
			}
		} else
			return list;
		if (tag.hasKey("maxFluid") && tag.hasKey("currentFluid"))
			list.add(StatCollector
					.translateToLocal("extracells.tooltip.amount")
					+ ": "
					+ tag.getInteger("currentFluid")
					+ "mB / "
					+ tag.getInteger("maxFluid") + "mB");
		return list;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return accessor.getStack();
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

}

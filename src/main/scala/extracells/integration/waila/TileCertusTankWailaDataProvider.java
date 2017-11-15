package extracells.integration.waila;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import extracells.tileentity.TileEntityCertusTank;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileCertusTankWailaDataProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity tile,
		NBTTagCompound tag, World world, BlockPos blockPos) {
		if (tile instanceof TileEntityCertusTank) {
			TileEntityCertusTank tileTank = (TileEntityCertusTank) tile;
			IFluidTankProperties properties = tileTank.getTankInfo(true)[0];
			FluidStack content = properties.getContents();
			if (content == null || content.getFluid() == null) {
				tag.setString("fluidName", "");
			} else {
				tag.setString("fluidName",
					content.getFluid().getName());
				tag.setInteger("currentFluid",
					content.amount);
			}
			tag.setInteger("maxFluid",
				properties.getCapacity());
		}
		return tag;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> list,
		IWailaDataAccessor accessor, IWailaConfigHandler config) {
		NBTTagCompound tag = accessor.getNBTData();
		if (tag == null) {
			return list;
		}
		if (tag.hasKey("fluidName")) {
			String fluidName = tag.getString("fluidName");
			if (fluidName.isEmpty()) {
				list.add(I18n
					.translateToLocal("extracells.tooltip.fluid")
					+ ": "
					+ I18n
					.translateToLocal("extracells.tooltip.empty1"));
				list.add(I18n
					.translateToLocal("extracells.tooltip.amount")
					+ ": 0mB / " + tag.getInteger("maxFluid") + "mB");
				return list;
			} else {
				Fluid fluid = FluidRegistry.getFluid(fluidName);
				list.add(I18n
					.translateToLocal("extracells.tooltip.fluid")
					+ ": "
					+ fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME)));
			}
		} else {
			return list;
		}
		if (tag.hasKey("maxFluid") && tag.hasKey("currentFluid")) {
			list.add(I18n
				.translateToLocal("extracells.tooltip.amount")
				+ ": "
				+ tag.getInteger("currentFluid")
				+ "mB / "
				+ tag.getInteger("maxFluid") + "mB");
		}
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

package extracells.part.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.models.PartModels;
import extracells.util.AEUtils;
import extracells.util.PermissionUtil;

public class PartFluidExport extends PartFluidIO {

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 5.0F;
	}

	@Override
	public boolean doWork(int rate, int TicksSinceLastCall) {
		IFluidHandler facingTank = getFacingTank();
		if (facingTank == null || !isActive()) {
			return false;
		}

		for (Fluid fluid : getActiveFilters()) {
			if (fluid != null) {
				IAEFluidStack stack = extractFluid(AEUtils.createFluidStack(new FluidStack(fluid, rate * TicksSinceLastCall)), Actionable.SIMULATE);

				if (stack == null) {
					continue;
				}
				int filled = facingTank.fill(stack.getFluidStack(), true);

				if (filled > 0) {
					extractFluid(AEUtils.createFluidStack(new FluidStack(fluid, filled)), Actionable.MODULATE);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(6, 6, 12, 10, 10, 13);
		bch.addBox(4, 4, 13, 12, 12, 14);
		bch.addBox(5, 5, 14, 11, 11, 15);
		bch.addBox(6, 6, 15, 10, 10, 16);
		bch.addBox(6, 6, 11, 10, 10, 12);
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		return super.getCapability(capabilityClass);
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.EXPORT_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.EXPORT_ON;
		}
		return PartModels.EXPORT_OFF;
	}
}

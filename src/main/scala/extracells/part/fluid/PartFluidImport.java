package extracells.part.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.models.PartModels;
import extracells.util.AEUtils;
import extracells.util.PermissionUtil;

public class PartFluidImport extends PartFluidIO implements IFluidHandler {

	public static final int AMOUNT_PER_TRANSFER = 125;

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 5.0F;
	}

	@Override
	public boolean doWork(int rate, int TicksSinceLastCall) {
		if (getFacingTank() == null || !isActive()) {
			return false;
		}
		boolean empty = true;

		for (Fluid fluid : getActiveFilters()) {
			if (fluid != null) {
				empty = false;

				if (fillToNetwork(fluid, rate * TicksSinceLastCall)) {
					return true;
				}
			}
		}
		return empty && fillToNetwork(null, rate * TicksSinceLastCall);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		boolean redstonePowered = isRedstonePowered();
		if (resource == null || redstonePowered && getRedstoneMode() == RedstoneMode.LOW_SIGNAL || !redstonePowered && getRedstoneMode() == RedstoneMode.HIGH_SIGNAL) {
			return 0;
		}
		int drainAmount = Math.min(AMOUNT_PER_TRANSFER + this.speedState * AMOUNT_PER_TRANSFER, resource.amount);
		FluidStack toFill = new FluidStack(resource.getFluid(), drainAmount);
		Actionable action = doFill ? Actionable.MODULATE : Actionable.SIMULATE;
		IAEFluidStack filled = injectFluid(AEUtils.createFluidStack(toFill), action);
		if (filled == null) {
			return toFill.amount;
		}
		return toFill.amount - (int) filled.getStackSize();
	}

	protected boolean fillToNetwork(Fluid fluid, int toDrain) {
		FluidStack drained;
		IFluidHandler facingTank = getFacingTank();
		if (fluid == null) {
			drained = facingTank.drain(toDrain, false);
		} else {
			drained = facingTank.drain(new FluidStack(fluid, toDrain), false);
		}

		if (drained == null || drained.amount <= 0 || drained.getFluid() == null) {
			return false;
		}

		IAEFluidStack toFill = AEUtils.createFluidStack(drained);
		IAEFluidStack notInjected = injectFluid(toFill, Actionable.MODULATE);

		if (notInjected != null) {
			int amount = (int) (toFill.getStackSize() - notInjected.getStackSize());
			if (amount > 0) {
				if (fluid == null) {
					facingTank.drain(amount, true);
				} else {
					facingTank.drain(new FluidStack(toFill.getFluid(), amount), true);
				}
				return true;
			} else {
				return false;
			}
		} else {
			if (fluid == null) {
				facingTank.drain(toFill.getFluidStack().amount, true);
			} else {
				facingTank.drain(toFill.getFluidStack(), true);
			}
			return true;
		}
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(4, 4, 14, 12, 12, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
		bch.addBox(6, 6, 12, 10, 10, 13);
		bch.addBox(6, 6, 11, 10, 10, 12);
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[0];
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand enumHand, Vec3d pos) {
		return PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this) && super.onActivate(player, enumHand, pos);
	}

	@Override
	public <T> T getCapability(Capability<T> capabilityClass) {
		return super.getCapability(capabilityClass);
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.IMPORT_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.IMPORT_ON;
		} else {
			return PartModels.IMPORT_OFF;
		}
	}
}

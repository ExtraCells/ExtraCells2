package extracells.part.fluid;

import extracells.util.MachineSource;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.models.PartModels;
import extracells.util.AEUtils;
import extracells.util.FluidHelper;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor {

	protected boolean wasActivated(EntityPlayer player, EnumHand hand, Vec3d pos){
		return super.onActivate(player, hand, pos);
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		boolean wasActivated = super.onActivate(player, hand, pos);
		if (wasActivated) {
			return wasActivated;
		}
		if (player == null || player.world == null) {
			return true;
		}
		if (player.world.isRemote) {
			return true;
		}
		ItemStack heldItem = player.getHeldItem(hand);
		IMEMonitor<IAEFluidStack> mon = getFluidStorage();
		if (this.locked && heldItem != null && (!heldItem.isEmpty()) && mon != null) {
			ItemStack itemStack = heldItem.copy();
			itemStack.setCount(1);
			if (FluidHelper.isDrainableFilledContainer(itemStack)) {
				FluidStack f = FluidHelper.getFluidFromContainer(itemStack);
				if (f == null) {
					return true;
				}
				IAEFluidStack fluidStack = AEUtils.createFluidStack(f);
				IAEFluidStack injectItems = mon.injectItems(fluidStack.copy(),
					Actionable.SIMULATE, new MachineSource(this));
				if (mon.canAccept(fluidStack)
					&& (injectItems == null || injectItems.getStackSize() == 0L)) {
					mon.injectItems(fluidStack, Actionable.MODULATE, new MachineSource(
						this));

					Pair<Integer, ItemStack> emptyStack = FluidHelper.drainStack(itemStack, f);
					ItemStack empty = emptyStack.getRight();
					if (empty != null && !empty.isEmpty()) {
						dropItems(getHost().getTile().getWorld(), getHost().getTile().getPos().offset(getFacing()), empty);
					}
					ItemStack s3 = heldItem.copy();
					s3.setCount(s3.getCount() - 1);
					if (s3.getCount() == 0) {
						player.setHeldItem(hand, ItemStack.EMPTY);
					} else {
						player.setHeldItem(hand, s3);
					}
				}
				return true;
			} else if (FluidHelper.isFillableContainerWithRoom(itemStack)) {
				if (this.fluid == null) {
					return true;
				}
				IAEFluidStack extract = mon.extractItems(
						AEUtils.createFluidStack(this.fluid),
						Actionable.SIMULATE, new MachineSource(this));
				if (extract != null) {
					mon.extractItems(AEUtils.createFluidStack(new FluidStack(this.fluid,
							(int) extract.getStackSize())),
						Actionable.MODULATE, new MachineSource(this));
					Pair<Integer, ItemStack> empty1 = FluidHelper
						.fillStack(itemStack, extract.getFluidStack());
					if (empty1.getKey() == 0) {
						mon.injectItems(AEUtils.createFluidStack(new FluidStack(this.fluid,
								(int) extract.getStackSize())),
							Actionable.MODULATE, new MachineSource(this));
						return true;
					}
					ItemStack empty = empty1.getRight();
					if (empty != null && !empty.isEmpty()) {
						dropItems(getHost().getTile().getWorld(), getHost().getTile().getPos().offset(getFacing()), empty);
					}
					ItemStack s3 = heldItem.copy();
					s3.setCount(s3.getCount() - 1);
					if (s3.getCount() == 0) {
						player.setHeldItem(hand, ItemStack.EMPTY);
					} else {
						player.setHeldItem(hand, s3);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.CONVERSION_MONITOR_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.CONVERSION_MONITOR_ON;
		} else {
			return PartModels.CONVERSION_MONITOR_OFF;
		}
	}

}

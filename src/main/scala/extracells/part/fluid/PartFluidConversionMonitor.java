package extracells.part.fluid;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.models.PartModels;
import extracells.util.FluidUtil;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor {

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		boolean b = super.onActivate(player, hand, pos);
		if (b)
			return b;
		if (player == null || player.worldObj == null)
			return true;
		if (player.worldObj.isRemote)
			return true;
		ItemStack s = player.getHeldItem(hand);
		IMEMonitor<IAEFluidStack> mon = getFluidStorage();
		if (this.locked && s != null && mon != null) {
			ItemStack s2 = s.copy();
			s2.stackSize = 1;
			if (FluidUtil.isFilled(s2)) {
				FluidStack f = FluidUtil.getFluidFromContainer(s2);
				if (f == null)
					return true;
				IAEFluidStack fl = FluidUtil.createAEFluidStack(f);
				IAEFluidStack not = mon.injectItems(fl.copy(),
						Actionable.SIMULATE, new MachineSource(this));
				if (mon.canAccept(fl)
						&& (not == null || not.getStackSize() == 0L)) {
					mon.injectItems(fl, Actionable.MODULATE, new MachineSource(
							this));

					MutablePair<Integer, ItemStack> empty1 = FluidUtil
							.drainStack(s2, f);
					ItemStack empty = empty1.right;
					if (empty != null) {
						dropItems(getHost().getTile().getWorld(), getHost().getTile().getPos().offset(getFacing()), empty);
					}
					ItemStack s3 = s.copy();
					s3.stackSize = s3.stackSize - 1;
					if (s3.stackSize == 0) {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, null);
					} else {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, s3);
					}
				}
				return true;
			} else if (FluidUtil.isEmpty(s2)) {
				if (this.fluid == null)
					return true;
				IAEFluidStack extract;
				if (s2.getItem() instanceof IFluidContainerItem) {
					extract = mon.extractItems(FluidUtil.createAEFluidStack(
							this.fluid, ((IFluidContainerItem) s2.getItem())
									.getCapacity(s2)), Actionable.SIMULATE,
							new MachineSource(this));
				} else
					extract = mon.extractItems(
							FluidUtil.createAEFluidStack(this.fluid),
							Actionable.SIMULATE, new MachineSource(this));
				if (extract != null) {
					mon.extractItems(FluidUtil
							.createAEFluidStack(new FluidStack(this.fluid,
									(int) extract.getStackSize())),
							Actionable.MODULATE, new MachineSource(this));
					MutablePair<Integer, ItemStack> empty1 = FluidUtil
							.fillStack(s2, extract.getFluidStack());
					if (empty1.left == 0) {
						mon.injectItems(FluidUtil
								.createAEFluidStack(new FluidStack(this.fluid,
										(int) extract.getStackSize())),
								Actionable.MODULATE, new MachineSource(this));
						return true;
					}
					ItemStack empty = empty1.right;
					if (empty != null) {
						dropItems(getHost().getTile().getWorld(), getHost().getTile().getPos().offset(getFacing()), empty);
					}
					ItemStack s3 = s.copy();
					s3.stackSize = s3.stackSize - 1;
					if (s3.stackSize == 0) {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, null);
					} else {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, s3);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public IPartModel getStaticModels() {
		if(isActive() && isPowered()) {
			return PartModels.CONVERSION_MONITOR_HAS_CHANNEL;
		} else if(isPowered()) {
			return PartModels.CONVERSION_MONITOR_ON;
		} else {
			return PartModels.CONVERSION_MONITOR_OFF;
		}
	}

}

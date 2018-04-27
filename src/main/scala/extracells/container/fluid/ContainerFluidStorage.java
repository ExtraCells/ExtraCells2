package extracells.container.fluid;

import extracells.util.PlayerSource;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.IPortableFluidStorageCell;
import extracells.api.IWirelessFluidTermHandler;
import extracells.container.ContainerStorage;
import extracells.container.StorageType;
import extracells.util.AEUtils;
import extracells.util.FluidHelper;

public class ContainerFluidStorage extends ContainerStorage {

	public ContainerFluidStorage(EntityPlayer player, EnumHand hand) {
		super(StorageType.FLUID, player, hand);
	}

	public ContainerFluidStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IPortableFluidStorageCell storageCell, EnumHand hand) {
		super(StorageType.FLUID, monitor, player, storageCell, hand);
	}

	public ContainerFluidStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IWirelessFluidTermHandler handler, EnumHand hand) {
		super(StorageType.FLUID, monitor, player, handler, hand);
	}

	public ContainerFluidStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, EnumHand hand) {
		super(StorageType.FLUID, monitor, player, hand);
	}

	@Override
	public void doWork() {
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if ((secondSlot != null && !secondSlot.isEmpty()) && secondSlot.getCount() >= secondSlot.getMaxStackSize()) {
			return;
		}
		ItemStack container = this.inventory.getStackInSlot(0);
		if (!FluidHelper.isFluidContainer(container)) {
			return;
		}
		if (this.monitor == null) {
			return;
		}

		container = container.copy();
		container.setCount(1);

		if (FluidHelper.isDrainableFilledContainer(container)) {
			FluidStack containerFluid = FluidHelper.getFluidFromContainer(container);

			//Tries to inject fluid to network.
			IAEFluidStack notInjected = this.monitor.injectItems(
					AEUtils.createFluidStack(containerFluid),
					Actionable.SIMULATE, new PlayerSource(this.player, null));
			if (notInjected != null) {
				return;
			}
			ItemStack handItem = player.getHeldItem(hand);
			if (this.handler != null) {
				if (!this.handler.hasPower(this.player, 20.0D,
						handItem)) {
					return;
				}
				this.handler.usePower(this.player, 20.0D,
						handItem);
			} else if (this.storageCell != null) {
				if (!this.storageCell.hasPower(this.player, 20.0D,
						handItem)) {
					return;
				}
				this.storageCell.usePower(this.player, 20.0D,
						handItem);
			}
			Pair<Integer, ItemStack> drainedContainer = FluidHelper
					.drainStack(container, containerFluid);
			if (fillSecondSlot(drainedContainer.getRight())) {
				this.monitor.injectItems(
						AEUtils.createFluidStack(containerFluid),
						Actionable.MODULATE,
						new PlayerSource(this.player, null));
				decreaseFirstSlot();
			}
		} else if (FluidHelper.isFillableContainerWithRoom(container)) {
			if (this.selectedFluid == null) {
				return;
			}
			int capacity = FluidHelper.getCapacity(container, selectedFluid);
			//Tries to simulate the extraction of fluid from storage.
			IAEFluidStack result = this.monitor.extractItems(AEUtils.createFluidStack(this.selectedFluid, capacity), Actionable.SIMULATE, new PlayerSource(this.player, null));

			//Calculates the amount of fluid to fill container with.
			int proposedAmount = result == null ? 0 : (int) Math.min(capacity, result.getStackSize());

			if (proposedAmount == 0) {
				return;
			}

			//Tries to fill the container with fluid.
			Pair<Integer, ItemStack> filledContainer = FluidHelper.fillStack(container, new FluidStack(this.selectedFluid, proposedAmount));

			//Moves it to second slot and commits extraction to grid.
			if (fillSecondSlot(filledContainer.getRight())) {
				this.monitor.extractItems(AEUtils.createFluidStack(
					this.selectedFluid, filledContainer.getLeft()),
					Actionable.MODULATE,
					new PlayerSource(this.player, null));
				decreaseFirstSlot();
			}

		}
	}
}

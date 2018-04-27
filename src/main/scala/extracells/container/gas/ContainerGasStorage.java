package extracells.container.gas;

import extracells.util.PlayerSource;
import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.Optional;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.IPortableGasStorageCell;
import extracells.api.IWirelessGasTermHandler;
import extracells.container.ContainerStorage;
import extracells.container.StorageType;
import extracells.util.AEUtils;
import extracells.util.GasUtil;
import mekanism.api.gas.GasStack;

public class ContainerGasStorage extends ContainerStorage {

	private boolean doNextFill = false;

	public ContainerGasStorage(EntityPlayer player, EnumHand hand) {
		super(StorageType.GAS, player, hand);
	}

	public ContainerGasStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IPortableGasStorageCell storageCell, EnumHand hand) {
		super(StorageType.GAS, monitor, player, storageCell, hand);
	}

	public ContainerGasStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, IWirelessGasTermHandler handler, EnumHand hand) {
		super(StorageType.GAS, monitor, player, handler, hand);
	}

	public ContainerGasStorage(IMEMonitor<IAEFluidStack> monitor, EntityPlayer player, EnumHand hand) {
		super(StorageType.GAS, monitor, player, hand);
	}

	@Override
	public void doWork() {
		doWorkMekanism();
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public void doWorkMekanism() {
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if ((secondSlot != null && !secondSlot.isEmpty()) && secondSlot.getCount() >= secondSlot.getMaxStackSize()) {
			return;
		}
		ItemStack container = this.inventory.getStackInSlot(0);
		if (container == null || container.isEmpty()) {
			doNextFill = false;
		}
		if (!GasUtil.isGasContainer(container)) {
			return;
		}
		if (this.monitor == null) {
			return;
		}
		GasStack gasStack = GasUtil.getGasFromContainer(container);
		container = container.copy();
		container.setCount(1);
		if (GasUtil.isEmpty(container) || (gasStack.amount < GasUtil.getCapacity(container) && GasUtil.getFluidStack(gasStack).getFluid() == this.selectedFluid && doNextFill)) {
			if (this.selectedFluid == null) {
				return;
			}
			int capacity = GasUtil.getCapacity(container);
			//Tries to simulate the extraction of fluid from storage.
			IAEFluidStack result = this.monitor.extractItems(AEUtils.createFluidStack(this.selectedFluid, capacity), Actionable.SIMULATE, new PlayerSource(this.player, null));

			//Calculates the amount of fluid to fill container with.
			int proposedAmount = result == null ? 0 : gasStack == null ? (int) Math.min(capacity, result.getStackSize()) : (int) Math.min(capacity - gasStack.amount, result.getStackSize());

			//Tries to fill the container with fluid.
			MutablePair<Integer, ItemStack> filledContainer = GasUtil.fillStack(container, GasUtil.getGasStack(new FluidStack(this.selectedFluid, proposedAmount)));

			GasStack gasStack2 = GasUtil.getGasFromContainer(filledContainer.getRight());

			//Moves it to second slot and commits extraction to grid.
			if (container.getCount() == 1 && gasStack2.amount < GasUtil.getCapacity(filledContainer.getRight())) {
				this.inventory.setInventorySlotContents(0, filledContainer.getRight());
				monitor.extractItems(AEUtils.createFluidStack(this.selectedFluid, filledContainer.getLeft()), Actionable.MODULATE, new PlayerSource(this.player, null));
				doNextFill = true;

			} else if (fillSecondSlot(filledContainer.getRight())) {
				monitor.extractItems(AEUtils.createFluidStack(this.selectedFluid, filledContainer.getLeft()), Actionable.MODULATE, new PlayerSource(this.player, null));
				decreaseFirstSlot();
				doNextFill = false;
			}

		} else if (GasUtil.isFilled(container)) {
			GasStack containerGas = GasUtil.getGasFromContainer(container);

			MutablePair<Integer, ItemStack> drainedContainer = GasUtil.drainStack(container.copy(), containerGas);
			GasStack gasStack1 = containerGas.copy();
			gasStack1.amount = drainedContainer.getLeft();

			//Tries to inject fluid to network.
			IAEFluidStack notInjected = this.monitor.injectItems(GasUtil.createAEFluidStack(gasStack1), Actionable.SIMULATE, new PlayerSource(this.player, null));
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
			ItemStack emptyContainer = drainedContainer.getRight();
			if (emptyContainer != null && (!emptyContainer.isEmpty()) && GasUtil.getGasFromContainer(emptyContainer) != null && emptyContainer.getCount() == 1) {
				monitor.injectItems(GasUtil.createAEFluidStack(gasStack1), Actionable.MODULATE, new PlayerSource(this.player, null));
				this.inventory.setInventorySlotContents(0, emptyContainer);
			} else if (emptyContainer == null || emptyContainer.isEmpty() || fillSecondSlot(drainedContainer.getRight())) {
				monitor.injectItems(GasUtil.createAEFluidStack(containerGas), Actionable.MODULATE, new PlayerSource(this.player, null));
				decreaseFirstSlot();
			}
		}
	}
}

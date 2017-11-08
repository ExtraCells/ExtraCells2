package extracells.part.fluid;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import extracells.util.*;
import net.minecraftforge.fluids.FluidUtil;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.container.ContainerTerminal;
import extracells.container.StorageType;
import extracells.gridblock.ECBaseGridBlock;
import extracells.gui.GuiTerminal;
import extracells.inventory.IInventoryListener;
import extracells.inventory.InventoryPlain;
import extracells.models.PartModels;
import extracells.network.packet.part.PacketTerminalSelectFluidClient;
import extracells.part.PartECBase;

public class PartFluidTerminal extends PartECBase implements IGridTickable, IInventoryListener {

	private final List<Object> containers = new ArrayList<Object>();
	protected final InventoryPlain inventory = new InventoryPlain(
		"extracells.part.fluid.terminal", 2, 64, this) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return isItemValidForInputSlot(i, itemStack);
		}

		@Override
		protected void onContentsChanged() {
			saveData();
		}
	};
	protected Fluid currentFluid;

	protected boolean isItemValidForInputSlot(int i, ItemStack itemStack) {
		return FluidHelper.isFluidContainer(itemStack);
	}

	protected MachineSource machineSource = new MachineSource(this);

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : inventory.slots) {
			if (stack == null) {
				continue;
			}
			drops.add(stack);
		}
	}

	@Override
	public ItemStack getItemStack(PartItemStack type) {
		ItemStack stack = super.getItemStack(type);
		if (type.equals(PartItemStack.WRENCH)) {
			stack.getTagCompound().removeTag("inventory");
		}
		return stack;
	}

	public void addContainer(ContainerTerminal containerTerminalFluid) {
		this.containers.add(containerTerminalFluid);
		sendCurrentFluid();
	}

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 1.0F;
	}

	public void decreaseFirstSlot() {
		ItemStack slot = this.inventory.getStackInSlot(0);
		slot.setCount(slot.getCount() - 1);
		if (slot.getCount() <= 0) {
			this.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
		}
	}

	public void doWork() {
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if ((secondSlot != null && !secondSlot.isEmpty()) && secondSlot.getCount() >= secondSlot.getMaxStackSize()) {
			return;
		}
		ItemStack container = this.inventory.getStackInSlot(0);
		if (!FluidHelper.isFluidContainer(container)) {
			return;
		}
		container = container.copy();
		container.setCount(1);

		ECBaseGridBlock gridBlock = getGridBlock();
		if (gridBlock == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null) {
			return;
		}

		if(FluidHelper.isDrainableFilledContainer(container)) {
			FluidStack containerFluid = FluidHelper.getFluidFromContainer(container);
			IAEFluidStack notInjected = monitor.injectItems(AEUtils.createFluidStack(containerFluid), Actionable.SIMULATE, this.machineSource);
			if (notInjected != null) {
				return;
			}
			Pair<Integer, ItemStack> drainedContainer = FluidHelper.drainStack(container, containerFluid);
			ItemStack emptyContainer = drainedContainer.getRight();
			if (emptyContainer == null || fillSecondSlot(emptyContainer)) {
				monitor.injectItems(AEUtils.createFluidStack(containerFluid), Actionable.MODULATE, this.machineSource);
				decreaseFirstSlot();
			}
		}else if (FluidHelper.isFillableContainerWithRoom(container)) {
			if (this.currentFluid == null) {
				return;
			}
			int capacity = FluidHelper.getCapacity(container, currentFluid);
			IAEFluidStack result = monitor.extractItems(AEUtils.createFluidStack(this.currentFluid, capacity), Actionable.SIMULATE, this.machineSource);
			int proposedAmount = result == null ? 0 : (int) Math.min(capacity, result.getStackSize());
			Pair<Integer, ItemStack> filledContainer = FluidHelper.fillStack(container, new FluidStack(this.currentFluid, proposedAmount));
			if (filledContainer.getLeft() > proposedAmount) {
				return;
			}
			if (fillSecondSlot(filledContainer.getRight())) {
				monitor.extractItems(AEUtils.createFluidStack(this.currentFluid, filledContainer.getLeft()), Actionable.MODULATE, this.machineSource);
				decreaseFirstSlot();
			}
		}
	}

	public boolean fillSecondSlot(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot == null || secondSlot.isEmpty()) {
			this.inventory.setInventorySlotContents(1, itemStack);
			return true;
		} else {
			if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot)) {
				return false;
			}
			this.inventory.incrStackSize(1, itemStack.getCount());
			return true;
		}
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(4, 4, 13, 12, 12, 14);
		bch.addBox(5, 5, 12, 11, 11, 13);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiTerminal(this, player, StorageType.FLUID);
	}

	public IInventory getInventory() {
		return this.inventory;
	}

	@Override
	public double getPowerUsage() {
		return 0.5D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerTerminal(this, player, StorageType.FLUID);
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (isActive() && (PermissionUtil.hasPermission(player, SecurityPermissions.INJECT, (IPart) this) || PermissionUtil.hasPermission(player, SecurityPermissions.EXTRACT, (IPart) this))) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	@Override
	public void onInventoryChanged() {
		saveData();
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.inventory.readFromNBT(data.getTagList("inventory", 10));
	}

	public void removeContainer(ContainerTerminal containerTerminalFluid) {
		this.containers.remove(containerTerminalFluid);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public IPartModel getStaticModels() {
		if (isActive()) {
			return PartModels.TERMINAL_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.TERMINAL_ON;
		} else {
			return PartModels.TERMINAL_OFF;
		}
	}

	public void sendCurrentFluid() {
		for (Object containerFluidTerminal : this.containers) {
			sendCurrentFluid(containerFluidTerminal);
		}
	}

	public void sendCurrentFluid(Object container) {
		if (container instanceof ContainerTerminal) {
			ContainerTerminal containerFluidTerminal = (ContainerTerminal) container;
			NetworkUtil.sendToPlayer(new PacketTerminalSelectFluidClient(currentFluid), containerFluidTerminal.getPlayer());
		}

	}

	public void setCurrentFluid(Fluid currentFluid) {
		this.currentFluid = currentFluid;
		sendCurrentFluid();
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node,
		int TicksSinceLastCall) {
		doWork();
		return TickRateModulation.FASTER;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("inventory", this.inventory.writeToNBT());
	}
}

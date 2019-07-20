package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.*;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidTerminal;
import extracells.container.ContainerGasTerminal;
import extracells.gridblock.ECBaseGridBlock;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.part.PacketFluidTerminal;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import extracells.util.PermissionUtil;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class PartFluidTerminal extends PartECBase implements IGridTickable,
		IInventoryUpdateReceiver {

	protected Fluid currentFluid;
	private List<Object> containers = new ArrayList<Object>();
	protected ECPrivateInventory inventory = new ECPrivateInventory(
			"extracells.part.fluid.terminal", 2, 64, this) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return isItemValidForInputSlot(i, itemStack);
		}
	};

	protected boolean isItemValidForInputSlot(int i, ItemStack itemStack) {
		return FluidUtil.isFluidContainer(itemStack);
	}
	protected MachineSource machineSource = new MachineSource(this);

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : inventory.slots) {
			if (stack == null)
				continue;
			drops.add(stack);
		}
	}

	@Override
	public int getLightLevel() {
		return this.isPowered() ? 9 : 0;
	}

	public void addContainer(ContainerFluidTerminal containerTerminalFluid) {
		this.containers.add(containerTerminalFluid);
		sendCurrentFluid();
	}

	public void addContainer(ContainerGasTerminal containerTerminalGas) {
		this.containers.add(containerTerminalGas);
		sendCurrentFluid();
	}

	@Override
	public int cableConnectionRenderTo() {
		return 1;
	}

	public void decreaseFirstSlot() {
		ItemStack slot = this.inventory.getStackInSlot(0);
		slot.stackSize--;
		if (slot.stackSize <= 0)
			this.inventory.setInventorySlotContents(0, null);
	}

	public void doWork() {
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot != null && secondSlot.stackSize >= secondSlot.getMaxStackSize())
			return;
		ItemStack container = this.inventory.getStackInSlot(0);
		if (!FluidUtil.isFluidContainer(container))
			return;
		container = container.copy();
		container.stackSize = 1;

		ECBaseGridBlock gridBlock = getGridBlock();
		if (gridBlock == null)
			return;
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null)
			return;

		if (FluidUtil.isEmpty(container)) {
			if (this.currentFluid == null)
				return;
			int capacity = FluidUtil.getCapacity(container);
			IAEFluidStack result = monitor.extractItems(FluidUtil.createAEFluidStack(this.currentFluid, capacity), Actionable.SIMULATE, this.machineSource);
			int proposedAmount = result == null ? 0 : (int) Math.min(capacity, result.getStackSize());
			if(proposedAmount == 0)
				return;
			MutablePair<Integer, ItemStack> filledContainer = FluidUtil.fillStack(container, new FluidStack(this.currentFluid, proposedAmount));
			if(filledContainer.getLeft() > proposedAmount)
				return;
			if (fillSecondSlot(filledContainer.getRight())) {
				monitor.extractItems(FluidUtil.createAEFluidStack(this.currentFluid, filledContainer.getLeft()), Actionable.MODULATE, this.machineSource);
				decreaseFirstSlot();
			}
		} else {
			FluidStack containerFluid = FluidUtil.getFluidFromContainer(container);
			IAEFluidStack notInjected = monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.SIMULATE, this.machineSource);
			if (notInjected != null)
				return;
			MutablePair<Integer, ItemStack> drainedContainer = FluidUtil.drainStack(container, containerFluid);
			ItemStack emptyContainer = drainedContainer.getRight();
			if (emptyContainer == null || fillSecondSlot(emptyContainer)) {
				monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.MODULATE, this.machineSource);
				decreaseFirstSlot();
			}
		}
	}

	public boolean fillSecondSlot(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		ItemStack secondSlot = this.inventory.getStackInSlot(1);
		if (secondSlot == null) {
			this.inventory.setInventorySlotContents(1, itemStack);
			return true;
		} else {
			if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot))
				return false;
			this.inventory.incrStackSize(1, itemStack.stackSize);
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
		return new GuiFluidTerminal(this, player);
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
		return new ContainerFluidTerminal(this, player);
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (isActive() && (PermissionUtil.hasPermission(player, SecurityPermissions.INJECT, (IPart) this) || PermissionUtil.hasPermission(player, SecurityPermissions.EXTRACT, (IPart) this)))
			return super.onActivate(player, pos);
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

	public void removeContainer(ContainerFluidTerminal containerTerminalFluid) {
		this.containers.remove(containerTerminalFluid);
	}

	public void removeContainer(ContainerGasTerminal containerTerminalGas) {
		this.containers.remove(containerTerminalGas);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderInventoryBox(renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		ts.setBrightness(13 << 20 | 13 << 4);

		rh.setInvColor(0xFFFFFF);
		rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(),
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(3, 3, 15, 13, 13, 16);
		rh.setInvColor(AEColor.Transparent.blackVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[0],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.mediumVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[1],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.whiteVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[2],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		if (isActive())
			Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

		ts.setColorOpaque_I(0xFFFFFF);
		rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		IPartHost host = getHost();
		rh.setBounds(3, 3, 15, 13, 13, 16);
		ts.setColorOpaque_I(host.getColor().blackVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().mediumVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().whiteVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	public void sendCurrentFluid() {
		for (Object containerFluidTerminal : this.containers) {
			sendCurrentFluid(containerFluidTerminal);
		}
	}

	public void sendCurrentFluid(Object container) {
		if(container instanceof ContainerFluidTerminal){
			ContainerFluidTerminal containerFluidTerminal = (ContainerFluidTerminal) container;
			new PacketFluidTerminal(containerFluidTerminal.getPlayer(), this.currentFluid).sendPacketToPlayer(containerFluidTerminal.getPlayer());
		}else if(container instanceof ContainerGasTerminal){
			ContainerGasTerminal containerGasTerminal = (ContainerGasTerminal) container;
			new PacketFluidTerminal(containerGasTerminal.getPlayer(), this.currentFluid).sendPacketToPlayer(containerGasTerminal.getPlayer());
		}

	}

	public void setCurrentFluid(Fluid _currentFluid) {
		this.currentFluid = _currentFluid;
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

package extracells.network.packet.part;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidStorage;
import extracells.container.ContainerGasStorage;
import extracells.gui.GuiFluidStorage;
import extracells.gui.GuiGasStorage;
import extracells.integration.Integration;
import extracells.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class PacketFluidStorage extends AbstractPacket {

	private IItemList<IAEFluidStack> fluidStackList;
	private Fluid currentFluid;
	private boolean hasTermHandler;

	@SuppressWarnings("unused")
	public PacketFluidStorage() {}

	public PacketFluidStorage(EntityPlayer _player) {
		super(_player);
		this.mode = 2;
	}

	public PacketFluidStorage(EntityPlayer _player, boolean _hasTermHandler) {
		super(_player);
		this.mode = 3;
		this.hasTermHandler = _hasTermHandler;
	}

	public PacketFluidStorage(EntityPlayer _player, Fluid _currentFluid) {
		super(_player);
		this.mode = 1;
		this.currentFluid = _currentFluid;
	}

	public PacketFluidStorage(EntityPlayer _player, IItemList<IAEFluidStack> _list) {
		super(_player);
		this.mode = 0;
		this.fluidStackList = _list;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			case0();
			break;
		case 1:
			if (this.player != null && this.player.openContainer instanceof ContainerFluidStorage) {
				((ContainerFluidStorage) this.player.openContainer).receiveSelectedFluid(this.currentFluid);
			}else if (this.player != null && Integration.Mods.MEKANISMGAS.isEnabled() && this.player.openContainer instanceof ContainerGasStorage) {
				((ContainerGasStorage) this.player.openContainer).receiveSelectedFluid(this.currentFluid);
			}
			break;
		case 2:
			if (this.player != null) {
				if (!this.player.worldObj.isRemote) {
					if (this.player.openContainer instanceof ContainerFluidStorage) {
						((ContainerFluidStorage) this.player.openContainer).forceFluidUpdate();
						((ContainerFluidStorage) this.player.openContainer).doWork();
					}else if (this.player.openContainer instanceof ContainerGasStorage && Integration.Mods.MEKANISMGAS.isEnabled()) {
						((ContainerGasStorage) this.player.openContainer).forceFluidUpdate();
						((ContainerGasStorage) this.player.openContainer).doWork();
					}
				}
			}
			break;
		case 3:
			case3();
			break;
		}
	}

	@SideOnly(Side.CLIENT)
	private void case0(){
		if (this.player != null && this.player.isClientWorld()) {
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiFluidStorage) {
				ContainerFluidStorage container = (ContainerFluidStorage) ((GuiFluidStorage) gui).inventorySlots;
				container.updateFluidList(this.fluidStackList);
			}else if (gui instanceof GuiGasStorage  && Integration.Mods.MEKANISMGAS.isEnabled()) {
				ContainerGasStorage container = (ContainerGasStorage) ((GuiGasStorage) gui).inventorySlots;
				container.updateFluidList(this.fluidStackList);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void case3(){
		if (this.player != null && this.player.isClientWorld()) {
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiFluidStorage) {
				ContainerFluidStorage container = (ContainerFluidStorage) ((GuiFluidStorage) gui).inventorySlots;
				container.hasWirelessTermHandler = this.hasTermHandler;
			}else if (gui instanceof GuiGasStorage && Integration.Mods.MEKANISMGAS.isEnabled()) {
				ContainerGasStorage container = (ContainerGasStorage) ((GuiGasStorage) gui).inventorySlots;
				container.hasWirelessTermHandler = this.hasTermHandler;
			}
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.fluidStackList = AEApi.instance().storage().createFluidList();
			while (in.readableBytes() > 0) {
				Fluid fluid = readFluid(in);
				long fluidAmount = in.readLong();
				if (fluid != null) {
					IAEFluidStack stack = AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1));
					stack.setStackSize(fluidAmount);
					this.fluidStackList.add(stack);
				}
			}
			break;
		case 1:
			this.currentFluid = readFluid(in);
			break;
		case 2:
			break;
		case 3:
			this.hasTermHandler = in.readBoolean();
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			for (IAEFluidStack stack : this.fluidStackList) {
				FluidStack fluidStack = stack.getFluidStack();
				writeFluid(fluidStack.getFluid(), out);
				out.writeLong(fluidStack.amount);
			}
			break;
		case 1:
			writeFluid(this.currentFluid, out);
			break;
		case 2:
			break;
		case 3:
			out.writeBoolean(this.hasTermHandler);
			break;
		}
	}
}

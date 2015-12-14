package extracells.network.packet.part;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidTerminal;
import extracells.container.ContainerGasTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.gui.GuiGasTerminal;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidTerminal;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class PacketFluidTerminal extends AbstractPacket {

	IItemList<IAEFluidStack> fluidStackList;
	Fluid currentFluid;
	PartFluidTerminal terminalFluid;

	@SuppressWarnings("unused")
	public PacketFluidTerminal() {}

	public PacketFluidTerminal(EntityPlayer _player, Fluid _currentFluid) {
		super(_player);
		this.mode = 2;
		this.currentFluid = _currentFluid;
	}

	public PacketFluidTerminal(EntityPlayer _player, Fluid _currentFluid,
			PartFluidTerminal _terminalFluid) {
		super(_player);
		this.mode = 1;
		this.currentFluid = _currentFluid;
		this.terminalFluid = _terminalFluid;
	}

	public PacketFluidTerminal(EntityPlayer _player,
			IItemList<IAEFluidStack> _list) {
		super(_player);
		this.mode = 0;
		this.fluidStackList = _list;
	}

	public PacketFluidTerminal(EntityPlayer _player,
			PartFluidTerminal _terminalFluid) {
		super(_player);
		this.mode = 3;
		this.terminalFluid = _terminalFluid;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			case0();
			break;
		case 1:
			this.terminalFluid.setCurrentFluid(this.currentFluid);
			break;
		case 2:
			case2();
			break;
		case 3:
			if (this.player != null && this.player.openContainer instanceof ContainerFluidTerminal) {
				ContainerFluidTerminal fluidContainer = (ContainerFluidTerminal) this.player.openContainer;
				fluidContainer.forceFluidUpdate();
				this.terminalFluid.sendCurrentFluid(fluidContainer);
			} else if (this.player != null && this.player.openContainer instanceof ContainerGasTerminal) {
				ContainerGasTerminal fluidContainer = (ContainerGasTerminal) this.player.openContainer;
				fluidContainer.forceFluidUpdate();
				this.terminalFluid.sendCurrentFluid(fluidContainer);
			}
			break;
		}
	}

	@SideOnly(Side.CLIENT)
	public void case0(){
		if (this.player != null && this.player.isClientWorld()) {
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiFluidTerminal) {
				ContainerFluidTerminal container = (ContainerFluidTerminal) ((GuiFluidTerminal) gui).inventorySlots;
				container.updateFluidList(this.fluidStackList);
			} else if (gui instanceof GuiGasTerminal) {
				ContainerGasTerminal container = (ContainerGasTerminal) ((GuiGasTerminal) gui).inventorySlots;
				container.updateFluidList(this.fluidStackList);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void case2(){
		if (this.player != null && Minecraft.getMinecraft().currentScreen instanceof GuiFluidTerminal) {
			GuiFluidTerminal gui = (GuiFluidTerminal) Minecraft.getMinecraft().currentScreen;
			((ContainerFluidTerminal) gui.getContainer()).receiveSelectedFluid(this.currentFluid);
		} else if (this.player != null && Minecraft.getMinecraft().currentScreen instanceof GuiGasTerminal) {
			GuiGasTerminal gui = (GuiGasTerminal) Minecraft.getMinecraft().currentScreen;
			((ContainerGasTerminal) gui.getContainer()).receiveSelectedFluid(this.currentFluid);
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
				if (fluid == null || fluidAmount <= 0) {
					continue;
				}
				IAEFluidStack stack = AEApi.instance().storage()
						.createFluidStack(new FluidStack(fluid, 1));
				stack.setStackSize(fluidAmount);
				this.fluidStackList.add(stack);
			}
			break;
		case 1:
			this.terminalFluid = (PartFluidTerminal) readPart(in);
			this.currentFluid = readFluid(in);
			break;
		case 2:
			this.currentFluid = readFluid(in);
			break;
		case 3:
			this.terminalFluid = (PartFluidTerminal) readPart(in);
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
			writePart(this.terminalFluid, out);
			writeFluid(this.currentFluid, out);
			break;
		case 2:
			writeFluid(this.currentFluid, out);
			break;
		case 3:
			writePart(this.terminalFluid, out);
			break;
		}
	}
}

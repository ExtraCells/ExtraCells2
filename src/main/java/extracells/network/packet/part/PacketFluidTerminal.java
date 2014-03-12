package extracells.network.packet.part;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidTerminal;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;

public class PacketFluidTerminal extends AbstractPacket
{
	IItemList<IAEFluidStack> fluidStackList;
	Fluid currentFluid;
	PartFluidTerminal terminalFluid;

	@SuppressWarnings("unused")
	public PacketFluidTerminal()
	{
	}

	public PacketFluidTerminal(EntityPlayer _player, IItemList<IAEFluidStack> _list)
	{
		super(_player);
		mode = 0;
		fluidStackList = _list;
	}

	public PacketFluidTerminal(EntityPlayer _player, Fluid _currentFluid, PartFluidTerminal _terminalFluid)
	{
		super(_player);
		mode = 1;
		currentFluid = _currentFluid;
		terminalFluid = _terminalFluid;
	}

	public PacketFluidTerminal(EntityPlayer _player, Fluid _currentFluid)
	{
		super(_player);
		mode = 2;
		currentFluid = _currentFluid;
	}

	public PacketFluidTerminal(EntityPlayer _player, PartFluidTerminal _terminalFluid)
	{
		super(_player);
		mode = 3;
		terminalFluid = _terminalFluid;
	}

	@Override
	public void writeData(ByteBuf out) throws IOException
	{
		switch (mode)
		{
		case 0:
			out.writeInt(fluidStackList.size());
			for (IAEFluidStack stack : fluidStackList)
			{
				FluidStack fluidStack = stack.getFluidStack();
				writeFluid(fluidStack.getFluid(), out);
				out.writeLong(fluidStack.amount);
			}
			break;
		case 1:
			writePart(terminalFluid, out);
			writeFluid(currentFluid, out);
			break;
		case 2:
			writeFluid(currentFluid, out);
			break;
		case 3:
			writePart(terminalFluid, out);
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) throws IOException
	{
		switch (mode)
		{
		case 0:
			fluidStackList = AEApi.instance().storage().createFluidList();
			int length = in.readInt();
			for (int i = 0; i < length; i++)
			{
				Fluid fluid = readFluid(in);
				long fluidAmount = in.readLong();
				if (fluid != null)
				{
					IAEFluidStack stack = AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1));
					stack.setStackSize(fluidAmount);
					fluidStackList.add(stack);
				}
			}
			break;
		case 1:
			terminalFluid = (PartFluidTerminal) readPart(in);
			currentFluid = readFluid(in);
			break;
		case 2:
			currentFluid = readFluid(in);
			break;
		case 3:
			terminalFluid = (PartFluidTerminal) readPart(in);
			break;
		}
	}

	@Override
	public void execute()
	{
		switch (mode)
		{
		case 0:
			if (player != null && player.isClientWorld())
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidTerminal)
				{
					ContainerFluidTerminal container = (ContainerFluidTerminal) ((GuiFluidTerminal) gui).inventorySlots;
					container.updateFluidList(fluidStackList);
				}
			}
			break;
		case 1:
			terminalFluid.setCurrentFluid(currentFluid);
			break;
		case 2:
			if (player != null && player.isClientWorld() && player.openContainer instanceof ContainerFluidTerminal)
			{
				ContainerFluidTerminal container = (ContainerFluidTerminal) player.openContainer;
				container.setSelectedFluid(currentFluid);
			}
			break;
		case 3:
			if (player != null && player.openContainer instanceof ContainerFluidTerminal)
			{
				ContainerFluidTerminal fluidContainer = (ContainerFluidTerminal) player.openContainer;
				fluidContainer.forceFluidUpdate();
				terminalFluid.sendCurrentFluid(fluidContainer);
			}
			break;
		}
	}
}

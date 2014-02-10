package extracells.network.packet;

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
import net.minecraftforge.fluids.FluidRegistry;
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
	public void writePacketData(ByteBuf out) throws IOException
	{
		super.writePacketData(out);
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
			out.writeInt(currentFluid.getID());
			break;
		case 2:
			out.writeInt(currentFluid != null ? currentFluid.getID() : -1);
			break;
		case 3:
			writePart(terminalFluid, out);
			break;
		}
	}

	@Override
	public void readPacketData(ByteBuf in) throws IOException
	{
		super.readPacketData(in);
		switch (mode)
		{
		case 0:
			fluidStackList = AEApi.instance().storage().createItemList();
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
			currentFluid = FluidRegistry.getFluid(in.readInt());
			break;
		case 2:
			int fluidID = in.readInt();
			currentFluid = fluidID > 0 ? FluidRegistry.getFluid(fluidID) : null;
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
			if (player != null && player.worldObj.isRemote)
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
			if (player != null && player.worldObj.isRemote)
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidTerminal)
				{
					ContainerFluidTerminal container = (ContainerFluidTerminal) ((GuiFluidTerminal) gui).inventorySlots;
					container.setSelectedFluid(currentFluid);
				}
			}
			break;
		case 3:
			if (player != null && player.openContainer instanceof ContainerFluidTerminal)
				((ContainerFluidTerminal) player.openContainer).forceFluidUpdate();
			break;
		}
	}
}

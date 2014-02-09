package extracells.network.packet;

import extracells.gui.GuiBusIOFluid;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketBusIOFluid extends AbstractPacket
{
	private List<Fluid> filterFluids;
	private byte index;
	private Fluid fluid;
	private PartFluidIO part;
	private byte action;
	private byte ordinal;
	private byte filterSize;

	@SuppressWarnings("unused")
	public PacketBusIOFluid()
	{
	}

	public PacketBusIOFluid(List<Fluid> _filterFluids)
	{
		super();
		mode = 0;
		filterFluids = _filterFluids;
	}

	public PacketBusIOFluid(EntityPlayer _player, byte _index, Fluid _toSet, PartFluidIO _part)
	{
		super(_player);
		mode = 1;
		index = _index;
		fluid = _toSet;
		part = _part;
	}

	public PacketBusIOFluid(EntityPlayer _player, byte _action, PartFluidIO _part)
	{
		super(_player);
		mode = 2;
		action = _action;
		part = _part;
	}

	public PacketBusIOFluid(byte _action, byte _ordinal)
	{
		super();
		mode = 3;
		action = _action;
		ordinal = _ordinal;
	}

	public PacketBusIOFluid(EntityPlayer _player, PartFluidIO _part)
	{
		super(_player);
		System.out.println(_player);
		mode = 4;
		part = _part;
	}

	public PacketBusIOFluid(byte _filterSize)
	{
		super();
		mode = 5;
		filterSize = _filterSize;
	}

	@Override
	public void writePacketData(ByteBuf out) throws IOException
	{
		super.writePacketData(out);
		switch (mode)
		{
		case 0:
			out.writeInt(filterFluids.size());
			for (Fluid fluid : filterFluids)
			{
				writeFluid(fluid, out);
			}
			break;
		case 1:
			writeFluid(fluid, out);
			writePart(part, out);
			out.writeByte(index);
			break;
		case 2:
			writePart(part, out);
			out.writeByte(action);
			break;
		case 3:
			out.writeByte(action);
			out.writeByte(ordinal);
			break;
		case 4:
			writePart(part, out);
			break;
		case 5:
			out.writeByte(filterSize);
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
			filterFluids = new ArrayList<Fluid>();
			int length = in.readInt();
			for (int i = 0; i < length; i++)
				filterFluids.add(FluidRegistry.getFluid(readString(in)));
			break;
		case 1:
			fluid = readFluid(in);
			part = (PartFluidIO) readPart(in);
			index = in.readByte();
			break;
		case 2:
			part = (PartFluidIO) readPart(in);
			action = in.readByte();
			break;
		case 3:
			action = in.readByte();
			ordinal = in.readByte();
			break;
		case 4:
			part = (PartFluidIO) readPart(in);
			break;
		case 5:
			filterSize = in.readByte();
			break;
		}
	}

	public void execute()
	{
		Gui gui;
		switch (mode)
		{
		case 0:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusIOFluid)
			{
				GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
				partGui.updateFluids(filterFluids);
			}
			break;
		case 1:
			part.setFilterFluid(index, fluid, player);
			break;
		case 2:
			switch (action)
			{
			case 0:
				part.loopRedstoneMode(player);
				break;
			case 1:
				part.loopFluidMode(player);
				break;
			}
			break;
		case 3:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusIOFluid)
			{
				GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
				partGui.updateButtons(action, ordinal);
			}
			break;
		case 4:
			part.sendInformation(player);
			break;
		case 5:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusIOFluid)
			{
				GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
				partGui.changeConfig(filterSize);
			}
			break;
		}
	}
}

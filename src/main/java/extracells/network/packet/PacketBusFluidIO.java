package extracells.network.packet;

import appeng.api.config.RedstoneMode;
import extracells.gui.GuiBusFluidIO;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;
import java.util.List;

public class PacketBusFluidIO extends AbstractPacket
{
	private List<Fluid> filterFluids;
	private PartFluidIO part;
	private byte action;
	private byte ordinal;
	private byte filterSize;
	private boolean redstoneControlled;

	@SuppressWarnings("unused")
	public PacketBusFluidIO()
	{
	}

	public PacketBusFluidIO(EntityPlayer _player, byte _action, PartFluidIO _part)
	{
		super(_player);
		mode = 0;
		action = _action;
		part = _part;
	}

	public PacketBusFluidIO(RedstoneMode _redstoneMode)
	{
		super();
		mode = 1;
		ordinal = (byte) _redstoneMode.ordinal();
	}

	public PacketBusFluidIO(EntityPlayer _player, PartFluidIO _part)
	{
		super(_player);
		mode = 2;
		part = _part;
	}

	public PacketBusFluidIO(byte _filterSize)
	{
		super();
		mode = 3;
		filterSize = _filterSize;
	}

	public PacketBusFluidIO(boolean _redstoneControlled)
	{
		super();
		mode = 4;
		redstoneControlled = _redstoneControlled;
	}

	@Override
	public void writePacketData(ByteBuf out) throws IOException
	{
		super.writePacketData(out);
		switch (mode)
		{
		case 0:
			writePart(part, out);
			out.writeByte(action);
			break;
		case 1:
			out.writeByte(ordinal);
			break;
		case 2:
			writePart(part, out);
			break;
		case 3:
			out.writeByte(filterSize);
			break;
		case 4:
			out.writeBoolean(redstoneControlled);
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
			part = (PartFluidIO) readPart(in);
			action = in.readByte();
			break;
		case 1:
			ordinal = in.readByte();
			break;
		case 2:
			part = (PartFluidIO) readPart(in);
			break;
		case 3:
			filterSize = in.readByte();
			break;
		case 4:
			redstoneControlled = in.readBoolean();
			break;
		}
	}

	public void execute()
	{
		Gui gui;
		switch (mode)
		{
		case 0:

			part.loopRedstoneMode(player);
			break;
		case 1:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO)
			{
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.updateRedstoneMode(RedstoneMode.values()[ordinal]);
			}
			break;
		case 2:
			part.sendInformation(player);
			break;
		case 3:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO)
			{
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.changeConfig(filterSize);
			}
			break;
		case 4:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO)
			{
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.setRedstoneControlled(redstoneControlled);
			}
			break;
		}
	}
}

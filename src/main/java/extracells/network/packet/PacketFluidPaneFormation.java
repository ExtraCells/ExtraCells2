package extracells.network.packet;

import extracells.network.AbstractPacket;
import extracells.part.PartFluidPaneFormation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class PacketFluidPaneFormation extends AbstractPacket
{
	private PartFluidPaneFormation part;

	@SuppressWarnings("unused")
	public PacketFluidPaneFormation()
	{
	}

	public PacketFluidPaneFormation(EntityPlayer _player, PartFluidPaneFormation _part)
	{
		super(_player);
		mode = 0;
		part = _part;
	}

	@Override
	public void writePacketData(ByteBuf out) throws IOException
	{
		super.writePacketData(out);
		switch (mode)
		{
			case 0:
				writePart(part, out);
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
				part = (PartFluidPaneFormation) readPart(in);
				break;
		}
	}

	public void execute()
	{
		switch (mode)
		{
			case 0:
				part.sendInformation(player);
				break;
		}
	}
}

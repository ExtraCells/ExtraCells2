package extracells.network.packet;

import extracells.network.AbstractPacket;
import extracells.part.PartFluidPlaneFormation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public class PacketFluidPlaneFormation extends AbstractPacket
{
	private PartFluidPlaneFormation part;

	@SuppressWarnings("unused")
	public PacketFluidPlaneFormation()
	{
	}

	public PacketFluidPlaneFormation(EntityPlayer _player, PartFluidPlaneFormation _part)
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
				part = (PartFluidPlaneFormation) readPart(in);
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

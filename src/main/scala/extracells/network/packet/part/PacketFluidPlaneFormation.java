package extracells.network.packet.part;

import extracells.network.AbstractPacket;
import extracells.part.PartFluidPlaneFormation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class PacketFluidPlaneFormation extends AbstractPacket {

	private PartFluidPlaneFormation part;

	@SuppressWarnings("unused")
	public PacketFluidPlaneFormation() {}

	public PacketFluidPlaneFormation(EntityPlayer _player,
			PartFluidPlaneFormation _part) {
		super(_player);
		this.mode = 0;
		this.part = _part;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			this.part.sendInformation(this.player);
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.part = (PartFluidPlaneFormation) readPart(in);
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			writePart(this.part, out);
			break;
		}
	}
}

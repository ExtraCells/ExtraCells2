package extracells.network.packet.other;

import extracells.network.AbstractPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;

public class PacketGuiSwitch extends AbstractPacket {

	public int guiIndex;
	public TileEntity te;

	public PacketGuiSwitch() {}

	public PacketGuiSwitch(int guiIndex, TileEntity te) {
		this.guiIndex = guiIndex;
		this.te = te;
	}

	@Override
	public void execute() {
	}

	@Override
	public void readData(ByteBuf in) {
		guiIndex = in.readInt();
		if (in.readBoolean()) {
			te = readTileEntity(in);
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		out.writeInt(guiIndex);
		if (te == null) {
			out.writeBoolean(false);
		}
		else {
			out.writeBoolean(true);
			writeTileEntity(te, out);
		}
	}
}

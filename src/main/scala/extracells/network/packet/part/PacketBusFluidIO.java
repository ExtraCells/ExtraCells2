package extracells.network.packet.part;

import appeng.api.config.RedstoneMode;
import extracells.gui.GuiBusFluidIO;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;

import java.util.List;

public class PacketBusFluidIO extends AbstractPacket {

	private List<Fluid> filterFluids;
	private PartFluidIO part;
	private byte action;
	private byte ordinal;
	private byte filterSize;
	private boolean redstoneControlled;

	@SuppressWarnings("unused")
	public PacketBusFluidIO() {}

	public PacketBusFluidIO(boolean _redstoneControlled) {
		super();
		this.mode = 4;
		this.redstoneControlled = _redstoneControlled;
	}

	public PacketBusFluidIO(byte _filterSize) {
		super();
		this.mode = 3;
		this.filterSize = _filterSize;
	}

	public PacketBusFluidIO(EntityPlayer _player, byte _action,
			PartFluidIO _part) {
		super(_player);
		this.mode = 0;
		this.action = _action;
		this.part = _part;
	}

	public PacketBusFluidIO(EntityPlayer _player, PartFluidIO _part) {
		super(_player);
		this.mode = 2;
		this.part = _part;
	}

	public PacketBusFluidIO(RedstoneMode _redstoneMode) {
		super();
		this.mode = 1;
		this.ordinal = (byte) _redstoneMode.ordinal();
	}

	@Override
	public void execute() {
		Gui gui;
		switch (this.mode) {
		case 0:

			this.part.loopRedstoneMode(this.player);
			break;
		case 1:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO) {
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.updateRedstoneMode(RedstoneMode.values()[this.ordinal]);
			}
			break;
		case 2:
			this.part.sendInformation(this.player);
			break;
		case 3:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO) {
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.changeConfig(this.filterSize);
			}
			break;
		case 4:
			gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiBusFluidIO) {
				GuiBusFluidIO partGui = (GuiBusFluidIO) gui;
				partGui.setRedstoneControlled(this.redstoneControlled);
			}
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.part = (PartFluidIO) readPart(in);
			this.action = in.readByte();
			break;
		case 1:
			this.ordinal = in.readByte();
			break;
		case 2:
			this.part = (PartFluidIO) readPart(in);
			break;
		case 3:
			this.filterSize = in.readByte();
			break;
		case 4:
			this.redstoneControlled = in.readBoolean();
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			writePart(this.part, out);
			out.writeByte(this.action);
			break;
		case 1:
			out.writeByte(this.ordinal);
			break;
		case 2:
			writePart(this.part, out);
			break;
		case 3:
			out.writeByte(this.filterSize);
			break;
		case 4:
			out.writeBoolean(this.redstoneControlled);
			break;
		}
	}
}

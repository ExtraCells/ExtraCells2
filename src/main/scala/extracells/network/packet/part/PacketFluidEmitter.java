package extracells.network.packet.part;

import appeng.api.config.RedstoneMode;
import extracells.gui.GuiFluidEmitter;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidLevelEmitter;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

public class PacketFluidEmitter extends AbstractPacket {

	private long wantedAmount;
	private PartFluidLevelEmitter part;
	private RedstoneMode redstoneMode;
	private boolean toggle;

	public PacketFluidEmitter() {}

	public PacketFluidEmitter(boolean _toggle, PartFluidLevelEmitter _part,
			EntityPlayer _player) {
		this.mode = 3;
		this.toggle = _toggle;
		this.part = _part;
		this.player = _player;
	}

	public PacketFluidEmitter(int _wantedAmount, PartFluidLevelEmitter _part,
			EntityPlayer _player) {
		this.mode = 0;
		this.wantedAmount = _wantedAmount;
		this.part = _part;
		this.player = _player;
	}

	public PacketFluidEmitter(long _wantedAmount, EntityPlayer _player) {
		this.mode = 2;
		this.wantedAmount = _wantedAmount;
		this.player = _player;
	}

	public PacketFluidEmitter(RedstoneMode _redstoneMode, EntityPlayer _player) {
		this.mode = 4;
		this.redstoneMode = _redstoneMode;
		this.player = _player;
	}

	public PacketFluidEmitter(String textField, PartFluidLevelEmitter _part,
			EntityPlayer _player) {
		this.mode = 1;
		this.wantedAmount = textField.isEmpty() ? 0 : Long.parseLong(textField);
		this.part = _part;
		this.player = _player;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			this.part.changeWantedAmount((int) this.wantedAmount, this.player);
			break;
		case 1:
			this.part.setWantedAmount(this.wantedAmount, this.player);
			break;
		case 2:
			if (this.player != null && this.player.isClientWorld()) {
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidEmitter) {
					((GuiFluidEmitter) gui).setAmountField(this.wantedAmount);
				}
			}
			break;
		case 3:
			if (this.toggle) {
				this.part.toggleMode(this.player);
			} else {
				this.part.syncClientGui(this.player);
			}
			break;
		case 4:
			if (this.player != null && this.player.isClientWorld()) {
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidEmitter) {
					((GuiFluidEmitter) gui).setRedstoneMode(this.redstoneMode);
				}
			}
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.wantedAmount = in.readLong();
			this.part = (PartFluidLevelEmitter) readPart(in);
			break;
		case 1:
			this.wantedAmount = in.readLong();
			this.part = (PartFluidLevelEmitter) readPart(in);
			break;
		case 2:
			this.wantedAmount = in.readLong();
			break;
		case 3:
			this.toggle = in.readBoolean();
			this.part = (PartFluidLevelEmitter) readPart(in);
			break;
		case 4:
			this.redstoneMode = RedstoneMode.values()[in.readInt()];
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			out.writeLong(this.wantedAmount);
			writePart(this.part, out);
			break;
		case 1:
			out.writeLong(this.wantedAmount);
			writePart(this.part, out);
			break;
		case 2:
			out.writeLong(this.wantedAmount);
			break;
		case 3:
			out.writeBoolean(this.toggle);
			writePart(this.part, out);
			break;
		case 4:
			out.writeInt(this.redstoneMode.ordinal());
			break;
		}
	}
}

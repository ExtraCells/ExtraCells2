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

    public PacketFluidEmitter() {
    }

    public PacketFluidEmitter(int _wantedAmount, PartFluidLevelEmitter _part, EntityPlayer _player) {
        mode = 0;
        wantedAmount = _wantedAmount;
        part = _part;
        player = _player;
    }

    public PacketFluidEmitter(String textField, PartFluidLevelEmitter _part, EntityPlayer _player) {
        mode = 1;
        wantedAmount = textField.isEmpty() ? 0 : Long.parseLong(textField);
        part = _part;
        player = _player;
    }

    public PacketFluidEmitter(long _wantedAmount, EntityPlayer _player) {
        mode = 2;
        wantedAmount = _wantedAmount;
        player = _player;
    }

    public PacketFluidEmitter(boolean _toggle, PartFluidLevelEmitter _part, EntityPlayer _player) {
        mode = 3;
        toggle = _toggle;
        part = _part;
        player = _player;
    }

    public PacketFluidEmitter(RedstoneMode _redstoneMode, EntityPlayer _player) {
        mode = 4;
        redstoneMode = _redstoneMode;
        player = _player;
    }

    @Override
    public void writeData(ByteBuf out) {
        switch (mode) {
            case 0:
                out.writeLong(wantedAmount);
                writePart(part, out);
                break;
            case 1:
                out.writeLong(wantedAmount);
                writePart(part, out);
                break;
            case 2:
                out.writeLong(wantedAmount);
                break;
            case 3:
                out.writeBoolean(toggle);
                writePart(part, out);
                break;
            case 4:
                out.writeInt(redstoneMode.ordinal());
                break;
        }
    }

    @Override
    public void readData(ByteBuf in) {
        switch (mode) {
            case 0:
                wantedAmount = in.readLong();
                part = (PartFluidLevelEmitter) readPart(in);
                break;
            case 1:
                wantedAmount = in.readLong();
                part = (PartFluidLevelEmitter) readPart(in);
                break;
            case 2:
                wantedAmount = in.readLong();
                break;
            case 3:
                toggle = in.readBoolean();
                part = (PartFluidLevelEmitter) readPart(in);
                break;
            case 4:
                redstoneMode = RedstoneMode.values()[in.readInt()];
                break;
        }
    }

    @Override
    public void execute() {
        switch (mode) {
            case 0:
                part.changeWantedAmount((int) wantedAmount, player);
                break;
            case 1:
                part.setWantedAmount(wantedAmount, player);
                break;
            case 2:
                if (player != null && player.isClientWorld()) {
                    Gui gui = Minecraft.getMinecraft().currentScreen;
                    if (gui instanceof GuiFluidEmitter) {
                        ((GuiFluidEmitter) gui).setAmountField(wantedAmount);
                    }
                }
                break;
            case 3:
                if (toggle) {
                    part.toggleMode(player);
                } else {
                    part.syncClientGui(player);
                }
                break;
            case 4:
                if (player != null && player.isClientWorld()) {
                    Gui gui = Minecraft.getMinecraft().currentScreen;
                    if (gui instanceof GuiFluidEmitter) {
                        ((GuiFluidEmitter) gui).setRedstoneMode(redstoneMode);
                    }
                }
                break;
        }
    }
}

package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

import extracells.container.ContainerTerminal;
import extracells.network.packet.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.part.fluid.PartFluidTerminal;
import extracells.util.GuiUtil;

public class PacketTerminalOpenContainer extends Packet {
	PartFluidTerminal terminalFluid;

	public PacketTerminalOpenContainer(PartFluidTerminal terminalFluid) {
		this.terminalFluid = terminalFluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writePart(terminalFluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.TERMINAL_OPEN_CONTAINER;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			PartFluidTerminal terminalFluid = data.readPart(player.world);
			ContainerTerminal containerTerminal = GuiUtil.getContainer(player, ContainerTerminal.class);
			if (terminalFluid == null) {
				return;
			}

			containerTerminal.forceFluidUpdate();
			terminalFluid.sendCurrentFluid(containerTerminal);
		}
	}
}

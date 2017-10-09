package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

import extracells.container.ContainerStorage;
import extracells.network.packet.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.util.GuiUtil;

public class PacketStorageOpenContainer extends Packet {

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.STORAGE_OPEN_CONTAINER;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			ContainerStorage containerStorage = GuiUtil.getContainer(player, ContainerStorage.class);
			if (containerStorage == null) {
				return;
			}

			containerStorage.forceFluidUpdate();
			containerStorage.doWork();
		}
	}
}

package extracells.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

public interface IPacketHandlerServer extends IPacketHandler {
	void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException;
}

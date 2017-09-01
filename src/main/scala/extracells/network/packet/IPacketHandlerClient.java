package extracells.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

public interface IPacketHandlerClient extends IPacketHandler {
	void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException;
}

package extracells.network.packet;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public interface IPacket {
	FMLProxyPacket getPacket();

	PacketId getPacketId();
}

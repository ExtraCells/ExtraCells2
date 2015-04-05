package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.packet.part.PacketBusFluidIO;

public class HandlerBusFluidIO implements
		IMessageHandler<PacketBusFluidIO, IMessage> {

	@Override
	public IMessage onMessage(PacketBusFluidIO message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

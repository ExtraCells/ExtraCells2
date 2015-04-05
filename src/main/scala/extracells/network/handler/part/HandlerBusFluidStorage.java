package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.packet.part.PacketBusFluidStorage;

public class HandlerBusFluidStorage implements
		IMessageHandler<PacketBusFluidStorage, IMessage> {

	@Override
	public IMessage onMessage(PacketBusFluidStorage message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.packet.part.PacketFluidStorage;

public class HandlerFluidStorage implements
		IMessageHandler<PacketFluidStorage, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidStorage message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

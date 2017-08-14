package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketFluidEmitter;

public class HandlerFluidEmitter implements
	IMessageHandler<PacketFluidEmitter, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidEmitter message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

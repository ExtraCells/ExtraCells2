package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketFluidStorage;

public class HandlerFluidStorage implements
	IMessageHandler<PacketFluidStorage, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidStorage message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

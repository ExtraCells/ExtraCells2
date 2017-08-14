package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketFluidInterface;

public class HandlerFluidInterface implements
	IMessageHandler<PacketFluidInterface, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidInterface message, MessageContext ctx) {
		message.execute();
		return null;
	}

}

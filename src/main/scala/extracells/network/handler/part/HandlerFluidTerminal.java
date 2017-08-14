package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketFluidTerminal;

public class HandlerFluidTerminal implements
	IMessageHandler<PacketFluidTerminal, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidTerminal message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

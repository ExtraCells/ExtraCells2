package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.packet.part.PacketFluidTerminal;

public class HandlerFluidTerminal implements
		IMessageHandler<PacketFluidTerminal, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidTerminal message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

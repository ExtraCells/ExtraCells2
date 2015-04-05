package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.packet.part.PacketOreDictExport;

public class HandlerOreDictExport implements
		IMessageHandler<PacketOreDictExport, IMessage> {

	@Override
	public IMessage onMessage(PacketOreDictExport message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

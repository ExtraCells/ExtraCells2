package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketOreDictExport;

public class HandlerOreDictExport implements
	IMessageHandler<PacketOreDictExport, IMessage> {

	@Override
	public IMessage onMessage(PacketOreDictExport message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

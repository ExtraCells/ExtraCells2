package extracells.network.handler.other;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.other.PacketFluidSlot;

public class HandlerFluidSlot implements
	IMessageHandler<PacketFluidSlot, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidSlot message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

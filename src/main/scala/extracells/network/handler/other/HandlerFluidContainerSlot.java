package extracells.network.handler.other;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.other.PacketFluidContainerSlot;

public class HandlerFluidContainerSlot implements
	IMessageHandler<PacketFluidContainerSlot, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidContainerSlot message,
			MessageContext ctx) {
		message.execute();
		return null;
	}
}

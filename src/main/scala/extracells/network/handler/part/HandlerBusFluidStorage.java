package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketBusFluidStorage;

public class HandlerBusFluidStorage implements
	IMessageHandler<PacketBusFluidStorage, IMessage> {

	@Override
	public IMessage onMessage(PacketBusFluidStorage message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

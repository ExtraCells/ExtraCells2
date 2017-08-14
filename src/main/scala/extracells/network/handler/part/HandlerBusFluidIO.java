package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketBusFluidIO;

public class HandlerBusFluidIO implements
	IMessageHandler<PacketBusFluidIO, IMessage> {

	@Override
	public IMessage onMessage(PacketBusFluidIO message, MessageContext ctx) {
		message.execute();
		return null;
	}
}

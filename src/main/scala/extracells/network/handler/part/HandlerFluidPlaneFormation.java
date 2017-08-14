package extracells.network.handler.part;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import extracells.network.packet.part.PacketFluidPlaneFormation;

public class HandlerFluidPlaneFormation implements
	IMessageHandler<PacketFluidPlaneFormation, IMessage> {

	@Override
	public IMessage onMessage(PacketFluidPlaneFormation message,
			MessageContext ctx) {
		message.execute();
		return null;
	}
}

package extracells.network.handler.part;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
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

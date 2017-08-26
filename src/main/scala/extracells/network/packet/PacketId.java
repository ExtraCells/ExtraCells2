package extracells.network.packet;

import extracells.network.IPacketHandler;
import extracells.network.IPacketHandlerClient;
import extracells.network.IPacketHandlerServer;

public enum PacketId {
	FLUID_SLOT,
	FLUID_CONTAINER_SLOT,
	EXPORT_ORE,
	UPDATE,
	CONTAINER_UPDATE,
	TERMINAL_UPDATE_FLUID,
	TERMINAL_SELECT_FLUID,
	TERMINAL_OPEN_CONTAINER;

	private IPacketHandlerServer handlerServer;
	private IPacketHandlerClient handlerClient;

	public void registerHandler(IPacketHandler handler) {
		if(handler instanceof IPacketHandlerServer){
			registerHandler((IPacketHandlerServer) handler);
		}
		if(handler instanceof IPacketHandlerClient){
			registerHandler((IPacketHandlerClient) handler);
		}
	}

	public void registerHandler(IPacketHandlerServer handlerServer) {
		this.handlerServer = handlerServer;
	}

	public void registerHandler(IPacketHandlerClient handlerClient) {
		this.handlerClient = handlerClient;
	}

	public IPacketHandlerClient getHandlerClient() {
		return handlerClient;
	}

	public IPacketHandlerServer getHandlerServer() {
		return handlerServer;
	}
}

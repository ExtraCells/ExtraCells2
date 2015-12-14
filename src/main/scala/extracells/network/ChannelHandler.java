package extracells.network;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import extracells.network.handler.other.HandlerFluidContainerSlot;
import extracells.network.handler.other.HandlerFluidSlot;
import extracells.network.handler.part.*;
import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import java.util.EnumMap;

public class ChannelHandler {

	public static void registerMessages() {
		wrapper.registerMessage(HandlerFluidSlot.class, PacketFluidSlot.class,
				0, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidSlot.class, PacketFluidSlot.class,
				0, Side.SERVER);

		wrapper.registerMessage(HandlerBusFluidIO.class,
				PacketBusFluidIO.class, 1, Side.CLIENT);
		wrapper.registerMessage(HandlerBusFluidIO.class,
				PacketBusFluidIO.class, 1, Side.SERVER);

		wrapper.registerMessage(HandlerBusFluidStorage.class,
				PacketBusFluidStorage.class, 2, Side.CLIENT);
		wrapper.registerMessage(HandlerBusFluidStorage.class,
				PacketBusFluidStorage.class, 2, Side.SERVER);

		wrapper.registerMessage(HandlerFluidEmitter.class,
				PacketFluidEmitter.class, 3, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidEmitter.class,
				PacketFluidEmitter.class, 3, Side.SERVER);

		wrapper.registerMessage(HandlerFluidPlaneFormation.class,
				PacketFluidPlaneFormation.class, 4, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidPlaneFormation.class,
				PacketFluidPlaneFormation.class, 4, Side.SERVER);

		wrapper.registerMessage(HandlerFluidStorage.class,
				PacketFluidStorage.class, 5, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidStorage.class,
				PacketFluidStorage.class, 5, Side.SERVER);

		wrapper.registerMessage(HandlerFluidTerminal.class,
				PacketFluidTerminal.class, 6, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidTerminal.class,
				PacketFluidTerminal.class, 6, Side.SERVER);

		wrapper.registerMessage(HandlerFluidInterface.class,
				PacketFluidInterface.class, 7, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidInterface.class,
				PacketFluidInterface.class, 7, Side.SERVER);

		wrapper.registerMessage(HandlerFluidContainerSlot.class,
				PacketFluidContainerSlot.class, 8, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidContainerSlot.class,
				PacketFluidContainerSlot.class, 8, Side.SERVER);

		wrapper.registerMessage(HandlerOreDictExport.class,
				PacketOreDictExport.class, 9, Side.CLIENT);
		wrapper.registerMessage(HandlerOreDictExport.class,
				PacketOreDictExport.class, 9, Side.SERVER);

	}

	public static void sendPacketToAllPlayers(AbstractPacket packet) {
		wrapper.sendToAll(packet);
	}

	public static void sendPacketToAllPlayers(Packet packet, World world) {
		for (Object player : world.playerEntities) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).playerNetServerHandler
						.sendPacket(packet);
			}
		}
	}

	public static void sendPacketToPlayer(AbstractPacket packet,
			EntityPlayer player) {
		wrapper.sendTo(packet, (EntityPlayerMP) player);
	}

	public static void sendPacketToPlayersAround(AbstractPacket abstractPacket,
			NetworkRegistry.TargetPoint point) {
		wrapper.sendToAllAround(abstractPacket, point);
	}

	public static void sendPacketToServer(AbstractPacket packet) {
		wrapper.sendToServer(packet);
	}

	private static EnumMap<Side, FMLEmbeddedChannel> channels;

	public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE
			.newSimpleChannel("extracells");
}

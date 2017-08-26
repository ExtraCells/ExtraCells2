package extracells.network;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.network.handler.part.HandlerBusFluidIO;
import extracells.network.handler.part.HandlerBusFluidStorage;
import extracells.network.handler.part.HandlerFluidEmitter;
import extracells.network.handler.part.HandlerFluidInterface;
import extracells.network.handler.part.HandlerFluidPlaneFormation;
import extracells.network.packet.PacketId;
import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.network.packet.other.PacketFluidSlotSelect;
import extracells.network.packet.other.PacketFluidSlotUpdate;
import extracells.network.packet.part.PacketBusFluidIO;
import extracells.network.packet.part.PacketBusFluidStorage;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.network.packet.part.PacketFluidInterface;
import extracells.network.packet.part.PacketFluidPlaneFormation;
import extracells.network.packet.part.PacketOreDictExport;
import extracells.network.packet.part.PacketStorageOpenContainer;
import extracells.network.packet.part.PacketStorageSelectFluid;
import extracells.network.packet.part.PacketStorageUpdateFluid;
import extracells.network.packet.part.PacketStorageUpdateState;
import extracells.network.packet.part.PacketTerminalOpenContainer;
import extracells.network.packet.part.PacketTerminalSelectFluidClient;
import extracells.network.packet.part.PacketTerminalSelectFluidServer;
import extracells.network.packet.part.PacketTerminalUpdateFluid;
import extracells.util.Log;

public class PacketHandler {

	public static final String CHANNEL_ID = "extracells";
	private final FMLEventChannel channel;

	public PacketHandler() {
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_ID);
		channel.register(this);
	}

	@SubscribeEvent
	public void onPacket(ServerCustomPacketEvent event) {
		PacketBufferEC data = new PacketBufferEC(event.getPacket().payload());
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;

		byte packetIdOrdinal = data.readByte();
		PacketId packetId = PacketId.values()[packetIdOrdinal];
		IPacketHandlerServer packetHandler = packetId.getHandlerServer();
		checkThreadAndEnqueue(packetHandler, data, player, player.getServerWorld());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPacket(ClientCustomPacketEvent event) {
		PacketBufferEC data = new PacketBufferEC(event.getPacket().payload());

		byte packetIdOrdinal = data.readByte();
		PacketId packetId = PacketId.values()[packetIdOrdinal];
		IPacketHandlerClient packetHandler = packetId.getHandlerClient();
		checkThreadAndEnqueue(packetHandler, data, Minecraft.getMinecraft());
	}

	public void sendPacket(FMLProxyPacket packet, EntityPlayerMP player) {
		channel.sendTo(packet, player);
	}

	@SideOnly(Side.CLIENT)
	private static void checkThreadAndEnqueue(final IPacketHandlerClient packet, final PacketBufferEC data, IThreadListener threadListener) {
		if (!threadListener.isCallingFromMinecraftThread()) {
			threadListener.addScheduledTask(() -> {
				try {
					EntityPlayer player = Minecraft.getMinecraft().thePlayer;
					Preconditions.checkNotNull(player, "Tried to send data to client before the player exists.");
					packet.onPacketData(data, player);
				} catch (IOException e) {
					Log.error("Network Error", e);
				}
			});
		}
	}

	private static void checkThreadAndEnqueue(final IPacketHandlerServer packet, final PacketBufferEC data, final EntityPlayerMP player, IThreadListener threadListener) {
		if (!threadListener.isCallingFromMinecraftThread()) {
			threadListener.addScheduledTask(() -> {
				try {
					packet.onPacketData(data, player);
				} catch (IOException e) {
					Log.error("Network Error", e);
				}
			});
		}
	}

	private static EnumMap<Side, FMLEmbeddedChannel> channels;

	public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_ID+"_0");

	public static void registerMessages() {
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

		wrapper.registerMessage(HandlerFluidInterface.class,
				PacketFluidInterface.class, 7, Side.CLIENT);
		wrapper.registerMessage(HandlerFluidInterface.class,
				PacketFluidInterface.class, 7, Side.SERVER);
		registerClientPackets();
		registerServerPackets();
	}

	public static void registerServerPackets(){
		PacketId.FLUID_SLOT.registerHandler(new PacketFluidSlotSelect.Handler());
		PacketId.FLUID_CONTAINER_SLOT.registerHandler(new PacketFluidContainerSlot.Handler());
		PacketId.EXPORT_ORE.registerHandler(new PacketOreDictExport.HandlerServer());
		PacketId.TERMINAL_SELECT_FLUID.registerHandler(new PacketTerminalSelectFluidServer.Handler());
		PacketId.TERMINAL_OPEN_CONTAINER.registerHandler(new PacketTerminalOpenContainer.Handler());
		PacketId.STORAGE_OPEN_CONTAINER.registerHandler(new PacketStorageOpenContainer.Handler());
	}

	@SideOnly(Side.CLIENT)
	public static void registerClientPackets(){
		PacketId.FLUID_SLOT.registerHandler(new PacketFluidSlotUpdate.Handler());
		PacketId.EXPORT_ORE.registerHandler(new PacketOreDictExport.HandlerClient());
		PacketId.TERMINAL_UPDATE_FLUID.registerHandler(new PacketTerminalUpdateFluid.Handler());
		PacketId.TERMINAL_SELECT_FLUID.registerHandler(new PacketTerminalSelectFluidClient.Handler());
		PacketId.STORAGE_SELECT_FLUID.registerHandler(new PacketStorageSelectFluid.Handler());
		PacketId.STORAGE_UPDATE_FLUID.registerHandler(new PacketStorageUpdateFluid.Handler());
		PacketId.STORAGE_UPDATE_STATE.registerHandler(new PacketStorageUpdateState.Handler());
	}

	public static void sendPacketToAllPlayers(AbstractPacket packet) {
		wrapper.sendToAll(packet);
	}

	public static void sendPacketToAllPlayers(Packet packet, World world) {
		for (Object player : world.playerEntities) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).connection
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
}

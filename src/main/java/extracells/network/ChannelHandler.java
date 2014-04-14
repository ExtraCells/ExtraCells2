package extracells.network;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.relauncher.Side;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.other.PacketGui;
import extracells.network.packet.part.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.EnumMap;
import java.util.logging.Logger;

public class ChannelHandler extends FMLIndexedMessageToMessageCodec<AbstractPacket>
{
	private static EnumMap<Side, FMLEmbeddedChannel> channels;

	public ChannelHandler()
	{
		addDiscriminator(0, PacketFluidSlot.class);
		addDiscriminator(1, PacketFluidTerminal.class);
		addDiscriminator(2, PacketBusFluidIO.class);
		addDiscriminator(3, PacketBusFluidStorage.class);
		addDiscriminator(4, PacketFluidPlaneFormation.class);
		addDiscriminator(5, PacketFluidStorage.class);
		addDiscriminator(6, PacketFluidEmitter.class);
		addDiscriminator(7, PacketGui.class);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, AbstractPacket msg, ByteBuf target) throws Exception
	{
		msg.writePacketData(target);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, AbstractPacket msg)
	{
		try
		{
			msg.readPacketData(source);
			msg.execute();
		} catch (IOException e)
		{
			Logger.getLogger("ExtraCells").warning("Something caused a Protocol Exception!");
		}
	}

	public static void setChannels(EnumMap<Side, FMLEmbeddedChannel> _channels)
	{
		channels = _channels;
	}

	public static EnumMap<Side, FMLEmbeddedChannel> getChannels()
	{
		return channels;
	}

	public static void sendPacketToServer(AbstractPacket packet)
	{
		ChannelHandler.getChannels().get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		ChannelHandler.getChannels().get(Side.CLIENT).writeOutbound(packet);
	}

	public static void sendPacketToPlayer(AbstractPacket packet, EntityPlayer player)
	{
		ChannelHandler.getChannels().get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		ChannelHandler.getChannels().get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		ChannelHandler.getChannels().get(Side.SERVER).writeOutbound(packet);
	}

	public static void sendPacketToAllPlayers(AbstractPacket packet)
	{
		ChannelHandler.getChannels().get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		ChannelHandler.getChannels().get(Side.SERVER).writeOutbound(packet);
	}

	public static void sendPacketToAllPlayers(Packet packet, World world)
	{
		for (Object player : world.playerEntities)
		{
			if (player instanceof EntityPlayerMP)
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
		}
	}
}

package extracells.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.item.ItemPartECBase;
import extracells.network.packet.PacketFluidTerminal;
import extracells.part.PartFluidTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;

public abstract class AbstractPacket
{
	public static final String CHANNEL = "ExtraCells";
	private static final BiMap<Integer, Class<? extends AbstractPacket>> idMap;

	static
	{
		ImmutableBiMap.Builder<Integer, Class<? extends AbstractPacket>> builder = ImmutableBiMap.builder();

		builder.put(Integer.valueOf(ItemPartECBase.getPartId(PartFluidTerminal.class)), PacketFluidTerminal.class);
		idMap = builder.build();
	}

	public static AbstractPacket constructPacket(int packetId) throws ProtocolException, Throwable
	{
		Class<? extends AbstractPacket> clazz = idMap.get(Integer.valueOf(packetId));
		if (clazz == null)
		{
			throw new ProtocolException("Unknown Packet Id!");
		} else
		{
			return clazz.newInstance();
		}
	}

	@SuppressWarnings("serial")
	public static class ProtocolException extends Exception
	{

		public ProtocolException()
		{
		}

		public ProtocolException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public ProtocolException(String message)
		{
			super(message);
		}

		public ProtocolException(Throwable cause)
		{
			super(cause);
		}
	}

	public final int getPacketId()
	{
		if (idMap.inverse().containsKey(getClass()))
		{
			return idMap.inverse().get(getClass()).intValue();
		} else
		{
			throw new RuntimeException("Packet " + getClass().getSimpleName() + " is missing a mapping!");
		}
	}

	public final Packet makePacket()
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeByte(getPacketId());
		write(out);
		return PacketDispatcher.getPacket(CHANNEL, out.toByteArray());
	}

	public abstract void write(ByteArrayDataOutput out);

	public abstract void read(ByteArrayDataInput in) throws ProtocolException;

	public abstract void execute(EntityPlayer player, Side side) throws ProtocolException;
}
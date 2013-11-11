package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntitySolderingStation;

public class PacketSolderingStation extends AbstractPacket
{
	String playerName;
	int PacketType;
	int x, y, z;
	int deltaSize, deltaTypes, slotID;

	public static PacketSolderingStation changeSize(EntityPlayer player, int x, int y, int z, int deltaSize, int slotID)
	{
		PacketSolderingStation packet = new PacketSolderingStation();
		packet.playerName = player.username;
		packet.x = x;
		packet.y = y;
		packet.z = z;
		packet.deltaSize = deltaSize;
		packet.deltaTypes = 0;
		packet.slotID = slotID;
		return packet;
	}

	public static PacketSolderingStation changeTypes(EntityPlayer player, int x, int y, int z, int deltaTypes, int slotID)
	{
		PacketSolderingStation packet = new PacketSolderingStation();
		packet.playerName = player.username;
		packet.x = x;
		packet.y = y;
		packet.z = z;
		packet.deltaSize = 0;
		packet.deltaTypes = deltaTypes;
		packet.slotID = slotID;
		return packet;
	}

	public PacketSolderingStation()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeUTF(playerName);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(deltaSize);
		out.writeInt(deltaTypes);
		out.writeInt(slotID);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		playerName = in.readUTF();
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		deltaSize = in.readInt();
		deltaTypes = in.readInt();
		slotID = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntitySolderingStation tile = (TileEntitySolderingStation) player.worldObj.getBlockTileEntity(x, y, z);

			tile.changeStorage(player, slotID, deltaSize);
			tile.changeTypes(player, slotID, deltaTypes);
		}
	}
}

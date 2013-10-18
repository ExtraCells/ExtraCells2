package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntitySolderingStation;

public class PacketSolderingStation extends AbstractPacket
{
	String playerName;
	int PacketType;
	int x, y, z;
	int size, types;
	boolean remove;
	char upgrade, downgrade;

	public PacketSolderingStation(String playerName, int x, int y, int z, int size, int types, boolean remove, char upgrade, char downgrade)
	{
		this.playerName = playerName;
		this.x = x;
		this.y = y;
		this.z = z;
		this.size = size;
		this.types = types;
		this.remove = remove;
		this.upgrade = upgrade;
		this.downgrade = downgrade;
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
		out.writeInt(size);
		out.writeInt(types);
		out.writeBoolean(remove);
		out.writeChar(upgrade);
		out.writeChar(downgrade);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		playerName = in.readUTF();
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		int size = in.readInt();
		int types = in.readInt();
		Boolean remove = in.readBoolean();
		char upgrade = in.readChar();
		char downgrade = in.readChar();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntitySolderingStation tile = (TileEntitySolderingStation) player.worldObj.getBlockTileEntity(x, y, z);
			if (remove)
			{
				tile.remUser(playerName);
			} else
			{
				tile.updateData(playerName, size, types, upgrade, downgrade);
			}
		}
	}
}

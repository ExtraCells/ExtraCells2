package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityMEBattery;

public class PacketMEBattery extends AbstractPacket
{
	int x, y, z;
	String playername;

	public PacketMEBattery(int x, int y, int z, String playername)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.playername = playername;
	}

	public PacketMEBattery()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeUTF(playername);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		playername = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityMEBattery tile = (TileEntityMEBattery) player.worldObj.getBlockTileEntity(x, y, z);
			tile.updateGuiTile(playername);
		}
	}
}

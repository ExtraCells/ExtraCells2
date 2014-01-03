package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tileentity.TileEntityMEBattery;

public class PacketMEBattery extends AbstractPacket
{
	World world;
	int x, y, z;
	String playername;

	public PacketMEBattery(World world, int x, int y, int z, String playername)
	{
		this.world = world;
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
		out.writeInt(world.provider.dimensionId);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeUTF(playername);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
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
			TileEntityMEBattery tile = (TileEntityMEBattery) world.getBlockTileEntity(x, y, z);
			tile.updateGuiTile(playername);
		}
	}
}

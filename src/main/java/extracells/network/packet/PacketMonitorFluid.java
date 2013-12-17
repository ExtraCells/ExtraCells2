package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityTerminalFluid;

public class PacketMonitorFluid extends AbstractPacket
{
	int x, y, z;
	World world;
	int fluidID;

	public PacketMonitorFluid(World world, int x, int y, int z, int fluidID)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.fluidID = fluidID;
	}

	public PacketMonitorFluid()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(world.provider.dimensionId);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(fluidID);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		fluidID = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityTerminalFluid tile = (TileEntityTerminalFluid) world.getBlockTileEntity(x, y, z);
			tile.setCurrentFluid(fluidID);
		}
	}
}

package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tileentity.TileEntityBusFluidStorage;

public class PacketBusFluidStorage extends AbstractPacket
{
	World world;
	int x, y, z;
	int priority;

	public PacketBusFluidStorage(World world, int x, int y, int z, int priority)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.priority = priority;
	}

	public PacketBusFluidStorage()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(world.provider.dimensionId);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(priority);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		priority = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityBusFluidStorage tile = (TileEntityBusFluidStorage) world.getBlockTileEntity(x, y, z);
			tile.setPriority(priority);
		}
	}

}

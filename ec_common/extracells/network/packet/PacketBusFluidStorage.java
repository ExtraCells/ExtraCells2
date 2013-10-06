package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityBusFluidStorage;

public class PacketBusFluidStorage extends AbstractPacket
{
	int x, y, z;
	int priority;

	public PacketBusFluidStorage(int x, int y, int z, int priority)
	{
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
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(priority);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
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
			TileEntityBusFluidStorage tile = (TileEntityBusFluidStorage) player.worldObj.getBlockTileEntity(x, y, z);
			tile.setPriority(priority);
		}
	}

}

package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityTerminalFluid;

public class PacketMonitorFluid extends AbstractPacket
{
	int x, y, z;
	int action;

	public PacketMonitorFluid(int x, int y, int z, int action)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.action = action;
	}

	public PacketMonitorFluid()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(action);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		action = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityTerminalFluid tile = (TileEntityTerminalFluid) player.worldObj.getBlockTileEntity(x, y, z);
			int currentFluid = tile.getCurrentFluid();
			switch (action)
			{
			case 0:
				tile.setCurrentFluid(-1);
				break;
			case 1:
				tile.setCurrentFluid(1);
				break;
			}
		}
	}
}

package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.config.RedstoneModeInput;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityBusFluidExport;

public class PacketBusFluidExport extends AbstractPacket
{
	int x, y, z;
	String playername;
	int action;

	public PacketBusFluidExport(int x, int y, int z, int action, String playername)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.playername = playername;
		this.action = action;
	}

	public PacketBusFluidExport()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeUTF(playername);
		out.writeInt(action);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		playername = in.readUTF();
		action = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityBusFluidExport tile = (TileEntityBusFluidExport) player.worldObj.getBlockTileEntity(x, y, z);
			switch (action)
			{
			case 0:
				PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
				break;
			case 1:
				if (tile.getRedstoneAction().ordinal() >= 3)
				{

					tile.setRedstoneAction(RedstoneModeInput.values()[0]);
				} else
				{
					tile.setRedstoneAction(RedstoneModeInput.values()[tile.getRedstoneAction().ordinal() + 1]);
				}
				PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
				break;
			}
		}
	}
}

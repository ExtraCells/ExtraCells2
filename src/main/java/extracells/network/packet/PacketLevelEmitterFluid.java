package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import appeng.api.config.RedstoneModeInput;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tile.TileEntityLevelEmitterFluid;

public class PacketLevelEmitterFluid extends AbstractPacket
{
	World world;
	int x, y, z;
	long filterAmount;
	int type;

	public PacketLevelEmitterFluid(World world, int x, int y, int z, long filterAmount)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.filterAmount = filterAmount;
		this.type = 0;
	}

	public PacketLevelEmitterFluid(World world, int x, int y, int z)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = 1;
	}

	public PacketLevelEmitterFluid()
	{
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeInt(world.provider.dimensionId);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeLong(filterAmount);
		out.writeInt(type);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		filterAmount = in.readLong();
		type = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityLevelEmitterFluid tile = (TileEntityLevelEmitterFluid) world.getBlockTileEntity(x, y, z);
			switch (type)
			{
			case 0:
				tile.setAmount(filterAmount);
				break;
			case 1:
				switch (tile.getRedstoneAction())
				{
				case WhenOff:
					tile.setRedstoneAction(RedstoneModeInput.WhenOn);
					break;
				case WhenOn:
					tile.setRedstoneAction(RedstoneModeInput.WhenOff);
					break;
				default:
					break;
				}
				break;
			}
		}
	}
}

package extracells.network.packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.relauncher.Side;
import extracells.network.AbstractPacket;
import extracells.tileentity.TileEntityTerminalFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidStack;

public class PacketTerminalFluid extends AbstractPacket
{

	World world;
	int x, y, z;
	int fluidID, amount;
	String playerName;

	public PacketTerminalFluid(World world, int x, int y, int z, FluidStack stack)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		fluidID = stack.fluidID;
		amount = stack.amount;
	}

	public PacketTerminalFluid()
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
		out.writeInt(amount);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		fluidID = in.readInt();
		amount = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		if (side.isServer())
		{
			TileEntityTerminalFluid tile = (TileEntityTerminalFluid) world.getBlockTileEntity(x, y, z);
			tile.requestFluid(new FluidStack(fluidID, amount));
		}
	}
}

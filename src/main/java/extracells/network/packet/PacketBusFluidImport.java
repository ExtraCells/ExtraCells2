package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import appeng.api.config.RedstoneModeInput;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.gui.widget.WidgetFluidModes.FluidMode;
import extracells.network.AbstractPacket;
import extracells.tileentity.TileEntityBusFluidImport;

public class PacketBusFluidImport extends AbstractPacket
{
	World world;
	int x, y, z;
	String playername;
	int action;

	public PacketBusFluidImport(World world, int x, int y, int z, int action, String playername)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.playername = playername;
		this.action = action;
	}

	public PacketBusFluidImport()
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
		out.writeInt(action);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		world = DimensionManager.getWorld(in.readInt());
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
			TileEntityBusFluidImport tile = (TileEntityBusFluidImport) world.getBlockTileEntity(x, y, z);
			switch (action)
			{
			case 0:
				if (tile != null)
					PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
				break;
			case 1:
				if (tile.getRedstoneMode().ordinal() >= 3)
				{

					tile.setRedstoneMode(RedstoneModeInput.values()[0]);
				} else
				{
					tile.setRedstoneMode(RedstoneModeInput.values()[tile.getRedstoneMode().ordinal() + 1]);
				}
				if (tile != null)
					PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
				break;
			case 2:
				if (tile.getFluidMode().ordinal() >= 2)
				{

					tile.setFluidMode(FluidMode.values()[0]);
				} else
				{
					tile.setFluidMode(FluidMode.values()[tile.getFluidMode().ordinal() + 1]);
				}
				if (tile != null)
					PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
				break;
			}
		}
	}
}

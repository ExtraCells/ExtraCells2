package extracells.network;

import appeng.api.parts.IPartHost;
import com.google.common.base.Charsets;
import extracells.part.PartECBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;

public abstract class AbstractPacket
{
	protected EntityPlayer player;
	protected byte mode;

	public AbstractPacket(EntityPlayer _player)
	{
		player = _player;
	}

	@SuppressWarnings("unused")
	public AbstractPacket()
	{
		player = null;
	}

	public void writePacketData(ByteBuf out) throws IOException
	{
		out.writeByte(mode);
		writePlayer(player, out);
	}

	public void readPacketData(ByteBuf in) throws IOException
	{
		mode = in.readByte();
		player = readPlayer(in);
	}

	public abstract void execute();

	public String readString(ByteBuf in)
	{
		byte[] stringBytes = new byte[in.readInt()];
		in.readBytes(stringBytes);
		return new String(stringBytes, Charsets.UTF_8);
	}

	public void writeString(String string, ByteBuf out)
	{
		byte[] stringBytes;
		stringBytes = string.getBytes(Charsets.UTF_8);
		out.writeInt(stringBytes.length);
		out.writeBytes(stringBytes);
	}

	public World readWorld(ByteBuf in)
	{
		return DimensionManager.getWorld(in.readInt());
	}

	public void writeWorld(World world, ByteBuf out)
	{
		out.writeInt(world.provider.dimensionId);
	}

	public EntityPlayer readPlayer(ByteBuf in)
	{
		if (!in.readBoolean())
			return null;
		World playerWorld = readWorld(in);
		return playerWorld.getPlayerEntityByName(readString(in));
	}

	public void writePlayer(EntityPlayer player, ByteBuf out)
	{
		if (player == null)
		{
			out.writeBoolean(false);
			return;
		}
		out.writeBoolean(true);
		writeWorld(player.worldObj, out);
		writeString(player.getCommandSenderName(), out);
	}

	public TileEntity readTileEntity(ByteBuf in)
	{
		return readWorld(in).getTileEntity(in.readInt(), in.readInt(), in.readInt());
	}

	public void writeTileEntity(TileEntity tileEntity, ByteBuf out)
	{
		writeWorld(tileEntity.getWorldObj(), out);
		out.writeInt(tileEntity.xCoord);
		out.writeInt(tileEntity.yCoord);
		out.writeInt(tileEntity.zCoord);
	}

	public PartECBase readPart(ByteBuf in)
	{
		return (PartECBase) ((IPartHost) readTileEntity(in)).getPart(ForgeDirection.getOrientation(in.readByte()));
	}

	public void writePart(PartECBase part, ByteBuf out)
	{
		writeTileEntity(part.getHost().getTile(), out);
		out.writeByte(part.getSide().ordinal());
	}

	public Fluid readFluid(ByteBuf in)
	{
		return FluidRegistry.getFluid(readString(in));
	}

	public void writeFluid(Fluid fluid, ByteBuf out)
	{
		if (fluid == null)
		{
			writeString("", out);
			return;
		}
		writeString(fluid.getName(), out);
	}

	public void sendPacketToServer()
	{
		ChannelHandler.sendPacketToServer(this);
	}

	public void sendPacketToPlayer(EntityPlayer player)
	{
		ChannelHandler.sendPacketToPlayer(this, player);
	}

	public void sendPacketToAllPlayers()
	{
		ChannelHandler.sendPacketToAllPlayers(this);
	}
}

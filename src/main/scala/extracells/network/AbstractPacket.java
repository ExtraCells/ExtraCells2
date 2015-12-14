package extracells.network;

import appeng.api.parts.IPartHost;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.part.PartECBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public abstract class AbstractPacket implements IMessage {

	@SideOnly(Side.CLIENT)
	public static World getClientWorld() {
		return net.minecraft.client.Minecraft.getMinecraft().theWorld;
	}

	public static Fluid readFluid(ByteBuf in) {
		return FluidRegistry.getFluid(readString(in));
	}

	public static PartECBase readPart(ByteBuf in) {
		return (PartECBase) ((IPartHost) readTileEntity(in))
				.getPart(ForgeDirection.getOrientation(in.readByte()));
	}

	public static EntityPlayer readPlayer(ByteBuf in) {
		if (!in.readBoolean()) {
			return null;
		}
		World playerWorld = readWorld(in);
		return playerWorld.getPlayerEntityByName(readString(in));
	}

	public static String readString(ByteBuf in) {
		byte[] stringBytes = new byte[in.readInt()];
		in.readBytes(stringBytes);
		return new String(stringBytes, Charsets.UTF_8);
	}

	public static TileEntity readTileEntity(ByteBuf in) {
		return readWorld(in).getTileEntity(in.readInt(), in.readInt(),
				in.readInt());
	}

	public static World readWorld(ByteBuf in) {
		WorldServer world = DimensionManager.getWorld(in.readInt());
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			return world != null ? world : getClientWorld();
		}
		return world;
	}

	public static void writeFluid(Fluid fluid, ByteBuf out) {
		if (fluid == null) {
			writeString("", out);
			return;
		}
		writeString(fluid.getName(), out);
	}

	public static void writePart(PartECBase part, ByteBuf out) {
		writeTileEntity(part.getHost().getTile(), out);
		out.writeByte(part.getSide().ordinal());
	}

	public static void writePlayer(EntityPlayer player, ByteBuf out) {
		if (player == null) {
			out.writeBoolean(false);
			return;
		}
		out.writeBoolean(true);
		writeWorld(player.worldObj, out);
		writeString(player.getCommandSenderName(), out);
	}

	public static void writeString(String string, ByteBuf out) {
		byte[] stringBytes;
		stringBytes = string.getBytes(Charsets.UTF_8);
		out.writeInt(stringBytes.length);
		out.writeBytes(stringBytes);
	}

	public static void writeTileEntity(TileEntity tileEntity, ByteBuf out) {
		writeWorld(tileEntity.getWorldObj(), out);
		out.writeInt(tileEntity.xCoord);
		out.writeInt(tileEntity.yCoord);
		out.writeInt(tileEntity.zCoord);
	}

	public static void writeWorld(World world, ByteBuf out) {
		out.writeInt(world.provider.dimensionId);
	}

	protected EntityPlayer player;

	protected byte mode;

	@SuppressWarnings("unused")
	public AbstractPacket() {
		this.player = null;
	}

	public AbstractPacket(EntityPlayer _player) {
		this.player = _player;
	}

	public abstract void execute();

	@Override
	public void fromBytes(ByteBuf in) {
		this.mode = in.readByte();
		this.player = readPlayer(in);
		readData(in);
	}

	public abstract void readData(ByteBuf in);

	public void sendPacketToAllPlayers() {
		ChannelHandler.sendPacketToAllPlayers(this);
	}

	public void sendPacketToPlayer(EntityPlayer player) {
		ChannelHandler.sendPacketToPlayer(this, player);
	}

	public void sendPacketToPlayersAround(NetworkRegistry.TargetPoint point) {
		ChannelHandler.sendPacketToPlayersAround(this, point);
	}

	public void sendPacketToServer() {
		ChannelHandler.sendPacketToServer(this);
	}

	@Override
	public void toBytes(ByteBuf out) {
		out.writeByte(this.mode);
		writePlayer(this.player, out);
		writeData(out);
	}

	public abstract void writeData(ByteBuf out);
}

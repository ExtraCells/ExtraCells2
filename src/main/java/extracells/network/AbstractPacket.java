package extracells.network;

import appeng.api.parts.IPartHost;
import com.google.common.base.Charsets;
import extracells.part.PartECBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;

public abstract class AbstractPacket {

    protected EntityPlayer player;
    protected byte mode;

    public AbstractPacket(EntityPlayer _player) {
        player = _player;
    }

    @SuppressWarnings("unused")
    public AbstractPacket() {
        player = null;
    }

    public void writePacketData(ByteBuf out) throws IOException {
        out.writeByte(mode);
        writePlayer(player, out);
        writeData(out);
    }

    public abstract void writeData(ByteBuf out) throws IOException;

    public void readPacketData(ByteBuf in) throws IOException {
        mode = in.readByte();
        player = readPlayer(in);
        readData(in);
    }

    public abstract void readData(ByteBuf in) throws IOException;

    public abstract void execute();

    public static String readString(ByteBuf in) throws IOException {
        byte[] stringBytes = new byte[in.readInt()];
        in.readBytes(stringBytes);
        return new String(stringBytes, Charsets.UTF_8);
    }

    public static void writeString(String string, ByteBuf out) throws IOException {
        byte[] stringBytes;
        stringBytes = string.getBytes(Charsets.UTF_8);
        out.writeInt(stringBytes.length);
        out.writeBytes(stringBytes);
    }

    public static World readWorld(ByteBuf in) throws IOException {
        World world = DimensionManager.getWorld(in.readInt());
        return world != null ? world : Minecraft.getMinecraft().theWorld;
    }

    public static void writeWorld(World world, ByteBuf out) throws IOException {
        out.writeInt(world.provider.dimensionId);
    }

    public static EntityPlayer readPlayer(ByteBuf in) throws IOException {
        if (!in.readBoolean())
            return null;
        World playerWorld = readWorld(in);
        return playerWorld.getPlayerEntityByName(readString(in));
    }

    public static void writePlayer(EntityPlayer player, ByteBuf out) throws IOException {
        if (player == null) {
            out.writeBoolean(false);
            return;
        }
        out.writeBoolean(true);
        writeWorld(player.worldObj, out);
        writeString(player.getCommandSenderName(), out);
    }

    public static TileEntity readTileEntity(ByteBuf in) throws IOException {
        return readWorld(in).getTileEntity(in.readInt(), in.readInt(), in.readInt());
    }

    public static void writeTileEntity(TileEntity tileEntity, ByteBuf out) throws IOException {
        writeWorld(tileEntity.getWorldObj(), out);
        out.writeInt(tileEntity.xCoord);
        out.writeInt(tileEntity.yCoord);
        out.writeInt(tileEntity.zCoord);
    }

    public static PartECBase readPart(ByteBuf in) throws IOException {
        return (PartECBase) ((IPartHost) readTileEntity(in)).getPart(ForgeDirection.getOrientation(in.readByte()));
    }

    public static void writePart(PartECBase part, ByteBuf out) throws IOException {
        writeTileEntity(part.getHost().getTile(), out);
        out.writeByte(part.getSide().ordinal());
    }

    public static Fluid readFluid(ByteBuf in) throws IOException {
        return FluidRegistry.getFluid(readString(in));
    }

    public static void writeFluid(Fluid fluid, ByteBuf out) throws IOException {
        if (fluid == null) {
            writeString("", out);
            return;
        }
        writeString(fluid.getName(), out);
    }

    public void sendPacketToServer() {
        ChannelHandler.sendPacketToServer(this);
    }

    public void sendPacketToPlayer(EntityPlayer player) {
        ChannelHandler.sendPacketToPlayer(this, player);
    }

    public void sendPacketToAllPlayers() {
        ChannelHandler.sendPacketToAllPlayers(this);
    }
}

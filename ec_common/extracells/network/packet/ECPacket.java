package extracells.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import extracells.network.PacketHandler;

/**
 * 
 * ExtraCells
 * 
 * ECPacket
 * 
 * @author PaleoCrafter, diesieben07
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
public abstract class ECPacket {
    private static final BiMap<Integer, Class<? extends ECPacket>> idMap;

    static {
        ImmutableBiMap.Builder<Integer, Class<? extends ECPacket>> builder = ImmutableBiMap
                .builder();

        builder.put(Integer.valueOf(0), SolderingPacket.class);

        idMap = builder.build();
    }

    public static ECPacket constructPacket(int packetId)
            throws ProtocolException, ReflectiveOperationException {
        Class<? extends ECPacket> clazz = idMap.get(Integer.valueOf(packetId));
        if (clazz == null) {
            throw new ProtocolException("Unknown Packet Id!");
        } else {
            return clazz.newInstance();
        }
    }

    @SuppressWarnings("serial")
    public static class ProtocolException extends Exception {

        public ProtocolException() {
        }

        public ProtocolException(String message, Throwable cause) {
            super(message, cause);
        }

        public ProtocolException(String message) {
            super(message);
        }

        public ProtocolException(Throwable cause) {
            super(cause);
        }
    }

    public final int getPacketId() {
        if (idMap.inverse().containsKey(getClass())) {
            return idMap.inverse().get(getClass()).intValue();
        } else {
            throw new RuntimeException("Packet " + getClass().getSimpleName()
                    + " is missing a mapping!");
        }
    }

    public final Packet makePacket() throws IllegalArgumentException {
        if (PacketHandler.CHANNEL_NAME != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(getPacketId());
            write(out);
            return PacketDispatcher.getPacket(PacketHandler.CHANNEL_NAME,
                    out.toByteArray());
        }
        throw new IllegalArgumentException(
                "You have to define a channel for the MMOMats packets first!");
    }

    public abstract void write(ByteArrayDataOutput out);

    public abstract void read(ByteArrayDataInput in);

    public abstract void execute(EntityPlayer player, Side side);
}

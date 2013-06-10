package extracells.network;

import java.util.logging.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import extracells.network.packet.ECPacket;
import extracells.network.packet.ECPacket.ProtocolException;

/**
 * 
 * ExtraCells
 * 
 * PacketHandler
 * 
 * @author PaleoCrafter
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
public class PacketHandler implements IPacketHandler {
    public static final String CHANNEL_NAME = "ExtraCells";

    @Override
    public void onPacketData(INetworkManager manager,
            Packet250CustomPayload packet, Player player) {
        try {
            EntityPlayer entityPlayer = (EntityPlayer) player;
            ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
            int packetId = in.readUnsignedByte();
            ECPacket paleoPacket = ECPacket.constructPacket(packetId);
            paleoPacket.read(in);
            paleoPacket.execute(entityPlayer,
                    entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
        } catch (ProtocolException e) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).playerNetServerHandler
                        .kickPlayerFromServer("Protocol Exception!");
                Logger.getLogger("ExtraCells").warning(
                        "Player " + ((EntityPlayer) player).username
                                + " caused a Protocol Exception!");
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Unexpected Reflection exception during Packet construction!",
                    e);
        }
    }
}

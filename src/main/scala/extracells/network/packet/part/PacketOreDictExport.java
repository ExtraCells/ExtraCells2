package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiOreDictExport;
import extracells.network.IPacketHandlerClient;
import extracells.network.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.PacketBufferEC;
import extracells.network.packet.PacketId;

public class PacketOreDictExport extends Packet {

	private String filter;

	public PacketOreDictExport(String filter) {
		this.filter = filter;
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.EXPORT_ORE;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		data.writeString(filter);
	}

	@SideOnly(Side.CLIENT)
	public static class HandlerClient implements IPacketHandlerClient{
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			String filter = data.readString();
			GuiOreDictExport.updateFilter(filter);
		}
	}

	public static class HandlerServer implements IPacketHandlerServer{
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			String filter = data.readString();
			Container con = player.openContainer;
			if (con != null && con instanceof ContainerOreDictExport) {
				ContainerOreDictExport c = (ContainerOreDictExport) con;
				c.part.filter = filter;
			}
		}
	}
}

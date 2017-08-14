package extracells.network.packet.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiOreDictExport;
import extracells.network.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class PacketOreDictExport extends AbstractPacket {

	private String filter;
	private Side side;

	public PacketOreDictExport() {}

	public PacketOreDictExport(EntityPlayer _player, String filter, Side side) {
		super(_player);
		this.mode = 0;
		this.filter = filter;
		this.side = side;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			if (this.side.isClient())
				try {
					handleClient();
				} catch (Throwable e) {}
			else
				handleServer();
			break;
		}
	}

	@SideOnly(Side.CLIENT)
	private void handleClient() {
		GuiOreDictExport.updateFilter(this.filter);
	}

	private void handleServer() {
		Container con = this.player.openContainer;
		if (con != null && con instanceof ContainerOreDictExport) {
			ContainerOreDictExport c = (ContainerOreDictExport) con;
			c.part.filter = this.filter;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			if (in.readBoolean())
				this.side = Side.SERVER;
			else
				this.side = Side.CLIENT;
			this.filter = ByteBufUtils.readUTF8String(in);
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			out.writeBoolean(this.side.isServer());
			ByteBufUtils.writeUTF8String(out, this.filter);
			break;

		}
	}
}

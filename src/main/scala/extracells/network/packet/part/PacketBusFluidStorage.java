package extracells.network.packet.part;

import appeng.api.config.AccessRestriction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.GuiBusFluidStorage;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class PacketBusFluidStorage extends AbstractPacket {

	PartFluidStorage part;
	AccessRestriction access;

	public PacketBusFluidStorage() {}

	public PacketBusFluidStorage(EntityPlayer _player,
			AccessRestriction _access, boolean toClient) {
		super(_player);
		if (toClient)
			this.mode = 1;
		else
			this.mode = 2;
		this.access = _access;
	}

	public PacketBusFluidStorage(EntityPlayer _player, PartFluidStorage _part) {
		super(_player);
		this.mode = 0;
		this.player = _player;
		this.part = _part;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			this.part.sendInformation(this.player);
			break;
		case 1:
			try {
				handleClient();
			} catch (Throwable e) {}
			break;
		case 2:
			Container con = this.player.openContainer;
			if (con != null && con instanceof ContainerBusFluidStorage) {
				((ContainerBusFluidStorage) con).part.updateAccess(this.access);
				new PacketBusFluidStorage(this.player, this.access, true)
						.sendPacketToPlayer(this.player);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	void handleClient() {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (screen != null && screen instanceof GuiBusFluidStorage) {
			((GuiBusFluidStorage) screen).updateAccessRestriction(this.access);
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.part = (PartFluidStorage) readPart(in);
			break;
		case 1:
		case 2:
			this.access = AccessRestriction.valueOf(readString(in));
		}

	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			writePart(this.part, out);
			break;
		case 1:
		case 2:
			writeString(this.access.name(), out);
		}

	}
}

package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.gui.GuiStorage;
import extracells.network.IPacketHandlerClient;
import extracells.network.PacketBufferEC;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketId;
import extracells.util.GuiUtil;

public class PacketStorageSelectFluid extends Packet {
	Fluid fluid;

	public PacketStorageSelectFluid(Fluid fluid) {
		this.fluid = fluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writeFluid(fluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.STORAGE_SELECT_FLUID;
	}

	@SideOnly(Side.CLIENT)
	public static class Handler implements IPacketHandlerClient{
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			Fluid fluid = data.readFluid();
			GuiStorage guiTerminal = GuiUtil.getGui(GuiStorage.class);
			if(fluid == null || guiTerminal == null){
				return;
			}

			guiTerminal.receiveSelectedFluid(fluid);
		}
	}
}

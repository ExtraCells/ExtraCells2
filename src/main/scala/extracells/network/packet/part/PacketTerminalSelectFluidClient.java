package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.Fluid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.gui.GuiTerminal;
import extracells.network.packet.IPacketHandlerClient;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.util.GuiUtil;

public class PacketTerminalSelectFluidClient extends Packet {
	Fluid fluid;

	public PacketTerminalSelectFluidClient(Fluid fluid) {
		this.fluid = fluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writeFluid(fluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.TERMINAL_SELECT_FLUID;
	}

	@SideOnly(Side.CLIENT)
	public static class Handler implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			Fluid fluid = data.readFluid();
			GuiTerminal guiTerminal = GuiUtil.getGui(GuiTerminal.class);
			if (fluid == null || guiTerminal == null) {
				return;
			}

			guiTerminal.receiveSelectedFluid(fluid);
		}
	}
}

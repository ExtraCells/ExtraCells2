package extracells.network.packet.other;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fluids.Fluid;

import extracells.network.packet.IPacketHandlerClient;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;

public class PacketFluidSlotUpdate extends Packet {

	private List<Fluid> filterFluids;


	public PacketFluidSlotUpdate(List<Fluid> filterFluids) {
		this.filterFluids = filterFluids;
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.FLUID_SLOT;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		data.writeInt(this.filterFluids.size());
		for (int i = 0; i < this.filterFluids.size(); i++) {
			data.writeFluid(this.filterFluids.get(i));
		}
	}

	public static class Handler implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			List<Fluid> filterFluids = new LinkedList<>();
			int size = data.readInt();
			for (int i = 0; i < size; i++) {
				filterFluids.add(data.readFluid());
			}
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof IFluidSlotGui) {
				IFluidSlotGui partGui = (IFluidSlotGui) gui;
				partGui.updateFluids(filterFluids);
			}
		}
	}
}

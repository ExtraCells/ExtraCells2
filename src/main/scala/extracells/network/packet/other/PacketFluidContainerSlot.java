package extracells.network.packet.other;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import extracells.network.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.tileentity.TileEntityFluidFiller;

public class PacketFluidContainerSlot extends Packet {

	private ItemStack container;
	private TileEntityFluidFiller fluidFiller;

	public PacketFluidContainerSlot(TileEntityFluidFiller fluidFiller, ItemStack container) {
		this.fluidFiller = fluidFiller;
		this.container = container;
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.FLUID_CONTAINER_SLOT;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		data.writeTile(this.fluidFiller);
		data.writeItemStackToBuffer(this.container);
	}

	public static class Handler implements IPacketHandlerServer{
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			TileEntityFluidFiller fluidFiller = data.readTile(player.worldObj, TileEntityFluidFiller.class);
			ItemStack container = data.readItemStackFromBuffer();

			if(fluidFiller == null){
				return;
			}

			container.stackSize = 1;
			fluidFiller.containerItem = container;
			if (fluidFiller.hasWorldObj()) {
				fluidFiller.updateBlock();
			}
			fluidFiller.postUpdateEvent();
		}
	}
}

package extracells.network.packet.other;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.Fluid;

import extracells.network.packet.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.part.PartECBase;

public class PacketFluidSlotSelect extends Packet {

	private int index;
	private Fluid fluid;
	private IFluidSlotPartOrBlock partOrBlock;

	public PacketFluidSlotSelect(IFluidSlotPartOrBlock partOrBlock, int index, Fluid fluid) {
		this.partOrBlock = partOrBlock;
		this.index = index;
		this.fluid = fluid;
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.FLUID_SLOT;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		if (this.partOrBlock instanceof PartECBase) {
			data.writeBoolean(true);
			data.writePart((PartECBase) this.partOrBlock);
		} else {
			data.writeBoolean(false);
			data.writeTile((TileEntity) this.partOrBlock);
		}
		data.writeVarIntToBuffer(this.index);
		data.writeFluid(this.fluid);
	}

	public static class Handler implements IPacketHandlerServer{
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			IFluidSlotPartOrBlock partOrBlock;

			if (data.readBoolean()) {
				partOrBlock = data.readPart(player.worldObj);
			}else {
				partOrBlock = data.readTile(player.worldObj, IFluidSlotPartOrBlock.class);
			}
			int index = data.readVarIntFromBuffer();
			Fluid fluid = data.readFluid();
			if(partOrBlock == null){
				return;
			}
			partOrBlock.setFluid(index, fluid, player);
		}
	}
}

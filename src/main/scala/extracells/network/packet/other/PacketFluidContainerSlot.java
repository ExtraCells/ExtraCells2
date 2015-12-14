package extracells.network.packet.other;

import cpw.mods.fml.common.network.ByteBufUtils;
import extracells.network.AbstractPacket;
import extracells.tileentity.TileEntityFluidFiller;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketFluidContainerSlot extends AbstractPacket {

	private ItemStack container;
	private TileEntityFluidFiller fluidFiller;

	public PacketFluidContainerSlot() {}

	public PacketFluidContainerSlot(TileEntityFluidFiller _fluidFiller,
			ItemStack _container, EntityPlayer _player) {
		super(_player);
		this.mode = 0;
		this.fluidFiller = _fluidFiller;
		this.container = _container;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			this.container.stackSize = 1;
			this.fluidFiller.containerItem = this.container;
			if (this.fluidFiller.hasWorldObj())
				this.fluidFiller.getWorldObj().markBlockForUpdate(
						this.fluidFiller.xCoord, this.fluidFiller.yCoord,
						this.fluidFiller.zCoord);
			this.fluidFiller.postUpdateEvent();
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			this.fluidFiller = (TileEntityFluidFiller) readTileEntity(in);
			this.container = ByteBufUtils.readItemStack(in);
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			writeTileEntity(this.fluidFiller, out);
			ByteBufUtils.writeItemStack(out, this.container);
			break;
		}
	}
}

package extracells.network.packet.other;

import extracells.network.AbstractPacket;
import extracells.part.PartECBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;

public class PacketFluidSlot extends AbstractPacket {

	private int index;
	private Fluid fluid;
	private IFluidSlotPartOrBlock partOrBlock;
	private List<Fluid> filterFluids;

	public PacketFluidSlot() {}

	public PacketFluidSlot(IFluidSlotPartOrBlock _partOrBlock, int _index,
			Fluid _fluid, EntityPlayer _player) {
		super(_player);
		this.mode = 0;
		this.partOrBlock = _partOrBlock;
		this.index = _index;
		this.fluid = _fluid;
	}

	public PacketFluidSlot(List<Fluid> _filterFluids) {
		this.mode = 1;
		this.filterFluids = _filterFluids;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			this.partOrBlock.setFluid(this.index, this.fluid, this.player);
			break;
		case 1:
			Gui gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof IFluidSlotGui) {
				IFluidSlotGui partGui = (IFluidSlotGui) gui;
				partGui.updateFluids(this.filterFluids);
			}
			break;
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			if (in.readBoolean())
				this.partOrBlock = (IFluidSlotPartOrBlock) readPart(in);
			else
				this.partOrBlock = (IFluidSlotPartOrBlock) readTileEntity(in);
			this.index = in.readInt();
			this.fluid = readFluid(in);
			break;
		case 1:
			this.filterFluids = new ArrayList<Fluid>();
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				this.filterFluids.add(readFluid(in));
			}
			break;
		}
	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
			if (this.partOrBlock instanceof PartECBase) {
				out.writeBoolean(true);
				writePart((PartECBase) this.partOrBlock, out);
			} else {
				out.writeBoolean(false);
				writeTileEntity((TileEntity) this.partOrBlock, out);
			}
			out.writeInt(this.index);
			writeFluid(this.fluid, out);
			break;
		case 1:
			out.writeInt(this.filterFluids.size());
			for (int i = 0; i < this.filterFluids.size(); i++) {
				writeFluid(this.filterFluids.get(i), out);
			}
			break;
		}
	}
}

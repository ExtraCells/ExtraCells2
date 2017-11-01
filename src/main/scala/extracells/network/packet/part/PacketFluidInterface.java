package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEPartLocation;
import extracells.container.fluid.ContainerFluidInterface;
import extracells.gui.fluid.GuiFluidInterface;
import extracells.network.packet.IPacketHandlerClient;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.util.GuiUtil;

public class PacketFluidInterface extends Packet {

	FluidStack[] tank;
	String[] filter;

	public PacketFluidInterface(FluidStack[] tank, String[] filter) {
		this.tank = tank;
		this.filter = filter;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("lengthTank", this.tank.length);
		for (int i = 0; i < this.tank.length; i++) {
			if (this.tank[i] != null) {
				tag.setTag("tank#" + i,
					this.tank[i].writeToNBT(new NBTTagCompound()));
			}
		}
		tag.setInteger("lengthFilter", this.filter.length);
		for (int i = 0; i < this.filter.length; i++) {
			if (this.filter[i] != null) {
				tag.setString("filter#" + i, this.filter[i]);
			}
		}
		data.writeCompoundTag(tag);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.FLUID_INTERFACE;
	}

	@SideOnly(Side.CLIENT)
	public static class Handler implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			NBTTagCompound tag = data.readCompoundTag();
			FluidStack[] tank = new FluidStack[tag.getInteger("lengthTank")];
			for (int i = 0; i < tank.length; i++) {
				if (tag.hasKey("tank#" + i)) {
					tank[i] = FluidStack.loadFluidStackFromNBT(tag
						.getCompoundTag("tank#" + i));
				} else {
					tank[i] = null;
				}
			}
			String[] filter = new String[tag.getInteger("lengthFilter")];
			for (int i = 0; i < filter.length; i++) {
				if (tag.hasKey("filter#" + i)) {
					filter[i] = tag.getString("filter#" + i);
				} else {
					filter[i] = "";
				}
			}

			GuiFluidInterface gui = GuiUtil.getGui(GuiFluidInterface.class);
			ContainerFluidInterface container = GuiUtil.getContainer(gui, ContainerFluidInterface.class);
			if (container == null) {
				return;
			}
			for (int i = 0; i < tank.length; i++) {
				container.fluidInterface.setFluidTank(
					AEPartLocation.fromOrdinal(i), tank[i]);
			}
			for (int i = 0; i < filter.length; i++) {
				if (gui.filter[i] != null) {
					gui.filter[i].setFluid(FluidRegistry
						.getFluid(filter[i]));
				}
			}
		}
	}

}

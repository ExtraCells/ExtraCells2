package extracells.network.packet.part;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEPartLocation;
import extracells.container.ContainerFluidInterface;
import extracells.gui.GuiFluidInterface;
import extracells.network.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class PacketFluidInterface extends AbstractPacket {

	FluidStack[] tank;
	String[] filter;
	String fluidName;
	int filterSlot;

	public PacketFluidInterface() {}

	public PacketFluidInterface(FluidStack[] tank, String[] filter, EntityPlayer player) {
		super(player);
		this.mode = 0;
		this.tank = tank;
		this.filter = filter;
	}

	public PacketFluidInterface(String fluidName, int filterSlot, EntityPlayer player) {
		super(player);
		this.mode = 1;
		this.fluidName = fluidName;
		this.filterSlot = filterSlot;
	}

	@Override
	public void execute() {
		switch (this.mode) {
		case 0:
			mode0();
			break;
		case 1:
			if (this.player.openContainer != null && this.player.openContainer instanceof ContainerFluidInterface) {
				ContainerFluidInterface container = (ContainerFluidInterface) this.player.openContainer;
				container.fluidInterface.setFilter(
						AEPartLocation.fromOrdinal(this.filterSlot),
						FluidRegistry.getFluid(this.fluidName));
			}
			break;
		default:
		}

	}

	@SideOnly(Side.CLIENT)
	private void mode0() {
		EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		if (p.openContainer != null
				&& p.openContainer instanceof ContainerFluidInterface) {
			ContainerFluidInterface container = (ContainerFluidInterface) p.openContainer;
			if (Minecraft.getMinecraft().currentScreen != null
					&& Minecraft.getMinecraft().currentScreen instanceof GuiFluidInterface) {
				GuiFluidInterface gui = (GuiFluidInterface) Minecraft
						.getMinecraft().currentScreen;
				for (int i = 0; i < this.tank.length; i++) {
					container.fluidInterface.setFluidTank(
							AEPartLocation.fromOrdinal(i), this.tank[i]);
				}
				for (int i = 0; i < this.filter.length; i++) {
					if (gui.filter[i] != null)
						gui.filter[i].setFluid(FluidRegistry
								.getFluid(this.filter[i]));
				}
			}
		}
	}

	@Override
	public void readData(ByteBuf in) {
		switch (this.mode) {
		case 0:
			NBTTagCompound tag = ByteBufUtils.readTag(in);
			this.tank = new FluidStack[tag.getInteger("lengthTank")];
			for (int i = 0; i < this.tank.length; i++) {
				if (tag.hasKey("tank#" + i))
					this.tank[i] = FluidStack.loadFluidStackFromNBT(tag
							.getCompoundTag("tank#" + i));
				else
					this.tank[i] = null;
			}
			this.filter = new String[tag.getInteger("lengthFilter")];
			for (int i = 0; i < this.filter.length; i++) {
				if (tag.hasKey("filter#" + i))
					this.filter[i] = tag.getString("filter#" + i);
				else
					this.filter[i] = "";
			}
			break;
		case 1:
			this.filterSlot = in.readInt();
			this.fluidName = ByteBufUtils.readUTF8String(in);
			break;
		default:
		}

	}

	@Override
	public void writeData(ByteBuf out) {
		switch (this.mode) {
		case 0:
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
			ByteBufUtils.writeTag(out, tag);
			break;
		case 1:
			out.writeInt(this.filterSlot);
			ByteBufUtils.writeUTF8String(out, this.fluidName);
			break;
		default:
		}

	}

}

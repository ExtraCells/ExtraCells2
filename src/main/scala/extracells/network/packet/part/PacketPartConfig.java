package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.AccessRestriction;
import appeng.api.config.RedstoneMode;
import extracells.gui.fluid.GuiBusFluidIO;
import extracells.gui.fluid.GuiBusFluidStorage;
import extracells.gui.fluid.GuiFluidEmitter;
import extracells.network.packet.IPacketHandlerClient;
import extracells.network.packet.IPacketHandlerServer;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.part.PartECBase;
import extracells.part.fluid.PartFluidIO;
import extracells.part.fluid.PartFluidLevelEmitter;
import extracells.part.fluid.PartFluidPlaneFormation;
import extracells.part.fluid.PartFluidStorage;
import extracells.util.GuiUtil;
import extracells.util.NetworkUtil;

public class PacketPartConfig extends Packet {
	public static final String FLUID_EMITTER_TOGGLE = "FluidEmitter.Toggle";
	public static final String FLUID_EMITTER_AMOUNT = "FluidEmitter.Amount";
	public static final String FLUID_EMITTER_AMOUNT_CHANGE = "FluidEmitter.Amount.Change";
	public static final String FLUID_EMITTER_MODE = "FluidEmitter.Mode";

	public static final String FLUID_IO_REDSTONE = "FluidIO.Redstone";
	public static final String FLUID_IO_REDSTONE_LOOP = "FluidIO.Redstone.Loop";
	public static final String FLUID_IO_REDSTONE_MODE = "FluidIO.Redstone.Mode";
	public static final String FLUID_IO_INFO = "FluidIO.Info";
	public static final String FLUID_IO_FILTER = "FluidIO.Filter";

	public static final String FLUID_STORAGE_INFO = "FluidStorage.Info";
	public static final String FLUID_STORAGE_ACCESS = "FluidStorage.Access";

	public static final String FLUID_PLANE_FORMATION_INFO = "FluidPlaneFormation.Info";

	private PartECBase part;
	private String name;
	private String value;

	public PacketPartConfig(PartECBase part, String name) {
		this.part = part;
		this.name = name;
		this.value = "";
	}

	public PacketPartConfig(PartECBase part, String name, String value) {
		this.part = part;
		this.name = name;
		this.value = value;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		data.writePart(part);
		data.writeString(name);
		data.writeString(value);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.PART_CONFIG;
	}

	@SideOnly(Side.CLIENT)
	public static class HandlerClient implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			PartECBase part = data.readPart(player.world);
			String name = data.readString();
			String value = data.readString();
			if (name.equals(FLUID_EMITTER_AMOUNT) && part instanceof PartFluidLevelEmitter) {
				long amount = Long.valueOf(value);
				GuiFluidEmitter gui = GuiUtil.getGui(GuiFluidEmitter.class);
				if (gui == null) {
					return;
				}
				gui.setAmountField(amount);
			} else if (name.equals(FLUID_EMITTER_MODE) && part instanceof PartFluidLevelEmitter) {
				RedstoneMode redstoneMode = RedstoneMode.valueOf(value);
				GuiFluidEmitter gui = GuiUtil.getGui(GuiFluidEmitter.class);
				if (gui == null) {
					return;
				}
				gui.setRedstoneMode(redstoneMode);
			} else if (name.equals(FLUID_IO_REDSTONE) && part instanceof PartFluidIO) {
				boolean redstoneControlled = Boolean.valueOf(value);
				GuiBusFluidIO gui = GuiUtil.getGui(GuiBusFluidIO.class);
				if (gui == null) {
					return;
				}
				gui.setRedstoneControlled(redstoneControlled);
			} else if (name.equals(FLUID_IO_FILTER) && part instanceof PartFluidIO) {
				byte filterSize = Byte.valueOf(value);
				GuiBusFluidIO gui = GuiUtil.getGui(GuiBusFluidIO.class);
				if (gui == null) {
					return;
				}
				gui.changeConfig(filterSize);
			} else if (name.equals(FLUID_IO_REDSTONE_MODE) && part instanceof PartFluidIO) {
				RedstoneMode redstoneMode = RedstoneMode.valueOf(value);
				GuiBusFluidIO gui = GuiUtil.getGui(GuiBusFluidIO.class);
				if (gui == null) {
					return;
				}
				gui.updateRedstoneMode(redstoneMode);
			} else if (name.equals(FLUID_STORAGE_ACCESS)) {
				AccessRestriction access = AccessRestriction.valueOf(value);
				GuiBusFluidStorage gui = GuiUtil.getGui(GuiBusFluidStorage.class);
				if (gui == null || access == null) {
					return;
				}
				gui.updateAccessRestriction(access);
			}
		}
	}

	public static class HandlerServer implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			PartECBase part = data.readPart(player.world);
			String name = data.readString();
			String value = data.readString();
			if (name.equals(FLUID_EMITTER_TOGGLE) && part instanceof PartFluidLevelEmitter) {
				boolean toggle = Boolean.valueOf(value);
				if (toggle) {
					((PartFluidLevelEmitter) part).toggleMode(player);
				} else {
					((PartFluidLevelEmitter) part).syncClientGui(player);
				}
			} else if (name.equals(FLUID_EMITTER_AMOUNT_CHANGE) && part instanceof PartFluidLevelEmitter) {
				long amount = Long.valueOf(value);
				((PartFluidLevelEmitter) part).changeWantedAmount((int) amount, player);
			} else if (name.equals(FLUID_EMITTER_AMOUNT) && part instanceof PartFluidLevelEmitter) {
				long amount = Long.valueOf(value);
				((PartFluidLevelEmitter) part).setWantedAmount(amount, player);
			} else if (name.equals(FLUID_IO_INFO) && part instanceof PartFluidIO) {
				((PartFluidIO) part).sendInformation(player);
			} else if (name.equals(FLUID_IO_REDSTONE_LOOP) && part instanceof PartFluidIO) {
				((PartFluidIO) part).loopRedstoneMode(player);
			} else if (name.equals(FLUID_STORAGE_ACCESS) && part instanceof PartFluidStorage) {
				AccessRestriction access = AccessRestriction.valueOf(value);
				if (access == null) {
					return;
				}
				((PartFluidStorage) part).updateAccess(access);
				NetworkUtil.sendToPlayer(new PacketPartConfig(part, PacketPartConfig.FLUID_STORAGE_ACCESS, value), player);
			} else if (name.equals(FLUID_STORAGE_INFO) && part instanceof PartFluidStorage) {
				((PartFluidStorage) part).sendInformation(player);
			} else if (name.equals(FLUID_PLANE_FORMATION_INFO) && part instanceof PartFluidPlaneFormation) {
				((PartFluidPlaneFormation) part).sendInformation(player);
			}
		}
	}
}

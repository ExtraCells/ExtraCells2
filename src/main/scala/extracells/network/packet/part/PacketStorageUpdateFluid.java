package extracells.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.gui.GuiStorage;
import extracells.network.packet.IPacketHandlerClient;
import extracells.network.packet.Packet;
import extracells.network.packet.PacketBufferEC;
import extracells.network.packet.PacketId;
import extracells.util.GuiUtil;

public class PacketStorageUpdateFluid extends Packet {
	IItemList<IAEFluidStack> fluidStackList;

	public PacketStorageUpdateFluid(IItemList<IAEFluidStack> fluidStackList) {
		this.fluidStackList = fluidStackList;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writeAEFluidStacks(fluidStackList);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.STORAGE_UPDATE_FLUID;
	}

	@SideOnly(Side.CLIENT)
	public static class Handler implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			IItemList<IAEFluidStack> fluidStackList = data.readAEFluidStacks();
			GuiStorage gui = GuiUtil.getGui(GuiStorage.class);
			if (fluidStackList == null || gui == null) {
				return;
			}

			gui.updateFluids(fluidStackList);
		}
	}
}

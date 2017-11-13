package extracells.integration.mekanism.gas;

import appeng.api.storage.*;
import extracells.util.PlayerSource;
import extracells.util.StorageChannels;
import mekanism.api.gas.Gas;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

import appeng.api.implementations.tiles.IChestOrDrive;
import extracells.api.IGasStorageCell;
import extracells.integration.Capabilities;
import extracells.inventory.cell.HandlerItemPlayerStorageGas;
import extracells.inventory.cell.HandlerItemStorageGas;
import extracells.network.GuiHandler;

import java.util.ArrayList;
import java.util.List;

public class GasCellHandler implements ICellHandler {

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler) {
		return 0;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack itemStack, ISaveProvider saveProvider, IStorageChannel channel) {
		if (channel != StorageChannels.GAS() || !(itemStack.getItem() instanceof IGasStorageCell)) {
			return null;
		}
		return new HandlerItemStorageGas(itemStack, saveProvider, (ArrayList<Gas>) (Object)(((IGasStorageCell) itemStack.getItem()).getFilter(itemStack)));
	}

	public IMEInventoryHandler getCellInventoryPlayer(ItemStack itemStack, EntityPlayer player, EnumHand hand) {
		return new HandlerItemPlayerStorageGas(itemStack, null, (ArrayList<Gas>) (Object)((IGasStorageCell) itemStack.getItem()).getFilter(itemStack), player, hand);
	}

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler) {
		if (handler == null) {
			return 0;
		}

		HandlerItemStorageGas inventory = (HandlerItemStorageGas) handler;
		if (inventory.freeBytes() == 0) {
			return 3;
		}
		if (inventory.isFormatted() || inventory.usedTypes() == inventory.totalTypes()) {
			return 2;
		}

		return 1;
	}

	/*@Override
	public IIcon getTopTexture_Dark() {
		return TextureManager.TERMINAL_FRONT.getTextures()[0];
	}

	@Override
	public IIcon getTopTexture_Light() {
		return TextureManager.TERMINAL_FRONT.getTextures()[2];
	}

	@Override
	public IIcon getTopTexture_Medium() {
		return TextureManager.TERMINAL_FRONT.getTextures()[1];
	}*/


	@Override
	public boolean isCell(ItemStack is) {
		return is != null && is.getItem() != null && is.getItem() instanceof IGasStorageCell;
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, IStorageChannel chan) {
		if (chan != StorageChannels.GAS()) {
			return;
		}
		IStorageMonitorable monitorable = null;
		if (chest != null && chest instanceof TileEntity) {
			TileEntity tileEntity = (TileEntity) chest;
			if (tileEntity.hasCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, null)) {
				IStorageMonitorableAccessor accessor = tileEntity.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, null);
				monitorable = accessor.getInventory(new PlayerSource(player, chest));
			}
		}
		if (monitorable != null) {
			GuiHandler.launchGui(GuiHandler.getGuiId(4), player, EnumHand.MAIN_HAND, new Object[]{monitorable.getInventory(StorageChannels.GAS())});
		}
	}

}

package extracells.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannel;
import extracells.api.IFluidStorageCell;
import extracells.integration.Capabilities;
import extracells.inventory.HandlerItemPlayerStorageFluid;
import extracells.inventory.HandlerItemStorageFluid;
import extracells.network.GuiHandler;


public class FluidCellHandler implements ICellHandler {

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler) {
		return 0;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack itemStack, ISaveProvider saveProvider, StorageChannel channel) {
		if (channel == StorageChannel.ITEMS || !(itemStack.getItem() instanceof IFluidStorageCell)) {
			return null;
		}
		return new HandlerItemStorageFluid(itemStack, saveProvider, ((IFluidStorageCell) itemStack.getItem()).getFilter(itemStack));
	}

	public IMEInventoryHandler getCellInventoryPlayer(ItemStack itemStack, EntityPlayer player, EnumHand hand) {
		return new HandlerItemPlayerStorageFluid(itemStack, null, ((IFluidStorageCell) itemStack.getItem()).getFilter(itemStack), player, hand);
	}

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler) {
		if (handler == null) {
			return 0;
		}

		HandlerItemStorageFluid inventory = (HandlerItemStorageFluid) handler;
		if (inventory.freeBytes() == 0) {
			return 3;
		}
		if (inventory.isFormatted() || inventory.usedTypes() == inventory.totalTypes()) {
			return 2;
		}

		return 1;
	}

	@Override
	public boolean isCell(ItemStack is) {
		return is != null && is.getItem() != null && is.getItem() instanceof IFluidStorageCell;
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
		if (chan != StorageChannel.FLUIDS) {
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
			GuiHandler.launchGui(GuiHandler.getGuiId(0), player, EnumHand.MAIN_HAND, new Object[]{monitorable.getFluidInventory()});
		}
	}

}

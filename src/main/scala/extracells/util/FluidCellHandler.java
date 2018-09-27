package extracells.util;

import appeng.api.storage.*;
import extracells.api.IGasStorageCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

import appeng.api.implementations.tiles.IChestOrDrive;
import extracells.api.IFluidStorageCell;
import extracells.integration.Capabilities;
import extracells.inventory.cell.HandlerItemPlayerStorageFluid;
import extracells.inventory.cell.HandlerItemStorageFluid;
import extracells.network.GuiHandler;


public class FluidCellHandler implements ICellHandler {

	@Override
	public ICellInventoryHandler getCellInventory(ItemStack itemStack, ISaveProvider saveProvider, IStorageChannel channel) {
		if (channel != StorageChannels.FLUID() || !(itemStack.getItem() instanceof IFluidStorageCell)) {
			return null;
		}
		return new HandlerItemStorageFluid(itemStack, saveProvider, ((IFluidStorageCell) itemStack.getItem()).getFilter(itemStack));
	}

	public IMEInventoryHandler getCellInventoryPlayer(ItemStack itemStack, EntityPlayer player, EnumHand hand) {
		return new HandlerItemPlayerStorageFluid(itemStack, null, ((IFluidStorageCell) itemStack.getItem()).getFilter(itemStack), player, hand);
	}

	@Override
	public boolean isCell(ItemStack is) {
		return is != null && is.getItem() != null && is.getItem() instanceof IFluidStorageCell && (!(is.getItem() instanceof IGasStorageCell));
	}
}

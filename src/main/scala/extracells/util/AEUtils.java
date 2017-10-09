package extracells.util;

import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.StorageChannel;

public class AEUtils {

	public static boolean isItemChannel(StorageChannel channel) {
		return channel == getItemChannel();
	}

	public static boolean isFluidChannel(StorageChannel channel) {
		return channel == getFluidChannel();
	}

	public static StorageChannel getItemChannel() {
		return StorageChannel.ITEMS;
	}

	public static StorageChannel getFluidChannel() {
		return StorageChannel.FLUIDS;
	}

	public static ICellRegistry cell() {
		return AEApi.instance().registries().cell();
	}
}

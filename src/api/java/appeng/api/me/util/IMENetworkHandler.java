package appeng.api.me.util;

import appeng.api.IItemList;

public interface IMENetworkHandler extends IMEInventoryHandler {
	
	IItemList getAvailableItems();
	
}

package extracells.integration;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import appeng.api.storage.IStorageMonitorableAccessor;

public class Capabilities {
	@CapabilityInject(IStorageMonitorableAccessor.class)
	public static Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR;
}

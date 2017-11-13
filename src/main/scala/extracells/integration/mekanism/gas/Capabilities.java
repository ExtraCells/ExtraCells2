package extracells.integration.mekanism.gas;


import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {

    @CapabilityInject(IGasHandler.class)
    public static Capability<IGasHandler> GAS_HANDLER_CAPABILITY = null;

    @CapabilityInject(ITubeConnection.class)
    public static Capability<ITubeConnection> TUBE_CONNECTION_CAPABILITY = null;
}

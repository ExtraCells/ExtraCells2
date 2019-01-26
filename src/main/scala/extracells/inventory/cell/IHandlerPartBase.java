package extracells.inventory.cell;


import appeng.api.config.AccessRestriction;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEStack;
import net.minecraftforge.fluids.Fluid;

public interface IHandlerPartBase<T extends IAEStack<T>> extends IMEInventoryHandler<T>, IMEMonitorHandlerReceiver<T> {

    void setAccessRestriction(AccessRestriction access);

    void setInverted(boolean _inverted);

    void setPrioritizedFluids(Fluid[] _fluids);

    void onNeighborChange();
}

package extracells.integration.mekanism.gas;


import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraftforge.common.util.ForgeDirection;

public class LayerGasHandler extends LayerBase implements IGasHandler{
    @Override
    public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).receiveGas(side, stack, doTransfer);
        }
        return 0;
    }

    @Override
    public int receiveGas(ForgeDirection side, GasStack stack) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).receiveGas(side, stack, true);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).drawGas(side, amount, doTransfer);
        }
        return null;
    }

    @Override
    public GasStack drawGas(ForgeDirection side, int amount) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).drawGas(side, amount, true);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(ForgeDirection side, Gas type) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).canReceiveGas(side, type);
        }
        return false;
    }

    @Override
    public boolean canDrawGas(ForgeDirection side, Gas type) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).canDrawGas(side, type);
        }
        return false;
    }
}

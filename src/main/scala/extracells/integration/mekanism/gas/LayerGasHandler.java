package extracells.integration.mekanism.gas;


import net.minecraft.util.EnumFacing;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;

public class LayerGasHandler extends LayerBase implements IGasHandler{
    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).receiveGas(side, stack, doTransfer);
        }
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).drawGas(side, amount, doTransfer);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).canReceiveGas(side, type);
        }
        return false;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        IPart part = this.getPart(side);
        if(part instanceof IGasHandler){
            return ((IGasHandler) part).canDrawGas(side, type);
        }
        return false;
    }
}

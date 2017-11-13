package extracells.api.gas;


import appeng.api.storage.data.IAEStack;

public interface IAEGasStack extends IAEStack<IAEGasStack>{

    /**
     *
     * @return {@link mekanism.api.gas.GasStack}
     */
    Object getGasStack();

    void add(IAEGasStack var1);

    IAEGasStack copy();

    /**
     *
     * @return {@link mekanism.api.gas.Gas}
     */
    Object getGas();
}

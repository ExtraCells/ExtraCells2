package extracells.integration.mekanism.gas;


import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IItemList;
import extracells.api.gas.IAEGasStack;

import java.util.*;

public class GasList implements IItemList<IAEGasStack>{

    private final Map<IAEGasStack, IAEGasStack> records = new HashMap<>();

    @Override
    public void addStorage(IAEGasStack iaeGasStack) {
        if (iaeGasStack == null)
            return;
        IAEGasStack stack = this.getGasRecord(iaeGasStack);
        if (stack != null){
            stack.add(iaeGasStack);
            return;
        }
        IAEGasStack toAdd = iaeGasStack.copy();
        this.putGasRecord(toAdd);
    }

    @Override
    public void addCrafting(IAEGasStack iaeGasStack) {
        if(iaeGasStack == null)
            return;

        IAEGasStack stack = this.getGasRecord(iaeGasStack);

        if(stack != null)
        {
            stack.setCraftable(true);
            return;
        }

        IAEGasStack toAdd = iaeGasStack.copy();
        toAdd.setStackSize(0);
        toAdd.setCraftable(true);

        this.putGasRecord(toAdd);
    }

    @Override
    public void addRequestable(IAEGasStack iaeGasStack) {
        if(iaeGasStack == null)
            return;

        IAEGasStack stack = this.getGasRecord(iaeGasStack);

        if(stack != null)
        {
            stack.setCountRequestable(stack.getCountRequestable() + iaeGasStack.getCountRequestable());
            return;
        }

        IAEGasStack toAdd = iaeGasStack.copy();
        toAdd.setStackSize(0);
        toAdd.setCraftable(false);
        toAdd.setCountRequestable(iaeGasStack.getCountRequestable());

        this.putGasRecord(toAdd);
    }

    @Override
    public IAEGasStack getFirstItem() {
        for (IAEGasStack stack : this)
            return stack;
        return null;
    }

    @Override
    public int size() {
        return this.records.values().size();
    }

    @Override
    public Iterator<IAEGasStack> iterator() {
        return new GasIterator<>(this.records.values().iterator());
    }

    @Override
    public void resetStatus() {
        for (IAEGasStack gasStack : this)
            gasStack.reset();

    }

    @Override
    public void add(IAEGasStack iaeGasStack) {
        if(iaeGasStack == null)
            return;
        IAEGasStack stack = this.getGasRecord(iaeGasStack);
        if (stack != null){
            stack.add(iaeGasStack);
        }
        IAEGasStack toAdd = iaeGasStack.copy();
        this.putGasRecord(toAdd);
    }

    @Override
    public IAEGasStack findPrecise(IAEGasStack iaeGasStack) {
        if(iaeGasStack == null)
            return null;
        return this.getGasRecord(iaeGasStack);
    }

    @Override
    public Collection<IAEGasStack> findFuzzy(IAEGasStack iaeGasStack, FuzzyMode fuzzyMode) {
        if(iaeGasStack == null)
            return Collections.emptyList();

        return Collections.singletonList(this.findPrecise(iaeGasStack));
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    private IAEGasStack getGasRecord(IAEGasStack gas)
    {
        return this.records.get(gas);
    }

    private IAEGasStack putGasRecord(IAEGasStack gas)
    {
        return this.records.put(gas, gas);
    }
}

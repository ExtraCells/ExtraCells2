package extracells.crafting;

import java.util.ArrayList;
import java.util.List;

import extracells.api.crafting.IFluidCraftingPatternDetails;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

public class CraftingPatter implements IFluidCraftingPatternDetails {
	
	private final ICraftingPatternDetails pattern;
	
	private IAEFluidStack[] fluidsCondenced = null;
	private IAEFluidStack[] fluids = null;
	
	public CraftingPatter(ICraftingPatternDetails _pattern){
		pattern = _pattern;
	}
	
	@Override
	public ItemStack getPattern() {
		return pattern.getPattern();
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack,
			World world) {
		return pattern.isValidItemForSlot(slotIndex, itemStack, world);
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return removFluidContainers(pattern.getInputs(), false);
	}

	@Override
	public IAEItemStack[] getCondencedInputs() {
		return removFluidContainers(pattern.getCondencedInputs(), true);
	}

	@Override
	public IAEItemStack[] getCondencedOutputs() {
		return pattern.getCondencedOutputs();
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return pattern.getOutputs();
	}

	@Override
	public boolean canSubstitute() {
		return pattern.canSubstitute();
	}

	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
		return pattern.getOutput(craftingInv, world);
	}

	@Override
	public void setPriority(int priority) {
		pattern.setPriority(priority);
	}

	@Override
	public IAEFluidStack[] getCondencedFluidInputs(){
		if(fluidsCondenced ==  null){
			getCondencedInputs();
		}
		return fluidsCondenced;
	}

	@Override
	public IAEFluidStack[] getFluidInputs(){
		if(fluids == null){
			getInputs();
		}
		return fluids;
	}
	
	public IAEItemStack[] removFluidContainers(IAEItemStack[] requirements, boolean isCondenced)
	{

		IAEItemStack[] returnStack = new IAEItemStack[requirements.length];
		
		IAEFluidStack[] fluidStacks = new IAEFluidStack[requirements.length];

		int removed = 0;
		int i = 0;
		for (IAEItemStack currentRequirement : requirements)
		{
			
			if (currentRequirement != null)
			{
				FluidStack fluid = null;
				if (FluidContainerRegistry.isFilledContainer(currentRequirement.getItemStack()))
				{
					fluid = FluidContainerRegistry.getFluidForFilledItem(currentRequirement.getItemStack());
				} else if (currentRequirement.getItem() instanceof IFluidContainerItem)
				{
					fluid = ((IFluidContainerItem) currentRequirement.getItem()).getFluid(currentRequirement.getItemStack());
				}
				if (fluid == null)
				{
					returnStack[i] = currentRequirement;
				}else{
					removed ++;
					fluidStacks[i] = AEApi.instance().storage().createFluidStack(new FluidStack(FluidContainerRegistry.getFluidForFilledItem(currentRequirement.getItemStack()), (int) (FluidContainerRegistry.BUCKET_VOLUME * currentRequirement.getStackSize())));
				}
			}
			i = i++;
		}
		
		if(isCondenced){
			int i2 = 0;
			IAEFluidStack[] fluids = new IAEFluidStack[removed];
			for(IAEFluidStack fluid : fluidStacks){
				if(fluid != null){
					fluids[i2] = fluid;
					i2 ++;
				}
			}
			int i3 = 0;
			IAEItemStack[] items = new IAEItemStack[requirements.length - removed];
			for(IAEItemStack item : returnStack){
				if(item != null){
					items[i3] = item;
					i3++;
				}
			}
			returnStack = items;
			fluidsCondenced = fluids;
		}else{
			this.fluids = fluidStacks;
		}
		return returnStack;
	}

}

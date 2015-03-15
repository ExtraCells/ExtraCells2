package extracells.crafting;

import extracells.api.crafting.IFluidCraftingPatternDetails;
import extracells.registries.ItemEnum;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

public class CraftingPattern implements IFluidCraftingPatternDetails, Comparable<CraftingPattern> {
	
	protected final ICraftingPatternDetails pattern;
	
	private IAEFluidStack[] fluidsCondensed = null;
	private IAEFluidStack[] fluids = null;
	
	public CraftingPattern(ICraftingPatternDetails _pattern){
		pattern = _pattern;
	}
	
	@Override
	public ItemStack getPattern() {
		ItemStack p = pattern.getPattern();
		if(p == null)
			return null;
		ItemStack s = new ItemStack(ItemEnum.CRAFTINGPATTERN.getItem());
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("item", p.writeToNBT(new NBTTagCompound()));
		s.setTagCompound(tag);
		return s;
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack,
			World world) {
		return pattern.isValidItemForSlot(slotIndex, itemStack, world);
	}

	@Override
	public boolean isCraftable() {
		return pattern.isCraftable();
	}

	@Override
	public IAEItemStack[] getInputs() {
		return removeFluidContainers(pattern.getInputs(), false);
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return removeFluidContainers(pattern.getCondensedInputs(), true);
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return pattern.getCondensedOutputs();
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
		IAEItemStack[] input = pattern.getInputs();
		for(int i = 0; i < input.length; i++){
			IAEItemStack stack = input[i];
			if(stack != null && FluidContainerRegistry.isFilledContainer(stack.getItemStack())){
				try{
				craftingInv.setInventorySlotContents(i, input[i].getItemStack());
				}catch(Throwable e){}
			}else if(stack != null && stack.getItem() instanceof IFluidContainerItem){
				try{
					craftingInv.setInventorySlotContents(i, input[i].getItemStack());
					}catch(Throwable e){}
			}
		}
		ItemStack returnStack = pattern.getOutput(craftingInv, world);
		for(int i = 0; i < input.length; i++){
			IAEItemStack stack = input[i];
			if(stack != null && FluidContainerRegistry.isFilledContainer(stack.getItemStack())){
				craftingInv.setInventorySlotContents(i, null);
			}else if(stack != null && stack.getItem() instanceof IFluidContainerItem){
				craftingInv.setInventorySlotContents(i, null);
			}
		}
		return returnStack;
	}

	@Override
	public void setPriority(int priority) {
		pattern.setPriority(priority);
	}

	@Override
	public IAEFluidStack[] getCondensedFluidInputs(){
		if(fluidsCondensed ==  null){
			getCondensedInputs();
		}
		return fluidsCondensed;
	}

	@Override
	public IAEFluidStack[] getFluidInputs(){
		if(fluids == null){
			getInputs();
		}
		return fluids;
	}
	
	public IAEItemStack[] removeFluidContainers(IAEItemStack[] requirements, boolean isCondenced)
	{

		IAEItemStack[] returnStack = new IAEItemStack[requirements.length];
		
		IAEFluidStack[] fluidStacks = new IAEFluidStack[requirements.length];

		int removed = 0;
		int i = 0;
		for (IAEItemStack currentRequirement : requirements)
		{
			
			if (currentRequirement != null)
			{
				ItemStack current = currentRequirement.getItemStack();
				current.stackSize = 1;
				FluidStack fluid = null;
				if (FluidContainerRegistry.isFilledContainer(current))
				{
					fluid = FluidContainerRegistry.getFluidForFilledItem(current);
				} else if (currentRequirement.getItem() instanceof IFluidContainerItem)
				{
					fluid = ((IFluidContainerItem) currentRequirement.getItem()).getFluid(current);
				}
				if (fluid == null)
				{
					returnStack[i] = currentRequirement;
				}else{
					removed ++;
					fluidStacks[i] = AEApi.instance().storage().createFluidStack(new FluidStack(fluid.getFluid(), (int) (fluid.amount * currentRequirement.getStackSize())));
				}
			}
			i++;
		}
		
		if(isCondenced){
			int i2 = 0;
			IAEFluidStack[] fluids = new IAEFluidStack[removed];
			for(IAEFluidStack fluid : fluidStacks){
				if(fluid != null){
					fluids[i2] = fluid;
					i2++;
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
			fluidsCondensed = fluids;
		}else{
			this.fluids = fluidStacks;
		}
		return returnStack;
	}

	@Override
	public int getPriority() {
		return pattern.getPriority();
	}
	
	@Override
	public boolean equals(Object obj){
		System.out.println(obj);
		if ( obj == null )
			return false;
		if ( this.getClass() != obj.getClass() )
			return false;
		CraftingPattern other = (CraftingPattern) obj;
		if ( this.pattern != null && other.pattern != null )
			return this.pattern.equals( other.pattern );
		return false;
	}
	
	@Override
	public int compareTo(CraftingPattern o){
		return compareInt(o.getPriority(), this.getPriority());
	}
	
	public int compareInt(int int1, int int2){
		if (int1 == int2)
			return 0;
		if (int1 < int2)
			return -1;
		return 1;
	}

}

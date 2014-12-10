package extracells.util.inventory;

import extracells.registries.ItemEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ECFluidFilterInventory extends ECPrivateInventory {

	private final ItemStack cellItem;
	
	public ECFluidFilterInventory(String _customName, int _size, ItemStack _cellItem) {
		super(_customName, _size, 1);
		cellItem = _cellItem;
		if(cellItem.hasTagCompound())
			if(cellItem.getTagCompound().hasKey("filter"))
				readFromNBT(cellItem.getTagCompound().getTagList("filter", 10));
	}
	
	@Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if(itemstack == null)
			return false;
		int fluidID;
		if(itemstack.getItem() == ItemEnum.FLUIDITEM.getItem()){
			fluidID = itemstack.getItemDamage();
		}else{
			FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemstack);
			if(fluidStack == null)
				return false;
			Fluid fluid = fluidStack.getFluid();
			if(fluid == null)
				return false;
			fluidID = fluid.getID();
		}
		
		for(ItemStack s : slots){
			if(s == null)
				continue;
			if(s.getItemDamage() == fluidID)
				return false;
		}
        return true;
    }
	
	@Override
    public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if(itemstack == null){
			super.setInventorySlotContents(slotId, null);
			return;
		}
		Fluid fluid;
		if(itemstack.getItem() == ItemEnum.FLUIDITEM.getItem()){
			fluid = FluidRegistry.getFluid(itemstack.getItemDamage());
			if(fluid == null)
				return;
		}else{
			if(!isItemValidForSlot(slotId, itemstack))
				return;
			FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemstack);
			if(fluidStack == null){
				super.setInventorySlotContents(slotId, null);
				return;
			}
			fluid = fluidStack.getFluid();
			if(fluid == null){
				super.setInventorySlotContents(slotId, null);
				return;
			}
		}
		super.setInventorySlotContents(slotId, new ItemStack(ItemEnum.FLUIDITEM.getItem(), 1, fluid.getID()));
	}
	
	@Override
	public void markDirty(){
		NBTTagCompound tag;
		if(cellItem.hasTagCompound())
			tag = cellItem.getTagCompound();
		else
			tag = new NBTTagCompound();
		tag.setTag("filter", writeToNBT());
	}

}

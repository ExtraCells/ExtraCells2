package extracells.item;

import java.util.ArrayList;
import java.util.List;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import extracells.api.ECApi;
import extracells.api.IHandlerFluidStorage;
import extracells.api.IPortableFluidStorageCell;
import extracells.util.inventory.ECFluidFilterInventory;
import extracells.util.inventory.ECPrivateInventory;

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy")
public class ItemStoragePortableCell extends Item implements IPortableFluidStorageCell, IAEItemPowerStorage, IEnergyContainerItem {

	IIcon icon;
	
	private static final int MAX_POWER = 20000;
	
	public ItemStoragePortableCell() {
        setMaxStackSize(1);
        setMaxDamage(0);
    }

    @Override
    public IIcon getIconFromDamage(int dmg) {
        return icon;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
    	icon = iconRegister.registerIcon("extracells:storage.fluid.portable");
        
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
        itemList.add(new ItemStack(item));
        ItemStack itemStack = new ItemStack(item);
        injectAEPower(itemStack, MAX_POWER);
        itemList.add(itemStack);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return "extracells.item.storage.fluid.portable";
    }

    @Override
    public int getMaxTypes(ItemStack unused) {
        return 3;
    }

    @Override
    public int getMaxBytes(ItemStack is) {
        return 512;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS);
        if (!(handler instanceof IHandlerFluidStorage)) {
            return;
        }
        IHandlerFluidStorage cellHandler = (IHandlerFluidStorage) handler;
        boolean partitioned = cellHandler.isFormatted();
        long usedBytes = cellHandler.usedBytes();
        double aeCurrentPower = getAECurrentPower(itemStack);

        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), usedBytes / 250, cellHandler.totalBytes() / 250));
        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
        if (usedBytes != 0) {
            list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes));
        }

        if (partitioned) {
            list.add(StatCollector.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal("gui.appliedenergistics2.Precise"));
        }
        list.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor((aeCurrentPower / MAX_POWER) * 1e4) / 1e2 + "%");
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - getAECurrentPower(itemStack) / MAX_POWER;
    }
    
    @Override
    public boolean showDurabilityBar(ItemStack itemStack) {
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        return ECApi.instance().openPortableCellGui(player, itemStack, world);
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }

	@Override
	public boolean isEditable(ItemStack is) {
		if(is == null)
			return false;
		return is.getItem() == this;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is) {
		return new ECPrivateInventory("configInventory", 0, 64);
	}

	@Override
	public IInventory getConfigInventory(ItemStack is) {
		return new ECFluidFilterInventory("configFluidCell", 63, is);
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is) {
		if(is == null)
			return null;
		if(!is.hasTagCompound())
			is.setTagCompound(new NBTTagCompound());
		if(is.getTagCompound().hasKey("fuzzyMode"))
			return FuzzyMode.valueOf(is.getTagCompound().getString("fuzzyMode"));
		is.getTagCompound().setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name());
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		if(is == null)
			return;
		
		if(!is.hasTagCompound())
			is.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = is.getTagCompound();
		tag.setString("fuzzyMode", fzMode.name());
		
	}
	
	@Override
	public ArrayList<Fluid> getFilter(ItemStack stack){
		ECFluidFilterInventory inventory = new ECFluidFilterInventory("", 63, stack);
		ItemStack[] stacks = inventory.slots;
		ArrayList<Fluid> filter = new ArrayList<Fluid>();
		if(stacks.length == 0)
			return null;
		for(ItemStack s : stacks){
			if(s == null)
				continue;
			Fluid f = FluidRegistry.getFluid(s.getItemDamage());
			if(f != null)
				filter.add(f);
		}
		return filter;
	}

	@Override
	public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
		extractAEPower(is, amount);
		return true;
	}

	@Override
	public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
		return getAECurrentPower(is) >= amount;
	}

	@Override
    public double injectAEPower(ItemStack itemStack, double amt) {
        NBTTagCompound tagCompound = ensureTagCompound(itemStack);
        double currentPower = tagCompound.getDouble("power");
        double toInject = Math.min(amt, MAX_POWER - currentPower);
        tagCompound.setDouble("power", currentPower + toInject);
        return toInject;
    }

    @Override
    public double extractAEPower(ItemStack itemStack, double amt) {
        NBTTagCompound tagCompound = ensureTagCompound(itemStack);
        double currentPower = tagCompound.getDouble("power");
        double toExtract = Math.min(amt, currentPower);
        tagCompound.setDouble("power", currentPower - toExtract);
        return toExtract;
    }

    @Override
    public double getAEMaxPower(ItemStack itemStack) {
        return MAX_POWER;
    }

    @Override
    public double getAECurrentPower(ItemStack itemStack) {
        NBTTagCompound tagCompound = ensureTagCompound(itemStack);
        return tagCompound.getDouble("power");
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack itemStack) {
        return AccessRestriction.READ_WRITE;
    }

    private NBTTagCompound ensureTagCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound())
            itemStack.setTagCompound(new NBTTagCompound());
        return itemStack.getTagCompound();
    }

    @Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		if(container == null)
			return 0;
		if(simulate){
			return (int) (getEnergyStored(container) >= maxExtract ? maxExtract : getEnergyStored(container));
		}else{
			return (int) PowerUnits.AE.convertTo(PowerUnits.RF, (extractAEPower(container, (PowerUnits.RF.convertTo(PowerUnits.AE, maxExtract)))));
		}
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(ItemStack arg0) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, (getAECurrentPower(arg0)));
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(ItemStack arg0) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF, (getAEMaxPower(arg0)));
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		if(container == null)
			return 0;
		if(simulate){
			double current = PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(container));
			double max = PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(container));
			if(max - current >= maxReceive)
				return maxReceive;
			else
				return (int) (max - current);
		}else{
			int notStored = (int) (PowerUnits.AE.convertTo(PowerUnits.RF, injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive))));
			return maxReceive - notStored;
		}
	}
}

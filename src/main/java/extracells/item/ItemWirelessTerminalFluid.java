package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.WorldCoord;
import extracells.api.ECApi;
import extracells.api.IWirelessFluidTermHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy")
public class ItemWirelessTerminalFluid extends Item implements IWirelessFluidTermHandler, IAEItemPowerStorage, IEnergyContainerItem {

    IIcon icon;
    private final int MAX_POWER = 3200000;

    public ItemWirelessTerminalFluid() {
        setMaxStackSize(1);
        ECApi.instance().registryWirelessFluidTermHandler(this);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
        itemList.add(new ItemStack(item));
        ItemStack itemStack = new ItemStack(item);
        injectAEPower(itemStack, MAX_POWER);
        itemList.add(itemStack);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        return ECApi.instance().openWirelessTerminal(entityPlayer, itemStack, world);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        if (!itemStack.hasTagCompound())
            itemStack.setTagCompound(new NBTTagCompound());
        String encryptionKey = itemStack.getTagCompound().getString("key");
        double aeCurrentPower = getAECurrentPower(itemStack);
        list.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor((aeCurrentPower / MAX_POWER) * 1e4) / 1e2 + "%");
        list.add(StatCollector.translateToLocal(encryptionKey != null && !encryptionKey.isEmpty() ? "gui.appliedenergistics2.Linked" : "gui.appliedenergistics2.Unlinked"));
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item");
    }

    public IIcon getIconFromDamage(int dmg) {
        return icon;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless");
    }

    @Override
    public String getEncryptionKey(ItemStack itemStack) {
        if (!itemStack.hasTagCompound())
            itemStack.setTagCompound(new NBTTagCompound());
        return itemStack.getTagCompound().getString("key");
    }

    @Override
    public void setEncryptionKey(ItemStack itemStack, String encKey, String name) {
        if (!itemStack.hasTagCompound())
            itemStack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        tagCompound.setString("key", encKey);
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
        return null;
    }

    private NBTTagCompound ensureTagCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound())
            itemStack.setTagCompound(new NBTTagCompound());
        return itemStack.getTagCompound();
    }

    @Override
    public int getDamage(ItemStack itemStack) {
        return (int) getAECurrentPower(itemStack);
    }

    @Override
    public int getMaxDamage(ItemStack itemStack) {
        return MAX_POWER;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - getAECurrentPower(itemStack) / MAX_POWER;
    }

    @Override
    public boolean showDurabilityBar(ItemStack itemStack) {
        return true;
    }
    
    @Override
	public boolean canHandle(ItemStack is) {
		if(is == null)
			return false;
		return is.getItem() == this;
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
	public boolean isItemNormalWirelessTermToo(ItemStack is) {
		return false;
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

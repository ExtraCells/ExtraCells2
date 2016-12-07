package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
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
import extracells.network.GuiHandler;
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

public class ItemWirelessTerminalFluid extends Item implements INetworkEncodable, IAEItemPowerStorage {

    IIcon icon;
    private final int MAX_POWER = 3200000;

    public ItemWirelessTerminalFluid() {
        setMaxStackSize(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
        itemList.add(new ItemStack(item));
        ItemStack itemStack = new ItemStack(item);
        injectAEPower(itemStack, MAX_POWER);
        itemList.add(itemStack);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        if (world.isRemote)
            return itemStack;
        NBTTagCompound nbtTagCompound = ensureTagCompound(itemStack);
        if (getAECurrentPower(itemStack) <= 0)
            return itemStack;
        Long key;
        try {
            key = Long.parseLong(nbtTagCompound.getString("key"));
        } catch (Throwable ignored) {
            return itemStack;
        }
        int x = (int) entityPlayer.posX;
        int y = (int) entityPlayer.posY;
        int z = (int) entityPlayer.posZ;
        IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable().findLocatableBySerial(key);
        if (securityTerminal == null)
            return itemStack;
        IGridNode gridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
        if (gridNode == null)
            return itemStack;
        IGrid grid = gridNode.getGrid();
        if (grid == null)
            return itemStack;
        for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) AEApi.instance().blocks().blockWireless.entity())) {
            IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
            WorldCoord distance = accessPoint.getLocation().subtract(x, y, z);
            int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
            if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
                IStorageGrid gridCache = grid.getCache(IStorageGrid.class);
                if (gridCache != null) {
                    IMEMonitor<IAEFluidStack> fluidInventory = gridCache.getFluidInventory();
                    if (fluidInventory != null) {
                        GuiHandler.launchGui(GuiHandler.getGuiId(1), entityPlayer, fluidInventory);
                    }
                }
            }
        }
        return itemStack;
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
}

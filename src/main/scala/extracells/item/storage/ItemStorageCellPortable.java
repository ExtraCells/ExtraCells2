package extracells.item.storage;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import extracells.api.IPortableStorageCell;
import extracells.models.ModelManager;

/**
 * @author BrockWS
 */
public abstract class ItemStorageCellPortable<T extends IAEStack<T>> extends ItemStorageCell<T> implements IPortableStorageCell, IAEItemPowerStorage {

    public static final double MAX_POWER = 20000;

    public ItemStorageCellPortable(CellDefinition definition, IStorageChannel<T> channel) {
        super(definition, channel);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Nonnull
    @Override
    public abstract ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand);

    @Nonnull
    @Override
    public abstract String getTranslationKey(ItemStack itemStack);

    @Override
    public abstract void registerModel(Item item, ModelManager manager);

    @Override
    public int getBytes(@Nonnull ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getTotalTypes(@Nonnull ItemStack cellItem) {
        return 3;
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, world, list, advanced);
        double currentAEPower = this.getAECurrentPower(itemStack);
        double percent = Math.floor(currentAEPower / ItemStorageCellPortable.MAX_POWER * 10000) / 100;
        list.add(I18n.format("gui.appliedenergistics2.StoredEnergy") + ": " + currentAEPower + " AE - " + percent + "%");
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs creativeTab, @Nonnull NonNullList<ItemStack> list) {
        if (!this.isInCreativeTab(creativeTab))
            return;
        list.add(new ItemStack(this));
        ItemStack powered = new ItemStack(this);
        this.injectAEPower(powered, this.getAEMaxPower(powered), Actionable.MODULATE);
        list.add(powered);
    }

    @Nonnull
    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.RARE;
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        NBTTagCompound tag = this.getTag(stack);
        double current = this.getAECurrentPower(stack);
        double toAdd = Math.min(this.getAEMaxPower(stack) - current, amount);
        if (mode == Actionable.MODULATE)
            tag.setDouble("power", current + toAdd);

        return amount - toAdd;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        NBTTagCompound tag = this.getTag(stack);
        double current = this.getAECurrentPower(stack);
        double toRemove = Math.min(current, amount);
        if (mode == Actionable.MODULATE)
            tag.setDouble("power", current - toRemove);
        return toRemove;
    }

    @Override
    public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
        return this.getAECurrentPower(is) >= amount;
    }

    @Override
    public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
        this.extractAEPower(is, amount, Actionable.MODULATE);
        return true;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        return ItemStorageCellPortable.MAX_POWER;
    }

    @Override
    public double getAECurrentPower(ItemStack stack) {
        return this.getTag(stack).getDouble("power");
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack stack) {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - this.getAECurrentPower(stack) / this.getAEMaxPower(stack);
    }

    @Nonnull
    protected NBTTagCompound getTag(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        return Objects.requireNonNull(stack.getTagCompound());
    }
}

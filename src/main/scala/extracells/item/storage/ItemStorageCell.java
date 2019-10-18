package extracells.item.storage;

import java.util.List;
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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

import extracells.item.ItemECBase;
import extracells.models.ModelManager;
import extracells.registries.ItemEnum;

public abstract class ItemStorageCell<T extends IAEStack<T>> extends ItemECBase implements IStorageCell<T> {
    protected final CellDefinition definition;
    protected final IStorageChannel<T> channel;

    public ItemStorageCell(CellDefinition definition, IStorageChannel<T> channel) {
        this.definition = definition;
        this.channel = channel;
        setMaxStackSize(1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag advanced) {
        ICellRegistry cellRegistry = AEApi.instance().registries().cell();
        ICellInventoryHandler<T> cellHandler = cellRegistry.getCellInventory(itemStack, null, channel);
        if (cellHandler == null) {
            list.add("Failed to get cell handler!");
            return;
        }
        ICellInventory<T> cellInventory = cellHandler.getCellInv();
        if (cellInventory == null) {
            list.add("Failed to get cell inventory!");
            return;
        }

        list.add(I18n.format("extracells.tooltip.storage." + definition + ".bytes", cellInventory.getUsedBytes(), cellInventory.getTotalBytes()));
        list.add(I18n.format("extracells.tooltip.storage." + definition + ".types", cellInventory.getStoredItemTypes(), cellInventory.getTotalItemTypes()));
        if (cellInventory.getStoredItemCount() != 0) {
            list.add(I18n.format("extracells.tooltip.storage." + definition + ".content", cellInventory.getStoredItemCount()));
        }

        if (cellHandler.isPreformatted()) {
            list.add(I18n.format("gui.appliedenergistics2.Partitioned") + " - " + I18n.format("gui.appliedenergistics2.Precise"));
        }
    }

    @Override
    @Nonnull
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.RARE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs creativeTab, @Nonnull NonNullList<ItemStack> listSubItems) {
        if (!this.isInCreativeTab(creativeTab))
            return;
        for (StorageType type : definition.cells) {
            listSubItems.add(new ItemStack(this, 1, type.getMeta()));
        }
    }

    @Override
    @Nonnull
    public String getTranslationKey(ItemStack itemStack) {
        StorageType type = definition.cells.fromMeta(itemStack.getItemDamage());
        return "extracells.item.storage." + type.getIdentifier();
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
        }
        ICellRegistry cellRegistry = AEApi.instance().registries().cell();
        ICellInventoryHandler<T> handler = cellRegistry.getCellInventory(itemStack, null, channel);
        if (handler == null) {
            return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
        }
        ICellInventory<T> cellInventory = handler.getCellInv();
        if (cellInventory == null) {
            return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
        }
        if (cellInventory.getUsedBytes() == 0 && player.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(definition.ordinal()))) {
            return new ActionResult<>(EnumActionResult.SUCCESS, ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack.getItemDamage() + definition.componentMetaStart));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(Item item, ModelManager manager) {
        for (StorageType type : definition.cells) {
            manager.registerItemModel(item, type.getMeta(), type.getModelName());
        }
    }

    @Override
    public int getBytes(@Nonnull ItemStack cellItem) {
        StorageType type = this.definition.cells.fromMeta(cellItem.getItemDamage());
        return type.getBytes();
    }

    @Override
    public int getBytesPerType(@Nonnull ItemStack cellItem) {
        return this.getBytes(cellItem) / 128;
    }

    @Override
    public int getTotalTypes(@Nonnull ItemStack cellItem) {
        return 5;
    }

    @Override
    public boolean isBlackListed(@Nonnull ItemStack cellItem, @Nonnull T requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(@Nonnull ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    @Nonnull
    public IStorageChannel<T> getChannel() {
        return this.channel;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        if (is == null) {
            return false;
        }
        return is.getItem() == this;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        if (is == null) {
            return null;
        }
        if (!is.hasTagCompound()) {
            is.setTagCompound(new NBTTagCompound());
        }
        if (is.getTagCompound().hasKey("fuzzyMode")) {
            return FuzzyMode.valueOf(is.getTagCompound().getString("fuzzyMode"));
        }
        is.getTagCompound().setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name());
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        if (is == null) {
            return;
        }
        NBTTagCompound tag;
        if (is.hasTagCompound()) {
            tag = is.getTagCompound();
        } else {
            tag = new NBTTagCompound();
        }
        tag.setString("fuzzyMode", fzMode.name());
        is.setTagCompound(tag);

    }
}

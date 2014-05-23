package extracells.item;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEFluidStack;
import extracells.inventory.HandlerItemStorageFluid;
import extracells.network.GuiHandler;
import extracells.registries.ItemEnum;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class ItemStorageFluid extends Item implements ICellHandler {

    public static final String[] suffixes = {"1k", "4k", "16k", "64k", "256k", "1024k", "4096k"};

    public static final int[] spaces = {1024, 4096, 16348, 65536, 262144, 1048576, 4194304};

    private IIcon[] icons;

    public ItemStorageFluid() {
        AEApi.instance().registries().cell().addCellHandler(this);
        setMaxStackSize(1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public IIcon getIconFromDamage(int dmg) {
        int j = MathHelper.clamp_int(dmg, 0, suffixes.length);
        return icons[j];
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[suffixes.length];

        for (int i = 0; i < suffixes.length; ++i) {
            icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i]);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List listSubItems) {
        for (int i = 0; i < suffixes.length; ++i) {
            listSubItems.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return "extracells.item.storage.fluid." + suffixes[itemStack.getItemDamage()];
    }

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() == this;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack is, StorageChannel channel) {
        if (channel == StorageChannel.ITEMS || is.getItem() != this)
            return null;
        return new HandlerItemStorageFluid(is);
    }

    @Override
    public IIcon getTopTexture() {
        return TextureManager.ITEM_STORAGE_FLUID.getTexture();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
        if (chan != StorageChannel.FLUIDS)
            return;
        IStorageMonitorable monitorable = null;
        if (chest != null)
            monitorable = ((IMEChest) chest).getMonitorable(ForgeDirection.UNKNOWN, new BaseActionSource());
        if (monitorable != null)
            GuiHandler.launchGui(GuiHandler.getGuiId(0), player, monitorable.getFluidInventory());
    }

    @Override
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        if (handler == null)
            return 0;

        HandlerItemStorageFluid inventory = (HandlerItemStorageFluid) handler;
        if (inventory.freeBytes() == 0)
            return 3;
        if (inventory.isPreformatted() || inventory.usedTypes() == inventory.totalBytes())
            return 2;

        return 1;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return 0;
    }

    public int maxTypes(ItemStack unused) {
        return 5;
    }

    public int maxStorage(ItemStack is) {
        return spaces[Math.max(0, is.getItemDamage())];
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, StorageChannel.FLUIDS);
        if (!(handler instanceof HandlerItemStorageFluid))
            return;
        HandlerItemStorageFluid cellHandler = (HandlerItemStorageFluid) handler;
        Boolean partitioned = cellHandler.isPreformatted();
        long usedBytes = cellHandler.usedBytes();

        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), usedBytes / 250, cellHandler.totalBytes() / 250));
        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
        if (usedBytes != 0)
            list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes));

        if (partitioned) {
            list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        if (!entityPlayer.isSneaking())
            return itemStack;
        IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, StorageChannel.FLUIDS);
        if (!(handler instanceof HandlerItemStorageFluid))
            return itemStack;
        HandlerItemStorageFluid cellHandler = (HandlerItemStorageFluid) handler;
        if (cellHandler.usedBytes() == 0 && entityPlayer.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(1)))
            return ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack.getItemDamage() + 4);
        return itemStack;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }
}

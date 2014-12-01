package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEFluidStack;
import extracells.inventory.HandlerItemStorageFluid;
import extracells.network.GuiHandler;
import extracells.registries.ItemEnum;
import extracells.render.TextureManager;
import extracells.util.inventory.ECFluidFilterInventory;
import extracells.util.inventory.ECPrivateInventory;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class ItemStorageFluid extends Item implements ICellHandler, ICellWorkbenchItem {

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
    public IMEInventoryHandler getCellInventory(ItemStack itemStack, ISaveProvider saveProvider, StorageChannel channel) {
        if (channel == StorageChannel.ITEMS || itemStack.getItem() != this) {
            return null;
        }
        return new HandlerItemStorageFluid(itemStack, saveProvider, getFilter(itemStack));
    }

    @Override
    public IIcon getTopTexture_Light() {
        return TextureManager.TERMINAL_FRONT.getTextures()[2];
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return TextureManager.TERMINAL_FRONT.getTextures()[1];
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return TextureManager.TERMINAL_FRONT.getTextures()[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {
        if (chan != StorageChannel.FLUIDS) {
            return;
        }
        IStorageMonitorable monitorable = null;
        if (chest != null) {
            monitorable = ((IMEChest) chest).getMonitorable(ForgeDirection.UNKNOWN, new PlayerSource(player, chest));
        }
        if (monitorable != null) {
            GuiHandler.launchGui(GuiHandler.getGuiId(0), player, monitorable.getFluidInventory());
        }
    }

    @Override
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        if (handler == null) {
            return 0;
        }

        HandlerItemStorageFluid inventory = (HandlerItemStorageFluid) handler;
        if (inventory.freeBytes() == 0) {
            return 3;
        }
        if (inventory.isPreformatted() || inventory.usedTypes() == inventory.totalBytes()) {
            return 2;
        }

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
        IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS);
        if (!(handler instanceof HandlerItemStorageFluid)) {
            return;
        }
        HandlerItemStorageFluid cellHandler = (HandlerItemStorageFluid) handler;
        Boolean partitioned = cellHandler.isPreformatted();
        long usedBytes = cellHandler.usedBytes();

        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), usedBytes / 250, cellHandler.totalBytes() / 250));
        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
        if (usedBytes != 0) {
            list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes));
        }

        if (partitioned) {
            list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        if (!entityPlayer.isSneaking()) {
            return itemStack;
        }
        IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS);
        if (!(handler instanceof HandlerItemStorageFluid)) {
            return itemStack;
        }
        HandlerItemStorageFluid cellHandler = (HandlerItemStorageFluid) handler;
        if (cellHandler.usedBytes() == 0 && entityPlayer.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(1))) {
            return ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack.getItemDamage() + 4);
        }
        return itemStack;
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
		return new ECFluidFilterInventory("configFluidCell", 64, is);
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
		NBTTagCompound tag;
		if(is.hasTagCompound())
			tag = is.getTagCompound();
		else
			tag = new NBTTagCompound();
		tag.setString("fuzzyMode", fzMode.name());
		is.setTagCompound(tag);
		
	}
	
	private ItemStack[] getFilter(ItemStack stack){
		ECFluidFilterInventory inventory = new ECFluidFilterInventory("", 63, stack);
		return inventory.slots;
	}
}

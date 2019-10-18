package extracells.item.storage;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.storage.data.IAEFluidStack;

import extracells.api.ECApi;
import extracells.api.IPortableFluidStorageCell;
import extracells.inventory.ECFluidFilterInventory;
import extracells.inventory.InventoryPlain;
import extracells.models.ModelManager;
import extracells.util.StorageChannels;

public class ItemStorageCellPortableFluid extends ItemStorageCellPortable<IAEFluidStack> implements IPortableFluidStorageCell {

    public ItemStorageCellPortableFluid() {
        super(CellDefinition.FLUID, StorageChannels.FLUID());
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        return new ActionResult<>(EnumActionResult.SUCCESS, ECApi.instance().openPortableFluidCellGui(player, hand, world));
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack itemStack) {
        return "extracells.item.storage.fluid.portable";
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack is) {
        return new InvWrapper(new InventoryPlain("configInventory", 0, 64));
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack is) {
        return new InvWrapper(new ECFluidFilterInventory("configFluidCell", 63, is));
    }

    @Override
    public void registerModel(Item item, ModelManager manager) {
        manager.registerItemModel(item, 0, "storage/fluid/portable");
    }
}

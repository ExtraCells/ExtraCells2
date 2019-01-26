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

import extracells.api.ECApi;
import extracells.api.IPortableGasStorageCell;
import extracells.api.gas.IAEGasStack;
import extracells.inventory.ECFluidFilterInventory;
import extracells.inventory.InventoryPlain;
import extracells.models.ModelManager;
import extracells.util.StorageChannels;

/**
 * @author BrockWS
 */
public class ItemStorageCellPortableGas extends ItemStorageCellPortable<IAEGasStack> implements IPortableGasStorageCell {

    public ItemStorageCellPortableGas() {
        super(CellDefinition.GAS, StorageChannels.GAS());
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        return new ActionResult<>(EnumActionResult.SUCCESS, ECApi.instance().openPortableGasCellGui(player, hand, world));
    }

    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return "extracells.item.storage.gas.portable";
    }

    @Override
    public void registerModel(Item item, ModelManager manager) {
        manager.registerItemModel(item, 0, "storage/gas/portable");
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack is) {
        return new InvWrapper(new InventoryPlain("configInventory", 0, 64));
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack is) {
        return new InvWrapper(new ECFluidFilterInventory("configGasCell", 63, is));
    }
}

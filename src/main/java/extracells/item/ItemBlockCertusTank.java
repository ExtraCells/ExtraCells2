package extracells.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemBlockCertusTank extends ItemBlock {

    public ItemBlockCertusTank(Block block) {
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        if (itemstack != null) {
            if (itemstack.hasTagCompound()) {
                try {
                    FluidStack fluidInTank = FluidStack.loadFluidStackFromNBT(itemstack.getTagCompound().getCompoundTag("tileEntity"));

                    if (fluidInTank != null && fluidInTank.getFluid() != null) {
                        return StatCollector.translateToLocal(getUnlocalizedName(itemstack)) + " - " + fluidInTank.getFluid().getLocalizedName();
                    }
                } catch (Throwable ignored) {
                }
            }
            return StatCollector.translateToLocal(getUnlocalizedName(itemstack));
        }
        return "";
    }

    @SuppressWarnings(
            {"rawtypes", "unchecked"})
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack != null && stack.hasTagCompound()) {
            if (FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")) != null)
                list.add(FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("tileEntity")).amount + "mB");
        }
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        if (!world.setBlock(x, y, z, field_150939_a, metadata, 3)) {
            return false;
        }

        if (world.getBlock(x, y, z) == field_150939_a) {
            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
        }

        if (stack != null && stack.hasTagCompound()) {
            ((TileEntityCertusTank) world.getTileEntity(x, y, z)).readFromNBTWithoutCoords(stack.getTagCompound().getCompoundTag("tileEntity"));
        }
        return true;
    }
}
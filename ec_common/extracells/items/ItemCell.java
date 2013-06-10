package extracells.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import appeng.api.IAEItemStack;
import appeng.api.Materials;
import appeng.api.Util;
import appeng.api.me.items.IStorageCell;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.extracells;

public class ItemCell extends Item implements IStorageCell {

    // Item Names
    public static final String[] localized_names = new String[] {
            "ME 256K Storage", "ME 1M Storage", "ME 4M Storage",
            "ME 16M Storage", "ME Block Container", "Adjustable ME Storage" };
    public static final String[] meta_names = new String[] { "item256kCell",
            "item1024kCell", "item4096kCell", "item16348kCell",
            "itemBlockContainer", "itemAdjustableCell" };
    // Bytes
    public static final int[] bytes_cell = new int[] { 262144, 1048576,
            4194304, 16777216, 65536 };
    public static final int[] types_cell = new int[] { 63, 63, 63, 63, 1, 12 };
    // Icons
    @SideOnly(Side.CLIENT)
    private Icon[] icons;

    public ItemCell(int id) {
        super(id);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setCreativeTab(extracells.ModTab);
    }

    @SideOnly(Side.CLIENT)
    public Icon getIconFromDamage(int par1) {
        int j = MathHelper.clamp_int(par1, 0, 5);
        return this.icons[j];
    }

    @Override
    public void registerIcons(IconRegister par1IconRegister) {
        this.icons = new Icon[meta_names.length];

        for (int i = 0; i < meta_names.length; ++i) {
            this.icons[i] = par1IconRegister.registerIcon("extracells:"
                    + meta_names[i]);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 5);
        return super.getUnlocalizedName() + "." + meta_names[i];
    }

    @Override
    public String getItemDisplayName(ItemStack stack) {
        long used_bytes = Util.getCellRegistry().getHandlerForCell(stack)
                .usedBytes();
        if (stack.getItemDamage() == 4) {
            if (used_bytes != 0) {
                return "ME Block Container - "
                        + Util.getCellRegistry().getHandlerForCell(stack)
                                .getAvailableItems().getItems().get(0)
                                .getDisplayName();
            } else {
                return "Empty ME Block Container";
            }
        } else {
            return ItemCell.localized_names[stack.getItemDamage()];
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SideOnly(Side.CLIENT)
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs,
            List par3List) {
        for (int j = 0; j < 6; ++j) {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list,
            boolean par4) {
        long used_bytes = Util.getCellRegistry().getHandlerForCell(stack)
                .usedBytes();
        long total_bytes = Util.getCellRegistry().getHandlerForCell(stack)
                .totalBytes();
        long used_types = Util.getCellRegistry().getHandlerForCell(stack)
                .storedItemTypes();
        long total_types = Util.getCellRegistry().getHandlerForCell(stack)
                .getTotalItemTypes();
        if (stack.getItemDamage() != 4) {
            list.add(used_bytes + " of " + total_bytes + " Bytes Used");
            list.add(used_types + " of " + total_types + " Types");
        } else if (stack.getItemDamage() == 4) {
            if (used_bytes != 0) {
                list.add("Block: "
                        + Util.getCellRegistry().getHandlerForCell(stack)
                                .getAvailableItems().getItems().get(0)
                                .getDisplayName());
            } else {
                list.add("Block: -");
            }
            list.add(used_bytes + " of " + total_bytes + " Bytes Used");
        }
    }

    @Override
    public int getBytes(ItemStack i) {
        if (i.getItemDamage() == 5) {
            if (i.hasTagCompound()) {
                return i.getTagCompound().getInteger("costum_size");
            } else {
                return 0;
            }
        } else {
            return bytes_cell[i.getItemDamage()];
        }
    }

    @Override
    public int BytePerType(ItemStack i) {
        if (i.getItemDamage() == 5) {
            if (i.hasTagCompound()) {
                if (Math.round(i.getTagCompound().getInteger("costum_types") / 128) == 0) {
                    return 1;
                } else {
                    return Math.round(i.getTagCompound().getInteger(
                            "costum_types") / 128);
                }
            } else {
                return 1;
            }
        } else {
            return bytes_cell[i.getItemDamage()] / 128;
        }
    }

    public int getTotalTypes(ItemStack i) {
        if (i.getItemDamage() == 5) {
            if (i.hasTagCompound()) {
                return i.getTagCompound().getInteger("costum_types");
            } else {
                return 0;
            }
        } else {
            return types_cell[i.getItemDamage()];
        }
    }

    public boolean isBlackListed(ItemStack cellItem,
            IAEItemStack requsetedAddition) {
        return false;
    }

    public EnumRarity getRarity(ItemStack par1) {
        return EnumRarity.epic;
    }

    @ForgeSubscribe
    @Override
    public ItemStack onItemRightClick(ItemStack i, World w, EntityPlayer p) {
        if (p.isSneaking()) {
            if (i.getItemDamage() != 4 || i.getItemDamage() != 5) {
                if (Util.getCellRegistry().getHandlerForCell(i)
                        .storedItemCount() == 0) {
                    p.inventory.decrStackSize(p.inventory.currentItem, 1);
                    p.inventory.addItemStackToInventory(new ItemStack(
                            extracells.Cluster, 1, i.getItemDamage()));
                    p.inventory
                            .addItemStackToInventory(Materials.matStorageCellHouseing
                                    .copy());
                }
            }
        }
        return i;

    }

    int sleep = 0;

    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player,
            World world, int x, int y, int z, int side, float xOffset,
            float yOffset, float zOffset) {
        if (itemstack.getItemDamage() == 4) {
            ForgeDirection face = ForgeDirection.getOrientation(side);
            if (world.getBlockId(x + face.offsetX, y + face.offsetY, z
                    + face.offsetZ) == 0
                    && Util.getCellRegistry().getHandlerForCell(itemstack)
                            .storedItemTypes() != 0) {
                IAEItemStack request = Util.createItemStack(Util
                        .getCellRegistry().getHandlerForCell(itemstack)
                        .getAvailableItems().getItems().get(0).copy());
                request.setStackSize(1);
                ItemStack block = request.getItemStack();
                if (block.getItem() instanceof ItemBlock) {
                    player.worldObj
                            .setBlock(
                                    x + face.offsetX,
                                    y + face.offsetY,
                                    z + face.offsetZ,
                                    Util.getCellRegistry()
                                            .getHandlerForCell(itemstack)
                                            .getAvailableItems().getItems()
                                            .get(0).itemID, appeng.api.Util
                                            .getCellRegistry()
                                            .getHandlerForCell(itemstack)
                                            .getAvailableItems().getItems()
                                            .get(0).getItemDamage(), 3);
                    Util.getCellRegistry().getHandlerForCell(itemstack)
                            .extractItems(request);
                    return true;
                } else {
                    if (sleep == 0) {
                        player.sendChatToPlayer("You can't place Items! Put a Block into the BLOCK-Container");
                        sleep++;
                    } else if (sleep == 10) {
                        sleep = 0;
                    } else {
                        sleep++;
                    }
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}

package extracells.tile;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySolderingStation extends TileEntity {
    private ArrayList<String> users;

    public TileEntitySolderingStation() {
        users = new ArrayList<String>();
    }

    public void addUser(String name) {
        if (!users.contains(name)) {
            users.add(name);
        }
    }

    public void remUser(String name) {
        users.remove(name);
    }

    public void updateData(String user, int size, int types) {
        if (users.contains(user)) {
            EntityPlayer p = worldObj.getPlayerEntityByName(user);
            if(p.getHeldItem() != null) {
                ItemStack stack = p.getHeldItem();
                if(!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound tag = stack.getTagCompound();
                tag.setInteger("costum_size", size);
                tag.setInteger("costum_types", types);
            }
        }
    }

}

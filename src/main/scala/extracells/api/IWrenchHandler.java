package extracells.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

public interface IWrenchHandler {

    boolean canWrench(ItemStack item, EntityPlayer user, RayTraceResult rayTraceResult, EnumHand hand);

    void wrenchUsed(ItemStack item, EntityPlayer user, RayTraceResult rayTraceResult, EnumHand hand);

}
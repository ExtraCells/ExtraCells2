package extracells.api;


import appeng.api.features.INetworkEncodable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IWirelessGasFluidTermHandler extends INetworkEncodable {

    boolean canHandle(ItemStack is);

    boolean hasPower(EntityPlayer player, double amount, ItemStack is);

    boolean isItemNormalWirelessTermToo(ItemStack is);

    boolean usePower(EntityPlayer player, double amount, ItemStack is);
}

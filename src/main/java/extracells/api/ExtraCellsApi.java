package extracells.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ExtraCellsApi {
	
	public String getVerion();
	
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world, int x, int y, int z, Long key);
}

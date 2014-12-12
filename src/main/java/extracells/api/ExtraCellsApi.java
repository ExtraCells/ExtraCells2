package extracells.api;

import extracells.api.definitions.IBlockDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ExtraCellsApi {

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	public String getVerion();

	public String getVersion();

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	public void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	public void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler);
	
	public IWirelessFluidTermHandler getWirelessFluidTermHandler(ItemStack is);
	
	public boolean isWirelessFluidTerminal(ItemStack is);
	
	@Deprecated
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world, int x, int y, int z, Long key);
	
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world);
	
	public ItemStack openPortableCellGui(EntityPlayer player, ItemStack stack, World world);
	
	public IItemDefinition items();
	
	public IBlockDefinition blocks();
	
	public IPartDefinition parts();
}

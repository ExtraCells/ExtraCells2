package thaumicenergistics.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Contains misc functionality intended to help other moders interact with ThE.
 * 
 * @author Nividica
 * 
 */
public interface IThEInteractionHelper
{
	/**
	 * Converts an amount of milibuckets to an amount of Essentia.
	 * 
	 * @return
	 */
	public abstract long convertEssentiaAmountToFluidAmount( long essentiaAmount );

	/**
	 * Converts an amount of Essentia to an amount of milibuckets.
	 * 
	 * @return
	 */
	public abstract long convertFluidAmountToEssentiaAmount( long milibuckets );

	/**
	 * Returns the Arcane Crafting Terminals GUI class.
	 */
	@SideOnly(Side.CLIENT)
	public abstract Class getArcaneCraftingTerminalGUIClass();

	/**
	 * Opens the wireless gui for the specified player.
	 * The item the player is holding is used for the settings and power.
	 * 
	 * @param player
	 */
	public abstract void openWirelessTerminalGui( final EntityPlayer player, final IThEWirelessEssentiaTerminal terminalInterface );

	/**
	 * Attempts to set the Arcane Crafting Terminals recipe to the items
	 * specified for the current player.
	 * The items array should be of size 9. Items will be placed in the crafting
	 * grid according
	 * to index where 0 = Top-Left, 1 = Top-Middle, 2 = Top-Right, etc.
	 * Nulls are allowed.
	 */
	@SideOnly(Side.CLIENT)
	public abstract void setArcaneCraftingTerminalRecipe( ItemStack[] items );
}

package thaumicenergistics.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * Provides the required functionality of a wireless terminal.
 * Presumably this interface would be implemented on an Item, but that is
 * not a requirement.
 * 
 * @author Nividica
 * 
 */
public interface IThEWirelessEssentiaTerminal
	extends INetworkEncodable, IAEItemPowerStorage
{
	/**
	 * Gets the tag used to store the terminal data.
	 * 
	 * @param terminalItemstack
	 * @return
	 */
	public NBTTagCompound getWETerminalTag( ItemStack terminalItemstack );
}

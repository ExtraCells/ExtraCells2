package appeng.api;

import net.minecraft.nbt.NBTBase;

/**
 * Don't cast this... either compare with it, or copy it.
 * 
 * Don't Implement.
 */
public interface IAETagCompound {
	
	/**
	 * Create a copy ( the copy will not be a IAETagCompount, it will be a NBTTagCompound. )
	 * @return
	 */
	NBTBase copy();
	
	/**
	 * compare to other NBTTagCompounds or IAETagCompounds
	 * @param a
	 * @return true, if they are the same.
	 */
	@Override
	boolean equals( Object a );
	
}

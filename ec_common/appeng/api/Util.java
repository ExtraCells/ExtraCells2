package appeng.api;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import appeng.api.exceptions.AppEngException;
import appeng.api.me.util.IAssemblerPattern;
import appeng.api.me.util.IMEInventory;
import appeng.api.me.util.IMEInventoryUtil;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Returns useful stuff of various sorts to access internal features and stuff, the meat of the important stuff is accessed here...
 */
public class Util
{
	static private Method isBlankPattern = null;
	static private Method isAssemblerPattern = null;
    static private Method getAssemblerPattern = null;

    static private ICellRegistry cellReg;
    static private ILocateableRegistry locReg;
    static private IExternalStorageRegistry externalStorageReg;
    static private IGrinderRecipeManager grinderRecipeManager;
    static private ISpecialComparisonRegistry specialComparisonRegistry;
    
    static private Method isCell = null;
    static private Method GetCell = null;
    static private Method addBasicBlackList = null;
    static private Method getLocateableBySerial = null;
    static private Method getIMEInventoryUtil = null;
    static private Method createItemList = null;
    static private Method createItemStack = null;
    static private Method addItems = null;
    static private Method extractItems = null;
    
    /**
     * Called by AE during initialization.
     * ** DO NOT CALL THIS **
     */
    public static void initAPI() throws AppEngException, NoSuchMethodException, SecurityException
    {
    	createItemStack = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("createItemStack", ItemStack.class );
    	if ( createItemStack == null ) throw new AppEngException("api.createItemStack");
    	isAssemblerPattern = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.AssemblerPatternInventory").getMethod("isAssemblerPattern", ItemStack.class);	
    	if ( isAssemblerPattern == null ) throw new AppEngException("api.isAssemblerPattern");
    	addItems = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.util.Platform").getMethod("addItems", IMEInventory.class, ItemStack.class );
    	if ( addItems == null ) throw new AppEngException("api.addItems");
    	extractItems = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.util.Platform").getMethod("extractItems", IMEInventory.class, ItemStack.class );
    	if ( extractItems == null ) throw new AppEngException("api.extractItems");
    	createItemList = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("createItemList");
    	if ( createItemList == null ) throw new AppEngException("api.createItemList");
    	getIMEInventoryUtil = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getIMEInventoryUtil", IMEInventory.class );
    	if ( getIMEInventoryUtil == null ) throw new AppEngException("api.getIMEInventoryUtil");
    	GetCell = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.CellInventory").getMethod("getCell", ItemStack.class);
    	if ( GetCell == null ) throw new AppEngException("api.GetCell");
    	addBasicBlackList = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.CellInventory").getMethod("addBasicBlackList", int.class, int.class );
    	if ( addBasicBlackList == null ) throw new AppEngException("api.addBasicBlackList");
    	isBlankPattern = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.AssemblerPatternInventory").getMethod("isBlankPattern", ItemStack.class);
    	if ( isBlankPattern == null ) throw new AppEngException("api.isBlankPattern");
    	getAssemblerPattern = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.AssemblerPatternInventory").getMethod("getAssemblerPattern", ItemStack.class);
        if ( getAssemblerPattern == null ) throw new AppEngException("api.getAssemblerPattern");
    	isCell = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.me.CellInventory").getMethod("isCell", ItemStack.class);
        if ( isCell == null ) throw new AppEngException("api.isCell");
        getLocateableBySerial = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getLocateableBySerial", Long.class);
        if ( getLocateableBySerial == null ) throw new AppEngException("api.getLocateableBySerial");    	
        
        Method getExternalStorageRegistry = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getExternalStorageRegistry" );
    	if ( getExternalStorageRegistry == null ) throw new AppEngException("api.getExternalStorageRegistry");
    	Method  getCellRegistry = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getCellRegistry" );
    	if ( getCellRegistry == null ) throw new AppEngException("api.getCellRegistry");
    	Method getGrinderRecipeManage = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getGrinderRecipeManage" );
    	if ( getGrinderRecipeManage == null ) throw new AppEngException("api.getGrinderRecipeManage");
    	Method getSpecialComparsonRegistry = ReflectionHelper.getClass(Util.class.getClassLoader(), "appeng.common.AppEngApi").getMethod("getSpecialComparsonRegistry" );
    	if ( getSpecialComparsonRegistry == null ) throw new AppEngException("api.getSpecialComparsonRegistry");
    	
    	try {
			externalStorageReg = (IExternalStorageRegistry)getExternalStorageRegistry.invoke(null);
	    	cellReg = (ICellRegistry)getCellRegistry.invoke(null);
	        grinderRecipeManager = (IGrinderRecipeManager)getGrinderRecipeManage.invoke(null);
	        specialComparisonRegistry = (ISpecialComparisonRegistry)getSpecialComparsonRegistry.invoke(null);
		} catch (Exception e) {
			 throw new AppEngException("api.establish.registiries");
		}
    }
    
    /**
     * Find an object by its serial.
     * @param ser
     * @return LocatedObject or null
     */
    public static Object getLocateableBySerial( long ser )
    {
        try
        {
            return getLocateableBySerial.invoke(null,ser);
        }
        catch (Exception e)
        {
            return null;
        }    	
    }
    
    /**
     * Creates a new AEItemstack.
     * @param is
     * @return newly generated AE Itemstack
     */
    public static IAEItemStack createItemStack( ItemStack is )
    {
        try
        {
            return (IAEItemStack)createItemStack.invoke(null,is);
        }
        catch (Exception e)
        {
            return null;
        }    	
    }
    
    /**
     * Simple Wrapper of the insertion process..
     * @param inv
     * @param is
     * @return ItemsNotInserted or null
     */
    public static ItemStack addItemsToInv( IMEInventory inv, ItemStack is )
    {
        try
        {
            return (ItemStack)addItems.invoke(null,inv,is);
        }
        catch (Exception e)
        {
            return null;
        }    	
    }
    
    /**
     * Simple Wrapper of the extraction process
     * @param inv
     * @param is
     * @return ItemsExtracted or null
     */
    public static ItemStack extractItemsFromInv( IMEInventory inv, ItemStack is )
    {
        try
        {
            return (ItemStack)extractItems.invoke(null,inv,is);
        }
        catch (Exception e)
        {
            return null;
        }    	
    }
    
    /**
     * Create a new Blank ItemList
     * @return new itemlist.
     */
    public static IItemList createItemList()
    {
        try
        {
            return (IItemList)createItemList.invoke(null);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * creates a new IMEInventoryUtil, only useful if you want to use the fancy get items by recipe functionaility.
     * @param ime
     * @return created InvUtil
     */
    public static IMEInventoryUtil getIMEInventoryUtil( IMEInventory ime )
    {
        try
        {
            return (IMEInventoryUtil)getIMEInventoryUtil.invoke(null,ime);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * Gets the instance of the special comparison registry ( Bees / Trees ) that sort of stuff
     * @return specialComparisonRegistry
     */
    public static ISpecialComparisonRegistry getSpecialComparisonRegistry()
    {
    	return specialComparisonRegistry;
    }
    
    /**
     * Gets the instance of the external storage registry - Storage Bus
     * @return externStorgeRegitry
     */
    public static IExternalStorageRegistry getExternalStorageRegistry()
    {
    	return externalStorageReg;
    }
    
    /**
     * Gets the instance of the Cell Registry
     * @return returns the cell registry
     */
    public static ICellRegistry getCellRegistry()
    {
    	return cellReg;
    }
    
    /**
     * Gets instance for the grinder recipe manager.
     * @return the grinder manager instance.
     */
    public static IGrinderRecipeManager getGrinderRecipeManage()
    {
    	return grinderRecipeManager;
    }

    /** Is it a Blank Pattern? */
    public static Boolean isBlankPattern(ItemStack i)
    {
        try
        {
            return (Boolean)isBlankPattern.invoke(null, i);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /** Is it an IAssemblerPattern? */
    public static Boolean isAssemblerPattern(ItemStack i)
    {
        try
        {
            return (Boolean)isAssemblerPattern.invoke(null, i);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /** Gets the IAssemblerPattern of the Assembly Pattern. */
    public static IAssemblerPattern getAssemblerPattern(ItemStack i)
    {
        try
        {
            return (IAssemblerPattern)getAssemblerPattern.invoke(null, i);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /** Is it a IStorageCell, this will only return true for IStoreCells and not custom cells, you should probobly not use it unless you have a specific case. */
    public static Boolean isBasicCell(ItemStack i)
    {
        try
        {
            return (Boolean)isCell.invoke(null, i);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /** 
     * if the item is a ME Compatible Storage Cell of any type.
     * @param i
     * @return true, if it is a storage call.
     */
    public static Boolean isCell(ItemStack i)
    {
    	return getCellRegistry().isCellHandled( i );
    }
    
    /**
     * Gets the Interface to insert/extract from the Storage Cell for the item.
     * @param i
     * @return newly procured cell handler.
     */
    public static IMEInventory getCell(ItemStack i)
    {
    	return getCellRegistry().getHandlerForCell( i );
    }
    
    /**
     * Lets you access internal storage of IStorageCell's
     * @param i
     * @return only works with Basic Cells, not custom ones, suggested not to use.
     */
    public static IMEInventory getBasicCell(ItemStack i)
    {
        try
        {
            if (i == null)
            {
                return null;
            }
            
            return (IMEInventory)GetCell.invoke(null, i);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * Lets you blast list a specific item from being stored in basic cells, this works on any mod cells that use IStorageCell as well.
     * @param ItemID
     * @param Meta
     */
    public static void addBasicBlackList( int ItemID, int Meta )
    {
        try
        {
            addBasicBlackList.invoke(null, ItemID, Meta );
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
    
}

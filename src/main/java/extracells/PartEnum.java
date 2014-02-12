package extracells;

import appeng.api.config.Upgrades;
import extracells.part.*;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public enum PartEnum
{
	FLUIDEXPORT("fluid.export", PartFluidExport.class, "fluid.IO"),
	FLUIDIMPORT("fluid.import", PartFluidImport.class, "fluid.IO"),
	FLUIDSTORAGE("fluid.storage", PartFluidStorage.class),
	FLUIDTERMINAL("fluid.terminal", PartFluidTerminal.class),
	FLUIDLEVELEMITTER("fluid.levelemitter", PartFluidLevelEmitter.class);

	String unlocalizedName;
	Class<? extends PartECBase> partClass;
	String groupName;

	PartEnum(String _unlocalizedName, Class<? extends PartECBase> _partClass, String _groupName)
	{
		unlocalizedName = "extracells.part." + _unlocalizedName;
		partClass = _partClass;
		groupName = "extracells." + _groupName;
	}

	PartEnum(String _unlocalizedName, Class<? extends PartECBase> _partClass)
	{
		this(_unlocalizedName, _partClass, null);
	}

	public PartECBase newInstance(ItemStack partStack) throws IllegalAccessException, InstantiationException
	{
		PartECBase partECBase = partClass.newInstance();
		partECBase.initializePart(partStack);
		return partECBase;
	}

	public String getUnlocalizedName()
	{
		return unlocalizedName;
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal(unlocalizedName);
	}

	public String getGroupName()
	{
		return groupName;
	}

	public Class<? extends PartECBase> getPartClass()
	{
		return partClass;
	}

	public static int getPartID(Class<? extends PartECBase> partClass)
	{
		for (int i = 0; i < values().length; i++)
		{
			if (values()[i].getPartClass() == partClass)
				return i;
		}
		return -1;
	}

	public static int getPartID(PartECBase partECBase)
	{
		return getPartID(partECBase.getClass());
	}

	public List<Pair<Upgrades, Integer>> getUpgrades() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		Method getPossibleUpgrades = partClass.getMethod("getPossibleUpgrades");
		return (List<Pair<Upgrades, Integer>>) getPossibleUpgrades.invoke(null);
	}
}

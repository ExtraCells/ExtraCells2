package extracells.util;

import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolverResult;
import extracells.registries.ItemEnum;

public class NameHandler implements ISubItemResolver
{
	@Override
	public Object resolveItemByName(String namespace, String fullName)
	{
		if (!namespace.equals("extracells"))
			return null;

		// Fluid Cells
		if (fullName.equals("fluidCell1k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 0);
		if (fullName.equals("fluidCell4k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 1);
		if (fullName.equals("fluidCell16k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 2);
		if (fullName.equals("fluidCell64k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 3);
		if (fullName.equals("fluidCell256k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 4);
		if (fullName.equals("fluidCell1024k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 5);
		if (fullName.equals("fluidCell4096k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 6);
		if (fullName.equals("fluidCell16384k"))
			return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 7);

		// Physical Cells
		if (fullName.equals("physCell256k"))
			return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 0);
		if (fullName.equals("physCell1024k"))
			return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 1);
		if (fullName.equals("physCell4096k"))
			return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 2);
		if (fullName.equals("physCell16384k"))
			return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 3);

		return null;
	}
}
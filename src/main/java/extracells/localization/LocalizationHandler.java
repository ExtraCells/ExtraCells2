package extracells.localization;

import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.Extracells;

public class LocalizationHandler
{
	public static void loadLanguages()
	{
		for (final Localization localeFile : Localization.values())
		{
			Extracells.proxy.loadLocalization(localeFile.filename(), localeFile.locale());
		}
	}
}
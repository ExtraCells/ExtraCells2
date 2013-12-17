package extracells.localization;

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
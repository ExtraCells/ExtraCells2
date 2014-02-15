package extracells.render;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public enum TextureManager
{
	BUS_SIDE("bus_side"),
	BUS_BORDER("bus_border"),
	BUS_COLOR("bus_color_border", "bus_color_point"),
	EXPORT_FRONT("export_front_1", "export_front_2"),
	IMPORT_FRONT("import_front_1", "import_front_2"),
	STORAGE_FRONT("storage_front_1", "storage_front_2"),
	TERMINAL_FRONT("terminal_front_1", "terminal_front_2", "terminal_front_3"),
	PANE_FRONT("pane_front_1", "pane_front_2", "pane_front_3");

	private String[] textureNames;
	private IIcon[] textures;

	TextureManager(String... _textureName)
	{
		textureNames = _textureName;
		textures = new IIcon[textureNames.length];
	}

	public void registerTexture(TextureMap textureMap)
	{
		for (int i = 0; i < textureNames.length; i++)
		{
			textures[i] = textureMap.registerIcon("extracells:part/" + textureNames[i]);
		}
	}

	public IIcon[] getTextures()
	{
		return textures;
	}

	public IIcon getTexture()
	{
		return textures[0];
	}
}

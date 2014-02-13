package extracells.render;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public enum TextureManager
{
	BUS_SIDE("bus_side"),
	BUS_BORDER("bus_border"),
	BUS_COLOR("bus_color_border", "bus_color_point"),
	EXPORT_FRONT("export_front"),
	IMPORT_FRONT("import_front"),
	STORAGE_FRONT("storage_front"),
	TERMINAL_FRONT("terminal_layer1", "terminal_layer2", "terminal_layer3");

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

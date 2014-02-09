package extracells;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public enum TextureManager
{
	BUS_SIDE("bus_side"),
	EXPORT_FRONT("export_front"),
	IMPORT_FRONT("import_front"),
	STORAGE_FRONT("storage_front");

	private String textureName;
	private IIcon texture;

	TextureManager(String _textureName)
	{
		textureName = _textureName;
	}

	public void registerTexture(TextureMap textureMap)
	{
		texture = textureMap.registerIcon("extracells:part/" + textureName);
	}

	public IIcon getTexture()
	{
		return texture;
	}
}

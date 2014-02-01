package extracells.proxy;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import extracells.TextureManager;
import net.minecraftforge.event.ForgeSubscribe;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy
{
	public ClientProxy()
	{
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void RegisterRenderers()
	{
	}

	@ForgeSubscribe
	public void registerTextures(TextureStitchEvent.Pre textureStitchEvent)
	{
		TextureMap map = textureStitchEvent.map;
		if (map.textureType == 0)
		{
			for (TextureManager currentTexture : TextureManager.values())
			{
				currentTexture.registerTexture(map);
			}
		}
	}
}

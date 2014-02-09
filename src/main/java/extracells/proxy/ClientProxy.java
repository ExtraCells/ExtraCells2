package extracells.proxy;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import extracells.BlockEnum;
import extracells.TextureManager;
import extracells.render.item.ItemRendererCertusTank;

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
		MinecraftForgeClient.registerItemRenderer(BlockEnum.CERTUSTANK.getBlockInstance().blockID, new ItemRendererCertusTank());
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

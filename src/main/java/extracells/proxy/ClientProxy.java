package extracells.proxy;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import extracells.BlockEnum;
import extracells.TextureManager;
import extracells.render.item.ItemRendererCertusTank;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

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
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.CERTUSTANK.getBlock()), new ItemRendererCertusTank());
	}

	@SubscribeEvent
	public void registerTextures(TextureStitchEvent.Pre textureStitchEvent)
	{
		TextureMap map = textureStitchEvent.map;
		if (map.getTextureType() == 0)
		{
			for (TextureManager currentTexture : TextureManager.values())
			{
				currentTexture.registerTexture(map);
			}
		}
	}
}

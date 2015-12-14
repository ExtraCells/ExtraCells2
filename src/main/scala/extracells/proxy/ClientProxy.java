package extracells.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.render.TextureManager;
import extracells.render.block.RendererHardMEDrive;
import extracells.render.item.ItemRendererCertusTank;
import extracells.render.item.ItemRendererFluid;
import extracells.render.item.ItemRendererFluidPattern;
import extracells.render.item.ItemRendererWalrus;
import extracells.render.tileentity.TileEntityRendererWalrus;
import extracells.tileentity.TileEntityWalrus;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void registerRenderers() {
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.CERTUSTANK.getBlock()),
				new ItemRendererCertusTank());
		MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDPATTERN.getItem(),
				new ItemRendererFluidPattern());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.WALRUS.getBlock()),
				new ItemRendererWalrus());
		MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDITEM.getItem(), new ItemRendererFluid());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRendererWalrus());

		RendererHardMEDrive.registerRenderer();
	}

	@SubscribeEvent
	public void registerTextures(TextureStitchEvent.Pre textureStitchEvent) {
		TextureMap map = textureStitchEvent.map;
		for (TextureManager currentTexture : TextureManager.values()) {
			currentTexture.registerTexture(map);
		}
	}

	@Override
	public boolean isClient(){
		return true;
	}

	@Override
	public boolean isServer(){
		return false;
	}
}

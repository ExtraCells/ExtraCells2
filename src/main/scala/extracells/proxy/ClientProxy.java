package extracells.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import extracells.models.ModelManager;
import extracells.models.PartModels;
import extracells.render.block.RendererHardMEDrive;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void registerRenderers() {
		/*MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.CERTUSTANK.getBlock()),
				new ItemRendererCertusTank());
		MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDPATTERN.getItem(),
				new ItemRendererFluidPattern());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.WALRUS.getBlock()),
				new ItemRendererWalrus());
		MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDITEM.getItem(), new ItemRendererFluid());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRendererWalrus());*/
		ModelManager.registerItemAndBlockColors();
		RendererHardMEDrive.registerRenderer();
	}

	/*@SubscribeEvent
	public void registerTextures(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();
		for (TextureManager currentTexture : TextureManager.values()) {
			currentTexture.registerTexture(map);
		}
	}*/

	@SubscribeEvent
	public void onBakeModels(ModelBakeEvent event) {
	}

	@Override
	public void registerModels() {
		OBJLoader.INSTANCE.addDomain("extracells");
		PartModels.registerModels();
		ModelManager.registerModels();
	}

	@Override
	public void registerBlock(Block block) {
		super.registerBlock(block);
		ModelManager.registerBlockClient(block);
	}

	@Override
	public void registerItem(Item item) {
		super.registerItem(item);
		ModelManager.registerItemClient(item);
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

package extracells.proxy;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import extracells.Constants;
import extracells.models.ModelManager;
import extracells.models.PartModels;
import extracells.models.blocks.FluidItemModel;
import extracells.models.blocks.ModelCertusTank;
import extracells.models.blocks.ModelWalrus;
import extracells.network.PacketHandler;
import extracells.render.tileentity.TileEntityRendererWalrus;
import extracells.tileentity.TileEntityWalrus;

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
		MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDITEM.getItem(), new ItemRendererFluid());*/

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRendererWalrus());
		ModelManager.registerItemAndBlockColors();
		//RendererHardMEDrive.registerRenderer();
	}

	@SubscribeEvent
	public void registerTextures(TextureStitchEvent event) {
		TextureMap map = event.getMap();
		map.setTextureEntry(new TextureAtlasSprite(Constants.MOD_ID + ":blocks/walrus"){
			public void loadSprite(PngSizeInfo sizeInfo, boolean p_188538_2_) throws IOException {
				this.width = sizeInfo.pngWidth;
				this.height = sizeInfo.pngHeight;

				if (p_188538_2_) {
					this.height = this.width;
				}
			}
		});
		/*for (TextureManager currentTexture : TextureManager.values()) {
			currentTexture.registerTexture(map);
		}*/
	}

	@SubscribeEvent
	public void onBakeModels(ModelBakeEvent event) {
		ModelCertusTank.onBakeModels(event);
		ModelWalrus.onBakeModels(event);
		ModelManager.onBakeModels(event);
	}

	@Override
	public void registerModels() {
		OBJLoader.INSTANCE.addDomain("extracells");
		ModelLoaderRegistry.registerLoader(new FluidItemModel.ModelLoader());
		PartModels.registerModels();
		ModelManager.registerModels();
		/*ModelManager.registerCustomBlockModel(new BlockModelEntry(
				new ModelResourceLocation(Constants.MOD_ID + ":certustank", "empty=false"),
				new ModelResourceLocation(Constants.MOD_ID + ":certustk", "inventory"),
				new ModelCertusTank(),
				BlockEnum.CERTUSTANK.getBlock(),
				false)
		);*/
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

	@Override
	public void registerPackets() {
		super.registerPackets();
		PacketHandler.registerClientPackets();
	}
}

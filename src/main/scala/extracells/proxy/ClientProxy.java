package extracells.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import extracells.models.ModelManager;
import extracells.models.PartModels;
import extracells.models.blocks.ModelCertusTank;
import extracells.network.PacketHandler;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void registerRenderers() {
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRendererWalrus());
		ModelManager.registerItemAndBlockColors();
	}

	@SubscribeEvent
	public void onBakeModels(ModelBakeEvent event) {
		ModelCertusTank.onBakeModels(event);
		//ModelWalrus.onBakeModels(event);
		ModelManager.onBakeModels(event);
	}

	@Override
	public void registerModels() {
		ModelManager.init();
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
	public boolean isClient() {
		return true;
	}

	@Override
	public boolean isServer() {
		return false;
	}

	@Override
	public void registerPackets() {
		super.registerPackets();
		PacketHandler.registerClientPackets();
	}
}

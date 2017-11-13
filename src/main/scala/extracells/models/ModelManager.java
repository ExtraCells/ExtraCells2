package extracells.models;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import extracells.integration.Integration;
import extracells.models.blocks.GasItemModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.model.IModelState;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.block.IColoredBlock;
import extracells.item.IColoredItem;
import extracells.models.blocks.FluidItemModel;
import extracells.models.drive.HardDriveModel;
import extracells.models.drive.PartDriveModel;
import extracells.util.ModelUtil;

@SideOnly(Side.CLIENT)
public class ModelManager {

	private static final ModelManager INSTANCE = new ModelManager();

	/* CUSTOM MODELS*/
	private static final Map<String, IModel> customModels = new HashMap<>();
	/* ITEM AND BLOCK REGISTERS*/
	private static final List<IItemModelRegister> itemModelRegisters = new ArrayList<>();
	private static final List<IStateMapperRegister> stateMapperRegisters = new ArrayList<>();
	private static final List<IColoredBlock> blockColorList = new ArrayList<>();
	private static final List<IColoredItem> itemColorList = new ArrayList<>();
	/* DEFAULT ITEM AND BLOCK MODEL STATES*/
	private static IModelState defaultBlockState;
	private static IModelState defaultItemState;

	public static ModelManager getInstance() {
		return INSTANCE;
	}

	public void registerItemModel(Item item, int meta, String identifier) {
		ModelLoader.setCustomModelResourceLocation(item, meta, getModelLocation(identifier));
	}

	public void registerItemModel(Item item, int meta, String modID, String identifier) {
		ModelLoader.setCustomModelResourceLocation(item, meta, getModelLocation(modID, identifier));
	}

	public void registerItemModel(Item item) {
		registerItemModel(item, 0);
	}

	public void registerItemModel(Item item, int meta) {
		ModelLoader.setCustomModelResourceLocation(item, meta, getModelLocation(item));
	}

	public void registerItemModel(Item item, ItemMeshDefinition definition) {
		ModelLoader.setCustomMeshDefinition(item, definition);
	}

	public ModelResourceLocation getModelLocation(Item item) {
		ResourceLocation resourceLocation = item.getRegistryName();
		Preconditions.checkNotNull(resourceLocation);
		String itemName = resourceLocation.getResourcePath();
		return getModelLocation(itemName);
	}

	public ModelResourceLocation getModelLocation(String identifier) {
		return getModelLocation("extracells", identifier);
	}

	public ModelResourceLocation getModelLocation(String modID, String identifier) {
		return new ModelResourceLocation(modID + ":" + identifier, "inventory");
	}

	public static void registerBlockClient(Block block) {
		if (block instanceof IItemModelRegister) {
			itemModelRegisters.add((IItemModelRegister) block);
		}
		if (block instanceof IStateMapperRegister) {
			stateMapperRegisters.add((IStateMapperRegister) block);
		}
		if (block instanceof IColoredBlock) {
			blockColorList.add((IColoredBlock) block);
		}
	}

	public static void registerItemClient(Item item) {
		if (item instanceof IItemModelRegister) {
			itemModelRegisters.add((IItemModelRegister) item);
		}
		if (item instanceof IColoredItem) {
			itemColorList.add((IColoredItem) item);
		}
	}

	public static void registerModels() {
		for (IItemModelRegister itemModelRegister : itemModelRegisters) {
			Item item = null;
			if (itemModelRegister instanceof Block) {
				item = Item.getItemFromBlock((Block) itemModelRegister);
			} else if (itemModelRegister instanceof Item) {
				item = (Item) itemModelRegister;
			}

			if (item != null) {
				itemModelRegister.registerModel(item, INSTANCE);
			}
		}

		for (IStateMapperRegister stateMapperRegister : stateMapperRegisters) {
			stateMapperRegister.registerStateMapper();
		}
	}

	public static void registerItemAndBlockColors() {
		Minecraft minecraft = Minecraft.getMinecraft();

		BlockColors blockColors = minecraft.getBlockColors();
		for (IColoredBlock blockColor : blockColorList) {
			if (blockColor instanceof Block) {
				blockColors.registerBlockColorHandler(ColoredBlockBlockColor.INSTANCE, (Block) blockColor);
			}
		}

		ItemColors itemColors = minecraft.getItemColors();
		for (IColoredItem itemColor : itemColorList) {
			if (itemColor instanceof Item) {
				itemColors.registerItemColorHandler(ColoredItemItemColor.INSTANCE, (Item) itemColor);
			}
		}
	}

	public static IModelState getDefaultBlockState() {
		return defaultBlockState;
	}

	public static IModelState getDefaultItemState() {
		return defaultItemState;
	}

	public static void addModel(String path, IModel model) {
		customModels.put(path, model);
	}

	public static void init() {
		addModel("models/block/builtin/hard_drive", new HardDriveModel());
		addModel("models/part/drive", new PartDriveModel());
		OBJLoader.INSTANCE.addDomain("extracells");
		ModelLoaderRegistry.registerLoader(new FluidItemModel.ModelLoader());
		ModelLoaderRegistry.registerLoader(new ECModelLoader(customModels));
		if(Integration.Mods.MEKANISMGAS.isEnabled())
			ModelLoaderRegistry.registerLoader(new GasItemModel.ModelLoader());
	}

	public static void onBakeModels(ModelBakeEvent event) {
		//load default item and block model states
		defaultItemState = ModelUtil.loadModelState(new ResourceLocation("minecraft:models/item/generated"));
		defaultBlockState = ModelUtil.loadModelState(new ResourceLocation("minecraft:models/block/block"));
	}

	private static class ColoredItemItemColor implements IItemColor {
		public static final ColoredItemItemColor INSTANCE = new ColoredItemItemColor();

		private ColoredItemItemColor() {

		}

		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			Item item = stack.getItem();
			if (item instanceof IColoredItem) {
				return ((IColoredItem) item).getColorFromItemstack(stack, tintIndex);
			}
			return 0xffffff;
		}
	}

	private static class ColoredBlockBlockColor implements IBlockColor {
		public static final ColoredBlockBlockColor INSTANCE = new ColoredBlockBlockColor();

		private ColoredBlockBlockColor() {

		}

		@Override
		public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
			Block block = state.getBlock();
			if (block instanceof IColoredBlock && worldIn != null && pos != null) {
				return ((IColoredBlock) block).colorMultiplier(state, worldIn, pos, tintIndex);
			}
			return 0xffffff;
		}
	}

	private static class BlockModeStateMapper extends StateMapperBase {
		private final BlockModelEntry index;

		public BlockModeStateMapper(BlockModelEntry index) {
			this.index = index;
		}

		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
			return index.blockModelLocation;
		}
	}
}

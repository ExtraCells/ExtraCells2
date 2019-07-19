package extracells.definitions;

import appeng.api.definitions.ITileDefinition;
import extracells.api.definitions.IBlockDefinition;
import extracells.registries.BlockEnum;
import extracells.tileentity.*;

public class BlockDefinition implements IBlockDefinition {

	public static final BlockDefinition instance = new BlockDefinition();

	@Override
	public ITileDefinition blockInterface() {
		return new BlockItemDefinitions(BlockEnum.ECBASEBLOCK.getBlock(),
				TileEntityFluidInterface.class);
	}

	@Override
	public ITileDefinition certusTank() {
		return new BlockItemDefinitions(BlockEnum.CERTUSTANK.getBlock(),
				TileEntityCertusTank.class);
	}

	@Override
	public ITileDefinition fluidCrafter() {
		return new BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.getBlock(),
				TileEntityFluidCrafter.class);
	}

	@Override
	public ITileDefinition fluidFiller() {
		return new BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.getBlock(), 1,
				TileEntityFluidFiller.class);
	}

	@Override
	public ITileDefinition walrus() {
		return new BlockItemDefinitions(BlockEnum.WALRUS.getBlock(),
				TileEntityWalrus.class);
	}

    @Override
    public ITileDefinition craftingStorage256k() {
        return new BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.getBlock(), 0,
                TileEntityCraftingStorage.class);
    }

    @Override
    public ITileDefinition craftingStorage1024k() {
        return new BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.getBlock(), 1,
                TileEntityCraftingStorage.class);
    }

    @Override
    public ITileDefinition craftingStorage4096k() {
        return new BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.getBlock(), 2,
                TileEntityCraftingStorage.class);
    }

    @Override
    public ITileDefinition craftingStorage16384k() {
        return new BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.getBlock(), 3,
                TileEntityCraftingStorage.class);
    }
}

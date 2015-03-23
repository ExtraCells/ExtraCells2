package extracells.definitions;

import appeng.api.util.AEItemDefinition;
import extracells.api.definitions.IBlockDefinition;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityCertusTank;
import extracells.tileentity.TileEntityFluidCrafter;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.tileentity.TileEntityWalrus;

public class BlockDefinition implements IBlockDefinition {

	public static final BlockDefinition instance = new BlockDefinition();

	@Override
	public AEItemDefinition blockInterface() {
		return new BlockItemDefinitions(BlockEnum.ECBASEBLOCK.getBlock(),
				TileEntityFluidInterface.class);
	}

	@Override
	public AEItemDefinition certusTank() {
		return new BlockItemDefinitions(BlockEnum.CERTUSTANK.getBlock(),
				TileEntityCertusTank.class);
	}

	@Override
	public AEItemDefinition fluidCrafter() {
		return new BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.getBlock(),
				TileEntityFluidCrafter.class);
	}

	@Override
	public AEItemDefinition fluidFiller() {
		return new BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.getBlock(), 1,
				TileEntityFluidFiller.class);
	}

	@Override
	public AEItemDefinition walrus() {
		return new BlockItemDefinitions(BlockEnum.WALRUS.getBlock(),
				TileEntityWalrus.class);
	}

}

package extracells.api.definitions;

import appeng.api.definitions.ITileDefinition;

public interface IBlockDefinition {

	ITileDefinition blockInterface();

	ITileDefinition certusTank();

	ITileDefinition fluidCrafter();

	ITileDefinition fluidFiller();

	ITileDefinition walrus();

	ITileDefinition craftingStorage256k();

    ITileDefinition craftingStorage1024k();

    ITileDefinition craftingStorage4096k();

    ITileDefinition craftingStorage16384k();
}

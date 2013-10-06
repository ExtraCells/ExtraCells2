package extracells;

import net.minecraft.util.StatCollector;

public enum BlockNames
{
	CERTUSTANK("tile.block.certustank.name"), 
	FLUIDIMPORT("tile.block.fluid.bus.import.name"), 
	FLUIDEXPORT("tile.block.fluid.bus.export.name"), 
	FLUIDSTORAGE("tile.block.fluid.bus.storage.name"), 
	FLUIDTERMINAL("tile.block.fluid.terminal.name"),
	FLUIDTRANSITION("tile.block.fluid.transitionplane.name"),
	BRMEDRIVE("tile.block.hardmedrive.name"),
	MEDROPPER("tile.block.medropper.name"),
	MEBATTERY("tile.block.mebattery.name"),
	SOLDERINGSTATION("tile.block.solderingstation.name");
	
	
	private final String internalName;
	
	BlockNames(String internalName){
		this.internalName = internalName;
	}
	
	public String getLocalizedName()
	{
		return StatCollector.translateToLocal(internalName);
	}
}

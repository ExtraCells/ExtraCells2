package extracells.util;

import appeng.api.recipes.ISubItemResolver;
import appeng.api.recipes.ResolverResult;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;

public class NameHandler implements ISubItemResolver {

    @Override
    public Object resolveItemByName(String namespace, String fullName) {
        if (!namespace.equals("extracells"))
            return null;

        // Fluid Cells
        if (fullName.equals("fluidCell1k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 0);
        if (fullName.equals("fluidCell4k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 1);
        if (fullName.equals("fluidCell16k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 2);
        if (fullName.equals("fluidCell64k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 3);
        if (fullName.equals("fluidCell256k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 4);
        if (fullName.equals("fluidCell1024k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 5);
        if (fullName.equals("fluidCell4096k"))
            return new ResolverResult(ItemEnum.FLUIDSTORAGE.getInternalName(), 6);

        // Physical Cells
        if (fullName.equals("physCell256k"))
            return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 0);
        if (fullName.equals("physCell1024k"))
            return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 1);
        if (fullName.equals("physCell4096k"))
            return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 2);
        if (fullName.equals("physCell16384k"))
            return new ResolverResult(ItemEnum.PHYSICALSTORAGE.getInternalName(), 3);

        //Fluid Storage Components
        if (fullName.equals("cell1kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 4);
        if (fullName.equals("cell4kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 5);
        if (fullName.equals("cell16kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 6);
        if (fullName.equals("cell64kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 7);
        if (fullName.equals("cell256kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 8);
        if (fullName.equals("cell1024kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 9);
        if (fullName.equals("cell4096kPartFluid"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 10);

        //Physical Storage Components
        if (fullName.equals("cell256kPart"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 0);
        if (fullName.equals("cell1024kPart"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 1);
        if (fullName.equals("cell4096kPart"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 2);
        if (fullName.equals("cell16384kPart"))
            return new ResolverResult(ItemEnum.STORAGECOMPONET.getInternalName(), 3);

        //Physical Storage Casing
        if (fullName.equals("physCasing"))
            return new ResolverResult(ItemEnum.STORAGECASING.getInternalName(), 0);

        //Fluid Storage Casing
        if (fullName.equals("fluidCasing"))
            return new ResolverResult(ItemEnum.STORAGECASING.getInternalName(), 1);

        //Parts
        if (fullName.equals("partFluidImportBus"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDIMPORT.ordinal());
        if (fullName.equals("partFluidExportBus"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDEXPORT.ordinal());
        if (fullName.equals("partFluidStorageBus"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDSTORAGE.ordinal());
        if (fullName.equals("partFluidTerminal"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDTERMINAL.ordinal());
        if (fullName.equals("partFluidLevelEmitter"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDLEVELEMITTER.ordinal());
        if (fullName.equals("partFluidAnnihilationPlane"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDPANEANNIHILATION.ordinal());
        if (fullName.equals("partFluidFormationPlane"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.FLUIDPANEFORMATION.ordinal());
        if (fullName.equals("partBattery"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.BATTERY.ordinal());
        if (fullName.equals("partDrive"))
            return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.DRIVE.ordinal());

        //MISC
        if (fullName.equals("certusTank"))
            return new ResolverResult(BlockEnum.CERTUSTANK.getInternalName(), 0);
        if (fullName.equals("fluidPattern"))
            return new ResolverResult(ItemEnum.FLUIDPATTERN.getInternalName(), 0);
        if (fullName.equals("fluidCrafter"))
        	return new ResolverResult(BlockEnum.CERTUSTANK.getInternalName(), 0);
        if (fullName.equals("wirelessFluidTerminal"))
        	return new ResolverResult(ItemEnum.FLUIDWIRELESSTERMINAL.getInternalName(), 0);
		if (fullName.equals("walrus"))
        	return new ResolverResult(BlockEnum.WALRUS.getInternalName(), 0);

        return null;
    }
}
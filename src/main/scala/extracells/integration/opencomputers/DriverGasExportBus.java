package extracells.integration.opencomputers;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import extracells.part.PartGasExport;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.util.GasUtil;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;
import li.cil.oc.server.network.Component;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class DriverGasExportBus implements SidedBlock{

    @Override
    public boolean worksWith(World world, int x, int y, int z, ForgeDirection side) {
        return getExportBus(world, x, y, z, side) != null;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null || (!(tile instanceof IPartHost)))
            return null;
        return new Environment((IPartHost) tile);
    }

    private static PartGasExport getExportBus(World world, int x, int y, int z, ForgeDirection dir){
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null || (!(tile instanceof IPartHost)))
            return null;
        IPartHost host = (IPartHost) tile;
        if(dir == null || dir == ForgeDirection.UNKNOWN){
            for (ForgeDirection side: ForgeDirection.VALID_DIRECTIONS){
                IPart part = host.getPart(side);
                if (part != null && part instanceof PartGasExport)
                    return (PartGasExport) part;
            }
            return null;
        }else{
            IPart part = host.getPart(dir);
            return part == null ? null : part instanceof PartGasExport ? (PartGasExport) part : null;
        }
    }

    public class Environment extends ManagedEnvironment implements NamedBlock{

        protected final TileEntity tile;
        protected final IPartHost host;

        Environment(IPartHost host){
            tile = (TileEntity) host;
            this.host = host;
            setNode(Network.newNode(this, Visibility.Network).
                    withComponent("me_exportbus").
                    create());
        }

        @Callback(doc = "function(side:number, [ slot:number]):table -- Get the configuration of the gas export bus pointing in the specified direction.")
        public Object[] getGasExportConfiguration(Context context, Arguments args){
            ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
            if (dir == null || dir == ForgeDirection.UNKNOWN)
                return new Object[]{null, "unknown side"};
            PartGasExport part = getExportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
            if (part == null)
                return new Object[]{null, "no export bus"};
            int slot = args.optInteger(1, 4);
            try{
                Fluid fluid = part.filterFluids[slot];
                if (fluid == null)
                    return new Object[]{null};
                return new Object[]{GasUtil.getGasStack(new FluidStack(fluid, 1000))};
            }catch(Throwable e){
                return new Object[]{null, "Invalid slot"};
            }

        }

        @Callback(doc = "function(side:number[, slot:number][, database:address, entry:number]):boolean -- Configure the gas export bus pointing in the specified direction to export gas stacks matching the specified descriptor.")
        public Object[] setGasExportConfiguration(Context context, Arguments args){
            ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
            if (dir == null || dir == ForgeDirection.UNKNOWN)
                return new Object[]{null, "unknown side"};
            PartGasExport part = getExportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
            if (part == null)
                return new Object[]{null, "no export bus"};
            int slot;
            String address;
            int entry;
            if (args.count() == 3){
                address = args.checkString(1);
                entry = args.checkInteger(2);
                slot = 4;
            }else if (args.count() < 3){
                slot = args.optInteger(1, 4);
                try{
                    part.filterFluids[slot] = null;
                    part.onInventoryChanged();
                    context.pause(0.5);
                    return new Object[]{true};
                }catch(Throwable e){
                    return new Object[]{false, "invalid slot"};
                }
            }else{
                slot = args.optInteger(1, 4);
                address = args.checkString(2);
                entry = args.checkInteger(3);
            }
            Node node = node().network().node(address);
            if (node == null)
                throw new IllegalArgumentException("no such component");
            if (!(node instanceof Component))
                throw new IllegalArgumentException("no such component");
            li.cil.oc.api.network.Environment env = node.host();
            if (!(env instanceof Database))
                throw new IllegalArgumentException("not a database");
            Database database = (Database) env;
            try{
                ItemStack data = database.getStackInSlot(entry - 1);
                if (data == null)
                    part.filterFluids[slot] = null;
                else{
                    GasStack fluid = GasUtil.getGasFromContainer(data);
                    if(fluid == null || fluid.getGas() == null)
                        return new Object[]{false, "not a fluid container"};
                    part.filterFluids[slot] = GasUtil.getFluidStack(fluid).getFluid();
                }
                part.onInventoryChanged();
                context.pause(0.5);
                return new Object[]{true};
            }catch(Throwable e){
                return new Object[]{false, "invalid slot"};
            }
        }

        @Callback(doc = "function(side:number, amount:number):boolean -- Make the gas export bus facing the specified direction perform a single export operation.")
        public Object[] exportGas(Context context, Arguments args){
            ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
            if (dir == null || dir == ForgeDirection.UNKNOWN)
                return new Object[]{false, "unknown side"};
            PartGasExport part = getExportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
            if (part == null)
                return new Object[]{false, "no export bus"};
            if (part.getFacingGasTank() == null)
                return new Object[]{false, "no tank"};
            int amount = Math.min(args.optInteger(1, 625), 125 + part.getSpeedState() * 125);
            boolean didSomething = part.doWork(amount, 1);
            if (didSomething)
                context.pause(0.25);
            return new Object[]{didSomething};
        }

        @Override
        public String preferredName() {
            return "me_exportbus";
        }

        @Override
        public int priority() {
            return 2;
        }

    }
    static class Provider implements EnvironmentProvider {
        @Override
        public Class<? extends li.cil.oc.api.network.Environment> getEnvironment(ItemStack stack) {
            if (stack == null)
                return null;
            if (stack.getItem() == ItemEnum.PARTITEM.getItem() && stack.getItemDamage() == PartEnum.FLUIDEXPORT.ordinal())
                return Environment.class;
            return null;
        }
    }

}

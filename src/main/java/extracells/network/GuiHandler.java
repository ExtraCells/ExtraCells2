package extracells.network;

import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.network.IGuiHandler;
import extracells.Extracells;
import extracells.api.IWirelessFluidTermHandler;
import extracells.container.ContainerFluidCrafter;
import extracells.container.ContainerFluidStorage;
import extracells.gui.GuiFluidCrafter;
import extracells.gui.GuiFluidStorage;
import extracells.part.PartECBase;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidCrafter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiHandler implements IGuiHandler {

    private static Object[] temp;

    private static Object getPartContainer(ForgeDirection side, EntityPlayer player, World world, int x, int y, int z) {
        PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
        return part.getServerGuiElement(player);
    }

    public static Object getPartGui(ForgeDirection side, EntityPlayer player, World world, int x, int y, int z) {
        IPartHost tileEntity = (IPartHost) world.getTileEntity(x, y, z);
        PartECBase part = (PartECBase) tileEntity.getPart(side);
        return part.getClientGuiElement(player);
    }

    @SuppressWarnings("unchecked")
    private static Object getContainer(int ID, EntityPlayer player, Object[] args) {
        switch (ID) {
            case 0:
            	IMEMonitor<IAEFluidStack> fluidInventory = (IMEMonitor<IAEFluidStack>) args[0];
            	return new ContainerFluidStorage(fluidInventory, player);
            case 1:
                IMEMonitor<IAEFluidStack> fluidInventory2 = (IMEMonitor<IAEFluidStack>) args[0];
                IWirelessFluidTermHandler handler = (IWirelessFluidTermHandler) args[1];
                return new ContainerFluidStorage(fluidInventory2, player, handler);
            default:
                return null;
        }
    }

    public static Object getGui(int ID, EntityPlayer player) {
        switch (ID) {
            case 0:
                return new GuiFluidStorage(player);
            case 1:
                return new GuiFluidStorage(player);
            default:
                return null;
        }
    }

    public static int getGuiId(PartECBase part) {
        return part.getSide().ordinal();
    }

    public static int getGuiId(int guiId) {
        return guiId + 6;
    }

    public static void launchGui(int ID, EntityPlayer player, Object... args) {
        temp = args;
        player.openGui(Extracells.instance, ID, null, 0, 0, 0);
    }

    public static void launchGui(int ID, EntityPlayer player, World world, int x, int y, int z) {
        player.openGui(Extracells.instance, ID, world, x, y, z);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        ForgeDirection side = ForgeDirection.getOrientation(ID);
        if (world != null && world.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.getBlock()){
        	TileEntity tileEntity = world.getTileEntity(x, y, z);
        	if(tileEntity == null || !(tileEntity instanceof TileEntityFluidCrafter))
        		return null;
        	return new ContainerFluidCrafter(player.inventory, ((TileEntityFluidCrafter) tileEntity).getInventory());
        }
        if (world != null && side != ForgeDirection.UNKNOWN)
            return getPartContainer(side, player, world, x, y, z);
        return getContainer(ID - 6, player, temp);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	ForgeDirection side = ForgeDirection.getOrientation(ID);
        if (world.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.getBlock()){
        	TileEntity tileEntity = world.getTileEntity(x, y, z);
        	if(tileEntity == null || !(tileEntity instanceof TileEntityFluidCrafter))
        		return null;
        	return new GuiFluidCrafter(player.inventory, ((TileEntityFluidCrafter) tileEntity).getInventory());
        }
        if (world != null && side != ForgeDirection.UNKNOWN)
            return getPartGui(side, player, world, x, y, z);
        return getGui(ID - 6, player);
    }
}

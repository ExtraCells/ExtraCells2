package extracells.network;

import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.network.IGuiHandler;
import extracells.Extracells;
import extracells.container.ContainerFluidStorage;
import extracells.gui.GuiFluidStorage;
import extracells.part.PartECBase;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiHandler implements IGuiHandler
{
	private static Object[] temp;

	private static Container getPartContainer(ForgeDirection side, EntityPlayer player, World world, int x, int y, int z)
	{
		PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
		return part.getServerGuiElement(player);
	}

	public static Gui getPartGui(ForgeDirection side, EntityPlayer player, World world, int x, int y, int z)
	{
		IPartHost tileEntity = (IPartHost) world.getTileEntity(x, y, z);
		PartECBase part = (PartECBase) tileEntity.getPart(side);
		return part.getClientGuiElement(player);
	}

	@SuppressWarnings("unchecked")
	private static Container getContainer(int ID, EntityPlayer player, Object[] args)
	{
		switch (ID)
		{
		case 0: // Fallthrough intentional.
		case 1:
			IMEMonitor<IAEFluidStack> fluidInventory = (IMEMonitor<IAEFluidStack>) args[0];
			return new ContainerFluidStorage(fluidInventory, player);
		default:
			return null;
		}
	}

	public static Gui getGui(int ID, EntityPlayer player)
	{
		switch (ID)
		{
		case 0:
			return new GuiFluidStorage(player);
		case 1:
			return new GuiFluidStorage(player);
		default:
			return null;
		}
	}

	public static int getGuiId(PartECBase part)
	{
		return part.getSide().ordinal();
	}

	public static int getGuiId(int guiId)
	{
		return guiId + 6;
	}

	public static void launchGui(int ID, EntityPlayer player, Object... args)
	{
		temp = args;
		player.openGui(Extracells.instance, ID, null, 0, 0, 0);
	}

	public static void launchGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		player.openGui(Extracells.instance, ID, world, x, y, z);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation(ID);
		if (world != null && side != ForgeDirection.UNKNOWN)
			return getPartContainer(side, player, world, x, y, z);
		return getContainer(ID - 6, player, temp);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation(ID);
		if (world != null && side != ForgeDirection.UNKNOWN)
			return getPartGui(side, player, world, x, y, z);
		return getGui(ID - 6, player);
	}
}

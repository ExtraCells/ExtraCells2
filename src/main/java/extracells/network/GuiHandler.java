package extracells.network;

import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import extracells.container.ContainerFluidStorage;
import extracells.gui.GuiFluidStorage;
import extracells.network.packet.other.PacketGui;
import extracells.part.PartECBase;
import extracells.registries.PartEnum;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiHandler
{

	private static Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation(ID & 0x7F);
		if (side != ForgeDirection.UNKNOWN)
		{
			PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
			return part.getServerGuiElement(player);
		}
		return null;
	}

	public static Gui getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation(ID & 0x7F);
		if (side != ForgeDirection.UNKNOWN)
		{
			IPartHost tileEntity = (IPartHost) world.getTileEntity(x, y, z);
			PartECBase part = (PartECBase) tileEntity.getPart(side);
			return part.getClientGuiElement(player);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Container getServerGuiElement(int ID, EntityPlayer player, Object[] args)
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

	public static Gui getClientGuiElement(int ID, EntityPlayer player)
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
		return PartEnum.getPartID(part) << 7 | part.getSide().ordinal();
	}

	public static void launchGui(int ID, EntityPlayer player, Object... args)
	{
		new PacketGui(ID, player).sendPacketToPlayer(player);
		openContainer(getServerGuiElement(ID, player, args), player);
	}

	public static void launchGui(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		new PacketGui(ID, player, world, x, y, z).sendPacketToPlayer(player);
		openContainer(getServerGuiElement(ID, player, world, x, y, z), player);
	}

	private static void openContainer(Container container, EntityPlayer player)
	{
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		entityPlayerMP.getNextWindowId();
		entityPlayerMP.closeContainer();
		int windowId = entityPlayerMP.currentWindowId;
		entityPlayerMP.openContainer = container;
		entityPlayerMP.openContainer.windowId = windowId;
		entityPlayerMP.openContainer.addCraftingToCrafters(entityPlayerMP);
	}
}

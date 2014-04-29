package extracells.item;

import appeng.api.AEApi;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.WorldCoord;
import extracells.network.GuiHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemWirelessTerminalFluid extends Item implements INetworkEncodable
{
	IIcon icon;

	public ItemWirelessTerminalFluid()
	{
		setMaxStackSize(1);
	}

	@SuppressWarnings("unchecked")
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		if (world.isRemote)
			return itemStack;
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		Long key;
		try
		{
			key = Long.parseLong(itemStack.getTagCompound().getString("key"));
		} catch (Throwable ignored)
		{
			return itemStack;
		}
		int x = (int) entityPlayer.posX;
		int y = (int) entityPlayer.posY;
		int z = (int) entityPlayer.posZ;
		IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locateable().findLocateableBySerial(key);
		if (securityTerminal == null)
			return itemStack;
		IGridNode gridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
		if (gridNode == null)
			return itemStack;
		IGrid grid = gridNode.getGrid();
		if (grid == null)
			return itemStack;
		for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) AEApi.instance().blocks().blockWireless.entity()))
		{
			IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
			WorldCoord distance = accessPoint.getLocation().subtract(x, y, z);
			int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
			if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange())
			{
				IStorageGrid gridCache = grid.getCache(IStorageGrid.class);
				if (gridCache != null)
				{
					IMEMonitor<IAEFluidStack> fluidInventory = gridCache.getFluidInventory();
					if (fluidInventory != null)
					{
						GuiHandler.launchGui(GuiHandler.getGuiId(1), entityPlayer, fluidInventory);
					}
				}
			}
		}
		return itemStack;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item");
	}

	public IIcon getIconFromDamage(int dmg)
	{
		return icon;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless");
	}

	@Override
	public String getEncryptionKey(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());
		return itemStack.getTagCompound().getString("key");
	}

	@Override
	public void setEncryptionKey(ItemStack itemStack, String encKey, String name)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		tagCompound.setString("key", encKey);
	}
}

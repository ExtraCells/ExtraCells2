package extracells.part;

import appeng.api.config.Upgrades;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import extracells.TextureManager;
import extracells.inventoryHandler.HandlerPartStorageFluid;
import io.netty.buffer.ByteBuf;
import javafx.util.Pair;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartFluidStorage extends PartECBase implements ICellContainer
{
	int priority = 0;
	HandlerPartStorageFluid handler = new HandlerPartStorageFluid(this);

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.STORAGE_FRONT.getTexture(), side, side);
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
		rh.renderInventoryBox(renderer);
		rh.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
		rh.renderInventoryBox(renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.STORAGE_FRONT.getTexture(), side, side);
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
		rh.renderBlock(x, y, z, renderer);
		rh.setBounds(4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F);
		rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		data.setInteger("priority", priority);
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		priority = data.getInteger("priority");
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{

	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		return false;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 2;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
		if (channel == StorageChannel.FLUIDS)
		{
			list.add(handler);
		}
		return list;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void blinkCell(int slot)
	{
	}

	@Override
	public void onNeighborChanged()
	{
		handler.onNeighborChange();
		if (node != null && node.getGrid() != null && gridBlock != null)
			node.getGrid().postEvent(new MENetworkStorageEvent(gridBlock.getFluidMonitor(), StorageChannel.FLUIDS));
	}

	public TileEntity getHostTile()
	{
		return hostTile;
	}

	public static List<Pair<Upgrades, Integer>> getPossibleUpgrades()
	{
		List<Pair<Upgrades, Integer>> pairList = new ArrayList<Pair<Upgrades, Integer>>();
		pairList.add(new Pair<Upgrades, Integer>(Upgrades.INVERTER, 1));
		return pairList;
	}
}

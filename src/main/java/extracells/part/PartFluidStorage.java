package extracells.part;

import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import extracells.inventoryHandler.HandlerPartStorageFluid;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

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
		rh.setBounds(1, 1, 15, 15, 15, 16);
		rh.renderInventoryBox(renderer);
		rh.setBounds(4, 4, 14, 12, 12, 15);
		rh.renderInventoryBox(renderer);

		rh.setBounds(6, 6, 13, 10, 10, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.STORAGE_FRONT.getTexture(), side, side);
		rh.setBounds(1, 1, 15, 15, 15, 16);
		rh.renderBlock(x, y, z, renderer);
		rh.setBounds(4, 4, 14, 12, 12, 15);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(6, 6, 13, 10, 10, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT(data);
		data.setInteger("priority", priority);
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT(data);
		priority = data.getInteger("priority");
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(1, 1, 15, 15, 15, 16);
		bch.addBox(4, 4, 14, 12, 12, 15);
		bch.addBox(6, 6, 13, 10, 10, 14);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
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

}

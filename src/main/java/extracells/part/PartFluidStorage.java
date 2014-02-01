package extracells.part;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import extracells.inventoryHandler.StorageBusHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartFluidStorage extends ECBasePart implements ICellContainer
{
	int priority = 0;
	IFluidHandler facingTank;

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
		rh.renderInventoryBox(renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
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
	public void writeToStream(DataOutputStream data) throws IOException
	{

	}

	@Override
	public boolean readFromStream(DataInputStream data) throws IOException
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
		return 1;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
		if (channel == StorageChannel.FLUIDS)
		{
			list.add(new StorageBusHandler(this, facingTank, side));
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
	public IGridNode getActionableNode()
	{
		return null;
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		onNeighborChanged();
	}

	@Override
	public void onNeighborChanged()
	{
		TileEntity tileEntity = hostTile.worldObj.getBlockTileEntity(hostTile.xCoord, hostTile.yCoord, hostTile.zCoord);
		facingTank = null;
		if (tileEntity instanceof IFluidHandler)
			facingTank = (IFluidHandler) tileEntity;
//		node.getGrid().postEvent(new MENetworkStorageEvent(gridBlock.getMonitor(), StorageChannel.FLUIDS));
	}
}

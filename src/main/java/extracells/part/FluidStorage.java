package extracells.part;

import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.util.DimensionalCoord;
import extracells.inventoryHandler.StorageBusHandler;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FluidStorage extends ECBasePart implements ICellContainer
{
	int priority = 0;
	IFluidHandler facingTank;

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{

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
	public void removeFromWorld()
	{

	}

	@Override
	public void addToWorld()
	{

	}

	@Override
	public IGridBlock createGridBlock()
	{
		return null;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{

	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 0;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
		if (channel == StorageChannel.FLUIDS)
		{
			list.add(new StorageBusHandler(externalNode, facingTank));
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
	public void onNeighborChanged()
	{
		DimensionalCoord coord = gridBlock.getLocation();
		TileEntity tileEntity = coord.getWorld().getBlockTileEntity(coord.x, coord.y, coord.z);
		facingTank = null;
		if (tileEntity instanceof IFluidHandler)
			facingTank = (IFluidHandler) tileEntity;
	}
}

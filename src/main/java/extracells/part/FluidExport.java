package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FluidExport extends ECBasePart implements IGridTickable, IActionHost
{
	IFluidHandler facingTank;

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));

		rh.setBounds(4F, 4F, 12F, 12, 12, 14);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5F, 5F, 14F, 11, 11, 15);
		rh.renderInventoryBox(renderer);

		rh.setBounds(6F, 6F, 15F, 10, 10, 16);
		rh.renderInventoryBox(renderer);

		/*
		 * / rh.setBounds(3F, 3F, 14F, 13, 13, 16); rh.renderInventoryBox(renderer); rh.setBounds(4F, 4F, 12F, 12, 12, 14); rh.renderInventoryBox(renderer); rh.setBounds(5F, 5F, 10F, 11, 11, 12); rh.renderInventoryBox(renderer); rh.setBounds(4F, 4F, 8F, 10, 10, 10); rh.renderInventoryBox(renderer); //
		 */
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.useSimpliedRendering(x, y, z, this);

		rh.setBounds(4F, 4F, 12F, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(5F, 5F, 14F, 11, 11, 15);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(6F, 6F, 15F, 10, 10, 16);
		rh.renderBlock(x, y, z, renderer);

		/*
		 * / rh.setBounds(3F, 3F, 14F, 13, 13, 16); rh.renderBlock(x, y, z, renderer); rh.setBounds(4F, 4F, 12F, 12, 12, 14); rh.renderBlock(x, y, z, renderer); rh.setBounds(5F, 5F, 10F, 11, 11, 12); rh.renderBlock(x, y, z, renderer); rh.setBounds(6F, 6F, 8F, 10, 10, 10); rh.renderBlock(x, y, z, renderer); //
		 */
	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{

	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{

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
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(4F, 4F, 12F, 12, 12, 14);
		bch.addBox(5F, 5F, 14F, 11, 11, 15);
		bch.addBox(6F, 6F, 15F, 10, 10, 16);
		/*
		 * / bch.addBox(3F, 3F, 14F, 13, 13, 16); bch.addBox(4F, 4F, 12F, 12, 12, 14); bch.addBox(5F, 5F, 10F, 11, 11, 12); bch.addBox(6F, 6F, 8F, 10, 10, 10);//
		 */
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		System.out.println("YA");
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		System.out.println("sdasd");
		return doWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	public boolean doWork()
	{
		if (node == null)
			return false;
		IGrid grid = node.getGrid();
		if (grid == null)
			return false;
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		if (storageGrid == null)
			return false;
		IMEMonitor<IAEFluidStack> monitor = storageGrid.getFluidInventory();
		if (monitor == null)
			return false;
		IAEFluidStack stack = monitor.extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.WATER, 1000)), Actionable.MODULATE, new MachineSource(this));
		if (stack != null)
			return true;
		return false;
	}

	@Override
	public void onNeighborChanged()
	{
		TileEntity tileEntity = hostTile.worldObj.getBlockTileEntity(hostTile.xCoord, hostTile.yCoord, hostTile.zCoord);
		facingTank = null;
		if (tileEntity instanceof IFluidHandler)
			facingTank = (IFluidHandler) tileEntity;
	}

	@Override
	public IGridNode getActionableNode()
	{
		return node;
	}
}

package extracells.part;

import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import extracells.gridblock.GBFluidExport;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FluidExport extends ECBasePart implements IGridTickable
{
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));

		rh.setBounds(4F, 4F, 11F, 12, 12, 12);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5F, 5F, 12F, 11, 11, 14);
		rh.renderInventoryBox(renderer);

		rh.setBounds(6F, 6F, 14F, 10, 10, 16);
		rh.renderInventoryBox(renderer);

		/*/
		rh.setBounds(3F, 3F, 14F, 13, 13, 16);
		rh.renderInventoryBox(renderer);

		rh.setBounds(4F, 4F, 12F, 12, 12, 14);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5F, 5F, 10F, 11, 11, 12);
		rh.renderInventoryBox(renderer);

		rh.setBounds(4F, 4F, 8F, 10, 10, 10);
		rh.renderInventoryBox(renderer);
		//*/
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.useSimpliedRendering(x, y, z, this);

		rh.setBounds(4F, 4F, 11F, 12, 12, 12);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(5F, 5F, 12F, 11, 11, 14);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(6F, 6F, 14F, 10, 10, 16);
		rh.renderBlock(x, y, z, renderer);

		/*/
		rh.setBounds(3F, 3F, 14F, 13, 13, 16);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(4F, 4F, 12F, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(5F, 5F, 10F, 11, 11, 12);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(6F, 6F, 8F, 10, 10, 10);
		rh.renderBlock(x, y, z, renderer);
		 //*/
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
	public IGridBlock createGridBlock()
	{
		return new GBFluidExport(this);
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(4F, 4F, 11F, 12, 12, 12);
		bch.addBox(5F, 5F, 12F, 11, 11, 14);
		bch.addBox(6F, 6F, 14F, 10, 10, 16);
/*/
		bch.addBox(3F, 3F, 14F, 13, 13, 16);
		bch.addBox(4F, 4F, 12F, 12, 12, 14);
		bch.addBox(5F, 5F, 10F, 11, 11, 12);
		bch.addBox(6F, 6F, 8F, 10, 10, 10);//*/
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
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return TickRateModulation.SAME;
	}
}

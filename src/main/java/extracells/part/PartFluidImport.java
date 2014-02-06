package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEFluidStack;
import extracells.TextureManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PartFluidImport extends PartECBase implements IGridTickable, IFluidHandler
{

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));

		rh.setTexture(TextureManager.IMPORT_FRONT.getTexture());
		rh.setBounds(4F, 4F, 15F, 12, 12, 16);
		rh.renderInventoryBox(renderer);

		rh.setTexture(TextureManager.BUS_SIDE.getTexture());
		rh.setBounds(5F, 5F, 14F, 11, 11, 15);
		rh.renderInventoryBox(renderer);

		rh.setBounds(6F, 6F, 12F, 10, 10, 14);
		rh.renderInventoryBox(renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.useSimpliedRendering(x, y, z, this);

		rh.setTexture(TextureManager.IMPORT_FRONT.getTexture());
		rh.setBounds(4F, 4F, 15F, 12, 12, 16);
		rh.renderBlock(x, y, z, renderer);

		rh.setTexture(TextureManager.BUS_SIDE.getTexture());
		rh.setBounds(5F, 5F, 14F, 11, 11, 15);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(6F, 6F, 12F, 10, 10, 14);
		rh.renderBlock(x, y, z, renderer);
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
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(4F, 4F, 15F, 12, 12, 16);
		bch.addBox(5F, 5F, 14F, 11, 11, 15);
		bch.addBox(6F, 6F, 12F, 10, 10, 14);
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
		return doWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	public boolean doWork()
	{
		if (facingTank == null)
			return false;

		FluidStack drained = facingTank.drain(ForgeDirection.DOWN, 250, false);

		if (drained == null || drained.amount <= 0 || drained.fluidID <= 0)
			return false;

		IAEFluidStack toFill = AEApi.instance().storage().createFluidStack(drained);
		IAEFluidStack notInjected = injectFluid(toFill, Actionable.MODULATE);

		if (notInjected != null)
		{
			int amount = (int) (toFill.getStackSize() - notInjected.getStackSize());
			if (amount > 0)
			{
				facingTank.drain(side.getOpposite(), amount, true);
				return true;
			} else
			{
				return false;
			}
		} else
		{
			facingTank.drain(side.getOpposite(), toFill.getFluidStack(), true);
			return true;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource == null)
			return 0;

		FluidStack toFill = new FluidStack(resource.fluidID, 20);// TODO mb/t
		IAEFluidStack filled = injectFluid(AEApi.instance().storage().createFluidStack(toFill), Actionable.MODULATE);

		if (filled == null)
			return 20;
		if (filled != null)
			return toFill.amount - (int) filled.getStackSize();
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[0];
	}
}

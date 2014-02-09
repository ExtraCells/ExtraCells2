package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEFluidStack;
import extracells.TextureManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class PartFluidImport extends PartFluidIO implements IFluidHandler
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
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(4F, 4F, 15F, 12, 12, 16);
		bch.addBox(5F, 5F, 14F, 11, 11, 15);
		bch.addBox(6F, 6F, 12F, 10, 10, 14);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	public boolean doWork(int rate, int TicksSinceLastCall)
	{
		if (facingTank == null)
			return false;
		boolean empty = true;

		List<Fluid> filter = new ArrayList<Fluid>();
		filter.add(filterFluids[4]);

		if (filterSize >= 1)
		{
			for (byte i = 1; i < 9; i += 2)
			{
				if (i != 4)
				{
					filter.add(filterFluids[i]);
				}
			}
		}

		if (filterSize >= 2)
		{
			for (byte i = 0; i < 9; i += 2)
			{
				if (i != 4)
				{
					filter.add(filterFluids[i]);
				}
			}
		}

		for (Fluid fluid : filter)
		{
			if (fluid != null)
			{
				empty = false;

				if (fillToNetwork(fluid, rate * TicksSinceLastCall))
				{
					return true;
				}
			}
		}
		if (empty)
			return fillToNetwork(null, rate * TicksSinceLastCall);
		return false;
	}

	private boolean fillToNetwork(Fluid fluid, int toDrain)
	{
		FluidStack drained;
		if (fluid == null)
			drained = facingTank.drain(ForgeDirection.DOWN, toDrain, false);
		else
			drained = facingTank.drain(ForgeDirection.DOWN, new FluidStack(fluid, toDrain), false);

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

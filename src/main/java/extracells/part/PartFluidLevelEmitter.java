package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PartFluidLevelEmitter extends PartECBase implements IStackWatcherHost
{
	private Fluid fluid;
	private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
	private IStackWatcher watcher;
	private long wantedAmount;
	private long currentAmount;

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(TextureManager.BUS_SIDE.getTexture());
		rh.setBounds(7, 7, 11, 9, 9, 17);
		rh.renderInventoryBox(renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(TextureManager.BUS_SIDE.getTexture());
		rh.setBounds(7, 7, 11, 9, 9, 17);
		rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT(data);
		if (fluid != null)
			data.setString("fluid", fluid.getName());
		else
			data.removeTag("fluid");
		data.setInteger("mode", mode.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT(data);
		fluid = FluidRegistry.getFluid(data.getString("fluid"));
		mode = RedstoneMode.values()[data.getInteger("mode")];
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(7, 7, 11, 9, 9, 17);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 8;
	}

	@Override
	public void updateWatcher(IStackWatcher newWatcher)
	{
		watcher = newWatcher;
		if (fluid != null)
			watcher.add(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1)));
	}

	@Override
	public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan)
	{
		if (chan == StorageChannel.FLUIDS && diffStack != null && ((IAEFluidStack) diffStack).getFluid() == fluid)
		{
			currentAmount = fullStack != null ? fullStack.getStackSize() : 0;
		}
	}

	@Override
	public int isProvidingStrongPower()
	{
		return isPowering() ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return isPowering() ? 15 : 0;
	}

	private boolean isPowering()
	{
		switch (mode)
		{
		case LOW_SIGNAL:
			return wantedAmount >= currentAmount;
		case HIGH_SIGNAL:
			return wantedAmount <= currentAmount;
		default:
			return false;
		}
	}

	public void changeFluid(Fluid _fluid)
	{
		fluid = _fluid;
		watcher.clear();
		updateWatcher(watcher);
	}

	public void toggleMode()
	{
		switch (mode)
		{
		case LOW_SIGNAL:
			mode = RedstoneMode.HIGH_SIGNAL;
			break;
		default:
			mode = RedstoneMode.LOW_SIGNAL;
			break;
		}

		tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air);
		tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ, Blocks.air);
	}

}

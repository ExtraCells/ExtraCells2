package extracells.part;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import extracells.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class PartFluidLevelEmitter extends PartECBase implements IStackWatcherHost, IMEMonitorHandlerReciever<IAEFluidStack>
{
	private Fluid fluid;
	private boolean mode;
	private IStackWatcher watcher;
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
		if (fluid != null)
			data.setString("fluid", fluid.getName());
		else
			data.removeTag("fluid");
		data.setBoolean("mode", mode);
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		fluid = FluidRegistry.getFluid(data.getString("fluid"));
		mode = data.getBoolean("mode");
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
	public boolean isValid(Object verificationToken)
	{
		return true;
	}

	@Override
	public void postChange(IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource actionSource)
	{

	}

	@Override
	public void updateWatcher(IStackWatcher newWatcher)
	{
		watcher = newWatcher;
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
	public void addToWorld()
	{
		super.addToWorld();
		if (gridBlock != null)
		{
			IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
			if (monitor != null)
				monitor.addListener(this, null);
		}
	}

	@Override
	public void removeFromWorld()
	{
		if (gridBlock != null)
		{
			IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
			if (monitor != null)
				monitor.removeListener(this);
		}
		super.removeFromWorld();

	}
}

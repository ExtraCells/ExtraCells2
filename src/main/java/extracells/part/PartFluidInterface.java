package extracells.part;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.api.IFluidInterface;
import extracells.container.ContainerFluidInterface;
import extracells.container.IContainerListener;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.render.TextureManager;
import extracells.util.EmptyMeItemMonitor;
import akka.dispatch.Filter;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class PartFluidInterface extends PartECBase implements IFluidHandler, IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable, IGridTickable {

	List<IContainerListener> listeners = new ArrayList<IContainerListener>();
	
	private FluidTank tank = new FluidTank(10000)
			{
		public FluidTank readFromNBT(NBTTagCompound nbt)
		{
			if (!nbt.hasKey("Empty"))
			{
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
				setFluid(fluid);
			} else
			{
				setFluid(null);
			}
			return this;
		}
	};
	private int fluidFilter = -1;
	public boolean doNextUpdate = false;
	private boolean needBreake = false;
	private int tickCount = 0;
	
	@Override
	public void initializePart(ItemStack partStack) {
        if (partStack.hasTagCompound()) {
            readFilter(partStack.getTagCompound());
        }
    }
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null)
			return 0;
		
		if((tank.getFluid() == null || tank.getFluid().getFluid() == resource.getFluid()) && resource.getFluid() == FluidRegistry.getFluid(fluidFilter)){
			int added = tank.fill(resource.copy(), doFill);
			if(added == resource.amount){
				doNextUpdate = true;
				return added;
			}
			added += fillToNetwork(new FluidStack(resource.getFluid(), resource.amount - added), doFill);
			doNextUpdate = true;
			return added;
		}

		int filled = 0;
		filled += fillToNetwork(resource, doFill);

		if (filled < resource.amount)
			filled += tank.fill(new FluidStack(resource.fluidID, resource.amount - filled), doFill);
		if (filled > 0)
			getHost().markForUpdate();
		doNextUpdate = true;
		return filled;
	}
	
	public int fillToNetwork(FluidStack resource, boolean doFill) {
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if(node == null || resource == null)
			return 0;
		IGrid grid = node.getGrid();
		if(grid == null)
			return 0;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if(storage == null)
			return 0;
		IAEFluidStack notRemoved;
		FluidStack copy = resource.copy();
		if(doFill){
			notRemoved = storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(resource), Actionable.MODULATE, new MachineSource(this));
		}else{
			notRemoved = storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(resource), Actionable.SIMULATE, new MachineSource(this));
		}
		if(notRemoved == null)
			return resource.amount;
		return (int) (resource.amount - notRemoved.getStackSize());
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		FluidStack tankFluid = tank.getFluid();
		if (resource == null || tankFluid == null || tankFluid.getFluid() != resource.getFluid())
			return null;
		return drain(from, resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		FluidStack drained = tank.drain(maxDrain, doDrain);
		if (drained != null)
			getHost().markForUpdate();
		doNextUpdate = true;
		return drained;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return tank.fill(new FluidStack(fluid, 1), false) > 0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		FluidStack tankFluid = tank.getFluid();
		return tankFluid != null && tankFluid.getFluid() == fluid;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
	
	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.INTERFACE.getTextures()[0], side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.renderInventoryFace(TextureManager.INTERFACE.getTextures()[0], ForgeDirection.SOUTH, renderer);

        rh.setTexture(side);
        rh.setBounds(5, 5, 12, 11, 11, 14);
        rh.renderInventoryBox(renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.INTERFACE.getTextures()[0], side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        ts.setBrightness(20971520);
        rh.renderFace(x, y, z, TextureManager.INTERFACE.getTextures()[0], ForgeDirection.SOUTH, renderer);

        rh.setTexture(side);
        rh.setBounds(5, 5, 12, 11, 11, 14);
        rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 12, 11, 11, 14);

	}

	@Override
	public int cableConnectionRenderTo() {
		return 3;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
		return new EmptyMeItemMonitor();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
		if(getGridNode(ForgeDirection.UNKNOWN) == null)
			return null;
		IGrid grid = getGridNode(ForgeDirection.UNKNOWN).getGrid();
		if(grid == null)
			return null;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if(storage == null)
			return null;
		return storage.getFluidInventory();
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {
		return this;
	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		setFilter(ForgeDirection.getOrientation(_index), _fluid);
	}

	@Override
	public void setFilter(ForgeDirection side, Fluid fluid) {
		if(fluid == null){
			fluidFilter = -1;
			doNextUpdate = true;
			return;
		}
		fluidFilter = fluid.getID();
		doNextUpdate = true;
		
	}

	@Override
	public Fluid getFilter(ForgeDirection side) {
		return FluidRegistry.getFluid(fluidFilter);
	}

	@Override
	public IFluidTank getFluidTank(ForgeDirection side) {
		return tank;
	}

	@Override
	public void setFluidTank(ForgeDirection side, FluidStack fluid) {
		tank.setFluid(fluid);
		doNextUpdate = true;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 40, false, false);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		if(doNextUpdate)
			forceUpdate();
		IGrid grid = node.getGrid();
		if(grid == null)
			return TickRateModulation.URGENT;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if(storage == null)
			return TickRateModulation.URGENT;
		if(tank.getFluid() != null && FluidRegistry.getFluid(fluidFilter) != tank.getFluid().getFluid()){
			FluidStack s = tank.drain(20, false);
			if(s != null){
				IAEFluidStack notAdded = storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(s.copy()), Actionable.SIMULATE, new MachineSource(this));
				if(notAdded != null){
					int toAdd = (int) (s.amount - notAdded.getStackSize());
					storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(tank.drain(toAdd, true)), Actionable.MODULATE, new MachineSource(this));
					doNextUpdate = true;
					needBreake = false;
				}else{
					storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(tank.drain(s.amount, true)), Actionable.MODULATE, new MachineSource(this));
					doNextUpdate = true;
					needBreake = false;
				}
			}
		}
		if((tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.getFluid(fluidFilter)) && FluidRegistry.getFluid(fluidFilter) != null){
			IAEFluidStack extracted = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter), 20)), Actionable.SIMULATE, new MachineSource(this));
			if(extracted == null)
				return TickRateModulation.URGENT;
			int accepted = tank.fill(extracted.getFluidStack(), false);
			if(accepted == 0)
				return TickRateModulation.URGENT;
			tank.fill(storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter), accepted)), Actionable.MODULATE, new MachineSource(this)).getFluidStack(), true);
			doNextUpdate = true;
			needBreake = false;
		}
		return TickRateModulation.URGENT;
	}
	
	private void forceUpdate(){
		getHost().markForUpdate();
		for(IContainerListener listener : listeners){
			if(listener != null)
				listener.updateContainer();
		}
		doNextUpdate = false;
	}
	
	public void registerListener(IContainerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(IContainerListener listener){
		listeners.remove(listener);
	}
	
	@Override
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        tag.setInteger("filter", fluidFilter);
        ByteBufUtils.writeTag(data, tag);
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        NBTTagCompound tag = ByteBufUtils.readTag(data);
        if(tag.hasKey("tank"))
			tank.readFromNBT(tag.getCompoundTag("tank"));
		if(tag.hasKey("filter"))
			fluidFilter = tag.getInteger("filter");
        return true;
    }
    
    @Override
    public Object getServerGuiElement(EntityPlayer player) {
    	return new ContainerFluidInterface(player, this);
    }
    
    @Override
    public Object getClientGuiElement(EntityPlayer player) {
    	return new GuiFluidInterface(player, this, getSide());
    }
    
    @Override
    public ItemStack getItemStack(PartItemStack type) {
        ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1, PartEnum.getPartID(this));
        if (type != PartItemStack.Break) {
            is.setTagCompound(writeFilter(new NBTTagCompound()));
        }
        return is;
    }
    
    @Override
    public void writeToNBT(NBTTagCompound data){
    	super.writeToNBT(data);
    	data.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    	data.setInteger("filter", fluidFilter);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound data){
    	super.readFromNBT(data);
    	if(data.hasKey("tank"))
    		tank.readFromNBT(data.getCompoundTag("tank"));
    	if(data.hasKey("filter"))
    		fluidFilter = data.getInteger("filter");
    }
    
    public NBTTagCompound writeFilter(NBTTagCompound tag){
		if(FluidRegistry.getFluid(fluidFilter) == null)
			return null;
		tag.setInteger("filter", fluidFilter);
		return tag;
	}
	
	public void readFilter(NBTTagCompound tag){
		if(tag.hasKey("filter"))
			fluidFilter = tag.getInteger("filter");
	}

}

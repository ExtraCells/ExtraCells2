package extracells.tileentity;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import extracells.api.IECTileEntity;
import extracells.api.IFluidInterface;
import extracells.container.IContainerListener;
import extracells.gridblock.ECFluidGridBlock;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.part.PacketFluidInterface;
import extracells.util.EmptyMeItemMonitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;

public class TileEntityFluidInterface extends TileEntity implements IActionHost, IFluidHandler, IECTileEntity, IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable {
	
	List<IContainerListener> listeners = new ArrayList<IContainerListener>();
	
	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	public FluidTank[] tanks = new FluidTank[6];
	public Integer[] fluidFilter = new Integer[tanks.length];
	public boolean doNextUpdate = false;
	private boolean needBreake = false;
	private int tickCount = 0;
	
	private boolean isFirstGetGridNode = true;
	
	public TileEntityFluidInterface(){
		super();
		gridBlock = new ECFluidGridBlock(this);
		for (int i = 0; i < tanks.length; i++)
		{
			tanks[i] = new FluidTank(10000)
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
			fluidFilter[i] = -1;
		}
	}
	
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if(FMLCommonHandler.instance().getSide().isClient() && (getWorldObj() == null || getWorldObj().isRemote))
			return null;
		if(isFirstGetGridNode){
			isFirstGetGridNode = false;
			getActionableNode().updateState();
		}
		return node;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{	
		if (from == ForgeDirection.UNKNOWN || resource == null)
			return 0;
		
		if((tanks[from.ordinal()].getFluid() == null || tanks[from.ordinal()].getFluid().getFluid() == resource.getFluid()) && resource.getFluid() == FluidRegistry.getFluid(fluidFilter[from.ordinal()])){
			int added = tanks[from.ordinal()].fill(resource.copy(), doFill);
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
			filled += tanks[from.ordinal()].fill(new FluidStack(resource.fluidID, resource.amount - filled), doFill);
		if (filled > 0)
			if(getWorldObj() != null)
				getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
		doNextUpdate = true;
		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		FluidStack tankFluid = tanks[from.ordinal()].getFluid();
		if (resource == null || tankFluid == null || tankFluid.getFluid() != resource.getFluid())
			return null;
		return drain(from, resource.amount, doDrain);

	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (from == ForgeDirection.UNKNOWN)
			return null;
		FluidStack drained = tanks[from.ordinal()].drain(maxDrain, doDrain);
		if (drained != null)
			if(getWorldObj() != null)
				getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
		doNextUpdate = true;
		return drained;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from != ForgeDirection.UNKNOWN && tanks[from.ordinal()].fill(new FluidStack(fluid, 1), false) > 0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if (from == ForgeDirection.UNKNOWN)
			return false;
		FluidStack tankFluid = tanks[from.ordinal()].getFluid();
		return tankFluid != null && tankFluid.getFluid() == fluid;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if (from == ForgeDirection.UNKNOWN)
			return null;
		return new FluidTankInfo[]
		{ tanks[from.ordinal()].getInfo() };
	}

	@Override
	public IGridNode getActionableNode() {
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			return null;
		if(node == null){
			node = AEApi.instance().createGridNode(gridBlock);
		}
		return node;
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
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
	public void setFilter(ForgeDirection side, Fluid fluid) {
		if(side == null || side == ForgeDirection.UNKNOWN)
			return;
		if(fluid == null){
			fluidFilter[side.ordinal()] = -1;
			doNextUpdate = true;
			return;
		}
		fluidFilter[side.ordinal()] = fluid.getID();
		doNextUpdate = true;
	}

	@Override
	public Fluid getFilter(ForgeDirection side) {
		if(side == null || side == ForgeDirection.UNKNOWN)
			return null;
		return FluidRegistry.getFluid(fluidFilter[side.ordinal()]);
	}

	@Override
	public IFluidTank getFluidTank(ForgeDirection side) {
		if(side == null || side == ForgeDirection.UNKNOWN)
			return null;
		return tanks[side.ordinal()];
	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		setFilter(ForgeDirection.getOrientation(_index), _fluid);
	}

	@Override
	public void setFluidTank(ForgeDirection side, FluidStack fluid) {
		if(side == null || side == ForgeDirection.UNKNOWN)
			return;
		tanks[side.ordinal()].setFluid(fluid);
		doNextUpdate = true;
	}
	
	private void forceUpdate(){
		getWorldObj().markBlockForUpdate(yCoord, yCoord, zCoord);
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
	public void updateEntity(){
		if(getWorldObj() == null || getWorldObj().isRemote)
			return;
		if(doNextUpdate)
			forceUpdate();
		tick();
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag){
		super.writeToNBT(tag);
		for (int i = 0; i < tanks.length; i++)
		{
			tag.setTag("tank#"+i, tanks[i].writeToNBT(new NBTTagCompound()));
			tag.setInteger("filter#"+i, fluidFilter[i]);
		}
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if(node != null){
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tag.setTag("nodes", nodeTag);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag){
		super.readFromNBT(tag);
		for (int i = 0; i < tanks.length; i++)
		{
			if(tag.hasKey("tank#"+i))
				tanks[i].readFromNBT(tag.getCompoundTag("tank#"+i));
			if(tag.hasKey("filter#"+i))
				fluidFilter[i] = tag.getInteger("filter#"+i);
		}
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if(tag.hasKey("nodes") && node != null){
			node.loadFromNBT("node0", tag.getCompoundTag("nodes"));
			node.updateState();
		}
	}
	
	public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }
	
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
		readFromNBT(pkt.func_148857_g());
    }
	
	private void tick(){
		if(tickCount >= 40 || !needBreake){
			tickCount = 0;
			needBreake = true;
		}else{
			tickCount++;
			return;
		}
		if(node == null)
			return;
		IGrid grid = node.getGrid();
		if(grid == null)
			return;
		IStorageGrid storage = grid.getCache(IStorageGrid.class);
		if(storage == null)
			return;
		for (int i = 0; i < tanks.length; i++){
			if(tanks[i].getFluid() != null && FluidRegistry.getFluid(fluidFilter[i]) != tanks[i].getFluid().getFluid()){
				FluidStack s = tanks[i].drain(20, false);
				if(s != null){
					IAEFluidStack notAdded = storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(s.copy()), Actionable.SIMULATE, new MachineSource(this));
					if(notAdded != null){
						int toAdd = (int) (s.amount - notAdded.getStackSize());
						storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(tanks[i].drain(toAdd, true)), Actionable.MODULATE, new MachineSource(this));
						doNextUpdate = true;
						needBreake = false;
					}else{
						storage.getFluidInventory().injectItems(AEApi.instance().storage().createFluidStack(tanks[i].drain(s.amount, true)), Actionable.MODULATE, new MachineSource(this));
						doNextUpdate = true;
						needBreake = false;
					}
				}
			}
			if((tanks[i].getFluid() == null || tanks[i].getFluid().getFluid() == FluidRegistry.getFluid(fluidFilter[i])) && FluidRegistry.getFluid(fluidFilter[i]) != null){
				IAEFluidStack extracted = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter[i]), 20)), Actionable.SIMULATE, new MachineSource(this));
				if(extracted == null)
					continue;
				int accepted = tanks[i].fill(extracted.getFluidStack(), false);
				if(accepted == 0)
					continue;
				tanks[i].fill(storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter[i]), accepted)), Actionable.MODULATE, new MachineSource(this)).getFluidStack(), true);
				doNextUpdate = true;
				needBreake = false;
			}
		}
	}
	
	public NBTTagCompound writeFilter(NBTTagCompound tag){
		for (int i = 0; i < fluidFilter.length; i++){
			tag.setInteger("fluid#"+i, fluidFilter[i]);
		}
		return tag;
	}
	
	public void readFilter(NBTTagCompound tag){
		for (int i = 0; i < fluidFilter.length; i++){
			if(tag.hasKey("fluid#"+i))
				fluidFilter[i] = tag.getInteger("fluid#"+i);
		}
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {
		return this;
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

}

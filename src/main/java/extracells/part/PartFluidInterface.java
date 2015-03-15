package extracells.part;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.api.IFluidInterface;
import extracells.api.crafting.IFluidCraftingPatternDetails;
import extracells.container.ContainerFluidInterface;
import extracells.container.IContainerListener;
import extracells.crafting.CraftingPattern;
import extracells.crafting.CraftingPattern2;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.render.TextureManager;
import extracells.util.EmptyMeItemMonitor;
import extracells.util.PermissionUtil;
import akka.dispatch.Filter;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class PartFluidInterface extends PartECBase implements IFluidHandler, IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable, IGridTickable, ICraftingProvider {

	List<IContainerListener> listeners = new ArrayList<IContainerListener>();
	
	private List<ICraftingPatternDetails> patternHandlers = new ArrayList<ICraftingPatternDetails>();
	private HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails> patternConvert = new HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>();
	private List<IAEItemStack> requestedItems = new ArrayList<IAEItemStack>();
	private List<IAEItemStack> removeList = new ArrayList<IAEItemStack>();
	public final FluidInterfaceInventory inventory = new FluidInterfaceInventory();
	private boolean update = false;
	
	private List<IAEStack> export = new ArrayList<IAEStack>();
	private List<IAEStack> removeFromExport = new ArrayList<IAEStack>();
	private List<IAEStack> addToExport = new ArrayList<IAEStack>();
	
	private IAEItemStack toExport = null;
	private final Item encodedPattern = AEApi.instance().items().itemEncodedPattern.item();
	
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
		pushItems();
		if(toExport != null){
			storage.getItemInventory().injectItems(toExport, Actionable.MODULATE, new MachineSource(this));
			toExport = null;
		}
		if(update){
			update = false;
			if(getGridNode() != null && getGridNode().getGrid() !=  null){
            	getGridNode().getGrid().postEvent(new MENetworkCraftingPatternChange(this, getGridNode()));
            }
		}
		if(tank.getFluid() != null && FluidRegistry.getFluid(fluidFilter) != tank.getFluid().getFluid()){
			FluidStack s = tank.drain(125, false);
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
			IAEFluidStack extracted = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(FluidRegistry.getFluid(fluidFilter), 125)), Actionable.SIMULATE, new MachineSource(this));
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
        NBTTagCompound inventory = new NBTTagCompound();
    	this.inventory.writeToNBT(inventory);
    	tag.setTag("inventory", inventory);
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
		if(tag.hasKey("inventory"))
    		this.inventory.readFromNBT(tag.getCompoundTag("inventory"));
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
    	writeToNBTWithoutExport(data);
    	NBTTagCompound tag = new NBTTagCompound();
    	writeOutputToNBT(tag);
    	data.setTag("export", tag);
    }
    
    public void writeToNBTWithoutExport(NBTTagCompound data){
    	super.writeToNBT(data);
    	data.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    	data.setInteger("filter", fluidFilter);
    	NBTTagCompound inventory = new NBTTagCompound();
    	this.inventory.writeToNBT(inventory);
    	data.setTag("inventory", inventory);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound data){
    	super.readFromNBT(data);
    	if(data.hasKey("tank"))
    		tank.readFromNBT(data.getCompoundTag("tank"));
    	if(data.hasKey("filter"))
    		fluidFilter = data.getInteger("filter");
    	if(data.hasKey("inventory"))
    		this.inventory.readFromNBT(data.getCompoundTag("inventory"));
    	if(data.hasKey("export"))
        	readOutputFromNBT(data.getCompoundTag("export"));
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

	@Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
    	if(PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)){
    		return super.onActivate(player, pos);
    	}
    	return false;
    }
	
	@Override
	public boolean pushPattern(ICraftingPatternDetails patDetails,
			InventoryCrafting table) {
		if(isBusy() || (!patternConvert.containsKey(patDetails)))
			return false;
		ICraftingPatternDetails patternDetails = patternConvert.get(patDetails);
		if(patternDetails instanceof CraftingPattern){
			CraftingPattern patter = (CraftingPattern) patternDetails;
			HashMap<Fluid, Long> fluids = new HashMap<Fluid, Long>();
			for(IAEFluidStack stack : patter.getCondensedFluidInputs()){
				if(fluids.containsKey(stack.getFluid())){
					Long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
					fluids.remove(stack.getFluid());
					fluids.put(stack.getFluid(), amount);
				}else{
					fluids.put(stack.getFluid(), stack.getStackSize());
				}
			}
			IGrid grid = getGridNode().getGrid();
			if(grid == null)
				return false;
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if(storage == null)
				return false;
			for(Fluid fluid : fluids.keySet()){
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid,  (int) (amount+0))), Actionable.SIMULATE, new MachineSource(this));
				if(extractFluid == null || extractFluid.getStackSize() != amount){
					return false;
				}
			}
			for(Fluid fluid : fluids.keySet()){
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid,  (int) (amount+0))), Actionable.MODULATE, new MachineSource(this));
				export.add(extractFluid);
			}
			for(IAEItemStack s : patter.getCondensedInputs()){
				if(s == null)
					continue;
				if(s.getItem() == ItemEnum.FLUIDPATTERN.getItem()){
					toExport = s.copy();
					continue;
				}
				export.add(s);
			}
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return !export.isEmpty();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		patternHandlers = new ArrayList<ICraftingPatternDetails>();
		patternConvert.clear();
		
		for (ItemStack currentPatternStack : inventory.inv)
		{
			if (currentPatternStack != null && currentPatternStack.getItem() != null && currentPatternStack.getItem() instanceof ICraftingPatternItem)
			{
				ICraftingPatternItem currentPattern = (ICraftingPatternItem) currentPatternStack.getItem();

				if (currentPattern != null && currentPattern.getPatternForItem(currentPatternStack, getGridNode().getWorld()) != null)
				{
					IFluidCraftingPatternDetails pattern = new CraftingPattern2(currentPattern.getPatternForItem(currentPatternStack, getGridNode().getWorld()));
					patternHandlers.add(pattern);
					ItemStack is = makeCraftingPatternItem(pattern);
					if(is == null)
						continue;
					ICraftingPatternDetails p = ((ICraftingPatternItem)is.getItem()).getPatternForItem(is, getGridNode().getWorld());
					patternConvert.put(p, pattern);
					craftingTracker.addCraftingOption(this, p);
				}
			}
		}
	}
	
	private class FluidInterfaceInventory implements IInventory{

		private ItemStack[] inv = new ItemStack[9];

        @Override
        public int getSizeInventory() {
                return inv.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
                return inv[slot];
        }
        
        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
                inv[slot] = stack;
                if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                        stack.stackSize = getInventoryStackLimit();
                }
                update = true;
        }

        @Override
        public ItemStack decrStackSize(int slot, int amt) {
                ItemStack stack = getStackInSlot(slot);
                if (stack != null) {
                        if (stack.stackSize <= amt) {
                                setInventorySlotContents(slot, null);
                        } else {
                                stack = stack.splitStack(amt);
                                if (stack.stackSize == 0) {
                                        setInventorySlotContents(slot, null);
                                }
                        }
                }
                update = true;
                return stack;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
                return null;
        }
        
        @Override
        public int getInventoryStackLimit() {
                return 1;
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
                return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}
        
        public void readFromNBT(NBTTagCompound tagCompound) {
                
                NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
                for (int i = 0; i < tagList.tagCount(); i++) {
                        NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
                        byte slot = tag.getByte("Slot");
                        if (slot >= 0 && slot < inv.length) {
                                inv[slot] = ItemStack.loadItemStackFromNBT(tag);
                        }
                }
        }

        public void writeToNBT(NBTTagCompound tagCompound) {
                NBTTagList itemList = new NBTTagList();
                for (int i = 0; i < inv.length; i++) {
                        ItemStack stack = inv[i];
                        if (stack != null) {
                                NBTTagCompound tag = new NBTTagCompound();
                                tag.setByte("Slot", (byte) i);
                                stack.writeToNBT(tag);
                                itemList.appendTag(tag);
                        }
                }
                tagCompound.setTag("Inventory", itemList);
        }

		@Override
		public String getInventoryName() {
			return "inventory.fluidInterface";
		}

		@Override
		public boolean hasCustomInventoryName() {
			return false;
		}

		@Override
		public void markDirty() {}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			if(stack.getItem() instanceof ICraftingPatternItem){
				IGridNode n = getGridNode();
				World w;
				if(n == null){
					w = getClientWorld();
				}else{
					w = n.getWorld();
				}
				if(w == null)
					return false;
				ICraftingPatternDetails details =  ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, w);
				return (details != null);
			}
			return false;
		}
	}
	
	@SideOnly(Side.CLIENT)
	private World getClientWorld(){
		return Minecraft.getMinecraft().theWorld;
	}
	
	private void pushItems(){
		for(IAEStack s : removeFromExport){
			export.remove(s);
		}
		removeFromExport.clear();
		for(IAEStack s : addToExport){
			export.add(s);
		}
		addToExport.clear();
		if(getGridNode().getWorld() == null || export.isEmpty())
			return;
		ForgeDirection dir = getSide();
		TileEntity tile = getGridNode().getWorld().getTileEntity(getGridNode().getGridBlock().getLocation().x + dir.offsetX, getGridNode().getGridBlock().getLocation().y + dir.offsetY, getGridNode().getGridBlock().getLocation().z + dir.offsetZ);
		if(tile != null){
			IAEStack stack0 = export.iterator().next();
			IAEStack stack = stack0.copy();
			if(stack instanceof IAEItemStack && tile instanceof IInventory){
				if(tile instanceof ISidedInventory){
					ISidedInventory inv = (ISidedInventory) tile;
					for(int i : inv.getAccessibleSlotsFromSide(dir.getOpposite().ordinal())){
						if(inv.canInsertItem(i, ((IAEItemStack)stack).getItemStack(), dir.getOpposite().ordinal())){
							if(inv.getStackInSlot(i) == null){
								inv.setInventorySlotContents(i, ((IAEItemStack)stack).getItemStack());
								removeFromExport.add(stack0);
								return;
							}else if(ItemStack.areItemStackTagsEqual(inv.getStackInSlot(i), ((IAEItemStack)stack).getItemStack())){
								int max = inv.getInventoryStackLimit();
								int current = inv.getStackInSlot(i).stackSize;
								int outStack = (int) stack.getStackSize();
								if(max == current)
									continue;
								if(current + outStack <= max){
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = s.stackSize + outStack;
									inv.setInventorySlotContents(i, s);
									removeFromExport.add(stack0);
									return;
								}else{
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = max;
									inv.setInventorySlotContents(i, s);
									removeFromExport.add(stack0);
									stack.setStackSize(outStack - max + current);
									addToExport.add(stack);
									return;
								}
							}
						}
					}
				}else{
					IInventory inv = (IInventory) tile;
					for(int i = 0; i < inv.getSizeInventory(); i++){
						if(inv.isItemValidForSlot(i, ((IAEItemStack)stack).getItemStack())){
							if(inv.getStackInSlot(i) == null){
								inv.setInventorySlotContents(i, ((IAEItemStack)stack).getItemStack());
								removeFromExport.add(stack0);
								return;
							}else if(ItemStack.areItemStackTagsEqual(inv.getStackInSlot(i), ((IAEItemStack)stack).getItemStack())){
								int max = inv.getInventoryStackLimit();
								int current = inv.getStackInSlot(i).stackSize;
								int outStack = (int) stack.getStackSize();
								if(max == current)
									continue;
								if(current + outStack <= max){
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = s.stackSize + outStack;
									inv.setInventorySlotContents(i, s);
									removeFromExport.add(stack0);
									return;
								}else{
									ItemStack s = inv.getStackInSlot(i).copy();
									s.stackSize = max;
									inv.setInventorySlotContents(i, s);
									removeFromExport.add(stack0);
									stack.setStackSize(outStack - max + current);
									addToExport.add(stack);
									return;
								}
							}
						}
					}
				}
			}else if(stack instanceof IAEFluidStack && tile instanceof IFluidHandler){
				IFluidHandler handler = (IFluidHandler) tile;
				IAEFluidStack fluid = (IAEFluidStack) stack;
				if(handler.canFill(dir.getOpposite(), fluid.copy().getFluid())){
					int amount = handler.fill(dir.getOpposite(), fluid.getFluidStack().copy(), false);
					if(amount == 0)
						return;
					if(amount == fluid.getStackSize()){
						handler.fill(dir.getOpposite(), fluid.getFluidStack().copy(), true);
						removeFromExport.add(stack0);
					}else{
						IAEFluidStack f = fluid.copy();
						f.setStackSize(f.getStackSize() - amount);
						FluidStack fl = fluid.getFluidStack().copy();
						fl.amount = amount;
						handler.fill(dir.getOpposite(), fl, true);
						removeFromExport.add(stack0);
						addToExport.add(f);
						return;
					}
				}
			}
		}
	}

	@Override
	public IInventory getPatternInventory() {
		return inventory;
	}
	
	@Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack pattern = inventory.getStackInSlot(i);
			if (pattern != null)
				drops.add(pattern);
		}
    }
	
	@Override
    public NBTTagCompound getWailaTag(NBTTagCompound tag){
		if(tank.getFluid() == null || tank.getFluid().getFluid() == null)
			tag.setInteger("fluidID", -1);
		else
			tag.setInteger("fluidID", tank.getFluid().fluidID);
    	tag.setInteger("amount", tank.getFluidAmount());
    	return tag;
    }
    
    @Override
    public List<String> getWailaBodey(NBTTagCompound tag, List<String> list){
    	FluidStack fluid = null;
    	int id = -1;
    	int amount = 0;
    	if(tag.hasKey("fluidID") && tag.hasKey("amount")){
    		id = tag.getInteger("fluidID");
    		amount = tag.getInteger("amount");
    	}
    	if(id != -1)
    		fluid = new FluidStack(id, amount);
    	if(fluid == null){
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid") + ": " + StatCollector.translateToLocal("extracells.tooltip.empty1"));
			list.add(StatCollector.translateToLocal("extracells.tooltip.amount") + ": 0mB / 10000mB");
		}else{
			list.add(StatCollector.translateToLocal("extracells.tooltip.fluid") + ": " + fluid.getLocalizedName());
			list.add(StatCollector.translateToLocal("extracells.tooltip.amount") + ": " + fluid.amount + "mB / 10000mB");
		}
    	return list;
    }
    
    private ItemStack makeCraftingPatternItem(ICraftingPatternDetails details){
    	if(details == null)
    		return null;
    	NBTTagList in = new NBTTagList();
		NBTTagList out = new NBTTagList();
		for (IAEItemStack s : details.getInputs()){
			if(s == null)
				in.appendTag(new NBTTagCompound());
			else
				in.appendTag(s.getItemStack().writeToNBT(new NBTTagCompound()));
		}
		for (IAEItemStack s : details.getOutputs()){
			if(s == null)
				out.appendTag(new NBTTagCompound());
			else
				out.appendTag(s.getItemStack().writeToNBT(new NBTTagCompound()));
		}
		NBTTagCompound itemTag = new NBTTagCompound();
		itemTag.setTag("in", in);
		itemTag.setTag("out", out);
		itemTag.setBoolean("crafting", details.isCraftable());
		ItemStack pattern = new ItemStack(encodedPattern);
		pattern.setTagCompound(itemTag);
		return pattern;
    }
    
    private NBTTagCompound writeOutputToNBT(NBTTagCompound tag){
    	int i = 0;
    	for(IAEStack s : removeFromExport){
    		if(s != null){
    			tag.setBoolean("remove-" + i + "-isItem", s.isItem());
    			NBTTagCompound data = new NBTTagCompound();
    			if(s.isItem()){
    				((IAEItemStack)s).getItemStack().writeToNBT(data);
    			}else{
    				((IAEFluidStack)s).getFluidStack().writeToNBT(data);
    			}
    			tag.setTag("remove-" + i, data);
    			tag.setLong("remove-" + i +"-amount", s.getStackSize());
    		}
    		i++;
		}
    	tag.setInteger("remove", removeFromExport.size());
    	i = 0;
		for(IAEStack s : addToExport){
			if(s != null){
				tag.setBoolean("add-" + i + "-isItem", s.isItem());
    			NBTTagCompound data = new NBTTagCompound();
    			if(s.isItem()){
    				((IAEItemStack)s).getItemStack().writeToNBT(data);
    			}else{
    				((IAEFluidStack)s).getFluidStack().writeToNBT(data);
    			};
    			tag.setTag("add-" + i, data);
    			tag.setLong("add-" + i +"-amount", s.getStackSize());
    		}
			i++;
		}
		tag.setInteger("add", addToExport.size());
		i = 0;
		for(IAEStack s : export){
			if(s != null){
				tag.setBoolean("export-" + i + "-isItem", s.isItem());
    			NBTTagCompound data = new NBTTagCompound();
    			if(s.isItem()){
    				((IAEItemStack)s).getItemStack().writeToNBT(data);
    			}else{
    				((IAEFluidStack)s).getFluidStack().writeToNBT(data);
    			}
    			tag.setTag("export-" + i, data);
    			tag.setLong("export-" + i +"-amount", s.getStackSize());
    		}
			i++;
		}
		tag.setInteger("export", export.size());
    	return tag;
    }
    
    private void readOutputFromNBT(NBTTagCompound tag){
    	addToExport.clear();
    	removeFromExport.clear();
    	export.clear();
    	int i = tag.getInteger("remove");
    	for(int j = 0; j < i; j++){
    		if(tag.getBoolean("remove-" + j + "-isItem")){
    			IAEItemStack s = AEApi.instance().storage().createItemStack(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("remove-" + j)));
    			s.setStackSize(tag.getLong("remove-" + j +"-amount"));
    			removeFromExport.add(s);
    		}else{
    			IAEFluidStack s = AEApi.instance().storage().createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("remove-" + j)));
    			s.setStackSize(tag.getLong("remove-" + j +"-amount"));
    			removeFromExport.add(s);
    		}
    	}
    	i = tag.getInteger("add");
    	for(int j = 0; j < i; j++){
    		if(tag.getBoolean("add-" + j + "-isItem")){
    			IAEItemStack s = AEApi.instance().storage().createItemStack(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("add-" + j)));
    			s.setStackSize(tag.getLong("add-" + j +"-amount"));
    			addToExport.add(s);
    		}else{
    			IAEFluidStack s = AEApi.instance().storage().createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("add-" + j)));
    			s.setStackSize(tag.getLong("add-" + j +"-amount"));
    			addToExport.add(s);
    		}
    	}
    	i = tag.getInteger("export");
    	for(int j = 0; j < i; j++){
    		if(tag.getBoolean("export-" + j + "-isItem")){
    			IAEItemStack s = AEApi.instance().storage().createItemStack(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("export-" + j)));
    			s.setStackSize(tag.getLong("export-" + j +"-amount"));
    			export.add(s);
    		}else{
    			IAEFluidStack s = AEApi.instance().storage().createFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("export-" + j)));
    			s.setStackSize(tag.getLong("export-" + j +"-amount"));
    			export.add(s);
    		}
    	}
    }
}

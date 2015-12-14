package extracells.tileentity;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import extracells.api.IECTileEntity;
import extracells.gridblock.ECFluidGridBlock;
import extracells.util.FluidUtil;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class TileEntityFluidFiller extends TileBase implements IActionHost,
		ICraftingProvider, IECTileEntity,
		IMEMonitorHandlerReceiver<IAEFluidStack>, IListenerTile {

	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	List<Fluid> fluids = new ArrayList<Fluid>();
	public ItemStack containerItem = new ItemStack(Items.bucket);
	ItemStack returnStack = null;
	int ticksToFinish = 0;

	private boolean isFirstGetGridNode = true;

	private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orNull();

	public TileEntityFluidFiller() {
		super();
		this.gridBlock = new ECFluidGridBlock(this);
	}

	@MENetworkEventSubscribe
	public void cellUpdate(MENetworkCellArrayUpdate event) {
		IStorageGrid storage = getStorageGrid();
		if (storage != null)
			postChange(storage.getFluidInventory(), null, null);
	}

	@Override
	public IGridNode getActionableNode() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return null;
		if (this.node == null) {
			this.node = AEApi.instance().createGridNode(this.gridBlock);
		}
		return this.node;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		writeToNBT(nbtTag);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord,
				this.zCoord, 1, nbtTag);
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (FMLCommonHandler.instance().getSide().isClient()
				&& (getWorldObj() == null || getWorldObj().isRemote))
			return null;
		if (this.isFirstGetGridNode) {
			this.isFirstGetGridNode = false;
			getActionableNode().updateState();
			IStorageGrid storage = getStorageGrid();
			storage.getFluidInventory().addListener(this, null);
		}
		return this.node;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
	}

	private ItemStack getPattern(ItemStack emptyContainer,
			ItemStack filledContainer) {
		NBTTagList in = new NBTTagList();
		NBTTagList out = new NBTTagList();
		in.appendTag(emptyContainer.writeToNBT(new NBTTagCompound()));
		out.appendTag(filledContainer.writeToNBT(new NBTTagCompound()));
		NBTTagCompound itemTag = new NBTTagCompound();
		itemTag.setTag("in", in);
		itemTag.setTag("out", out);
		itemTag.setBoolean("crafting", false);
		ItemStack pattern = new ItemStack(this.encodedPattern);
		pattern.setTagCompound(itemTag);
		return pattern;
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	private IStorageGrid getStorageGrid() {
		this.node = getGridNode(ForgeDirection.UNKNOWN);
		if (this.node == null)
			return null;
		IGrid grid = this.node.getGrid();
		if (grid == null)
			return null;
		return grid.getCache(IStorageGrid.class);
	}

	@Override
	public boolean isBusy() {
		return this.returnStack != null;
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	@Override
	public void onListUpdate() {}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor,
			Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
		List<Fluid> oldFluids = new ArrayList<Fluid>(this.fluids);
		boolean mustUpdate = false;
		this.fluids.clear();
		for (IAEFluidStack fluid : ((IMEMonitor<IAEFluidStack>) monitor)
				.getStorageList()) {
			if (!oldFluids.contains(fluid.getFluid()))
				mustUpdate = true;
			else
				oldFluids.remove(fluid.getFluid());
			this.fluids.add(fluid.getFluid());
		}
		if (!(oldFluids.isEmpty() && !mustUpdate)) {
			if (getGridNode(ForgeDirection.UNKNOWN) != null
					&& getGridNode(ForgeDirection.UNKNOWN).getGrid() != null) {
				getGridNode(ForgeDirection.UNKNOWN).getGrid().postEvent(
						new MENetworkCraftingPatternChange(this,
								getGridNode(ForgeDirection.UNKNOWN)));
			}
		}
	}

	public void postUpdateEvent() {
		if (getGridNode(ForgeDirection.UNKNOWN) != null
				&& getGridNode(ForgeDirection.UNKNOWN).getGrid() != null) {
			getGridNode(ForgeDirection.UNKNOWN).getGrid().postEvent(
					new MENetworkCraftingPatternChange(this,
							getGridNode(ForgeDirection.UNKNOWN)));
		}
	}

	@MENetworkEventSubscribe
	public void powerUpdate(MENetworkPowerStatusChange event) {
		IStorageGrid storage = getStorageGrid();
		if (storage != null)
			postChange(storage.getFluidInventory(), null, null);
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		IStorageGrid storage = getStorageGrid();
		if (storage == null)
			return;
		IMEMonitor<IAEFluidStack> fluidStorage = storage.getFluidInventory();
		for (IAEFluidStack fluidStack : fluidStorage.getStorageList()) {
			Fluid fluid = fluidStack.getFluid();
			if (fluid == null)
				continue;
			int maxCapacity = FluidUtil.getCapacity(this.containerItem);
			if (maxCapacity == 0)
				continue;
			MutablePair<Integer, ItemStack> filled = FluidUtil.fillStack(
					this.containerItem.copy(), new FluidStack(fluid,
							maxCapacity));
			if (filled.right == null)
				continue;
			ItemStack pattern = getPattern(this.containerItem, filled.right);
			ICraftingPatternItem patter = (ICraftingPatternItem) pattern
					.getItem();
			craftingTracker.addCraftingOption(this,
					patter.getPatternForItem(pattern, getWorldObj()));
		}

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails,
			InventoryCrafting table) {
		if (this.returnStack != null)
			return false;
		ItemStack filled = patternDetails.getCondensedOutputs()[0]
				.getItemStack();
		FluidStack fluid = FluidUtil.getFluidFromContainer(filled);
		IStorageGrid storage = getStorageGrid();
		if (storage == null)
			return false;
		IAEFluidStack fluidStack = AEApi
				.instance()
				.storage()
				.createFluidStack(
						new FluidStack(
								fluid.getFluid(),
								FluidUtil.getCapacity(patternDetails
										.getCondensedInputs()[0].getItemStack())));
		IAEFluidStack extracted = storage.getFluidInventory()
				.extractItems(fluidStack.copy(), Actionable.SIMULATE,
						new MachineSource(this));
		if (extracted == null
				|| extracted.getStackSize() != fluidStack.getStackSize())
			return false;
		storage.getFluidInventory().extractItems(fluidStack,
				Actionable.MODULATE, new MachineSource(this));
		this.returnStack = filled;
		this.ticksToFinish = 40;
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		if (tagCompound.hasKey("container"))
			this.containerItem = ItemStack.loadItemStackFromNBT(tagCompound
					.getCompoundTag("container"));
		else if (tagCompound.hasKey("isContainerEmpty")
				&& tagCompound.getBoolean("isContainerEmpty"))
			this.containerItem = null;
		if (tagCompound.hasKey("return"))
			this.returnStack = ItemStack.loadItemStackFromNBT(tagCompound
					.getCompoundTag("return"));
		else if (tagCompound.hasKey("isReturnEmpty")
				&& tagCompound.getBoolean("isReturnEmpty"))
			this.returnStack = null;
		if (tagCompound.hasKey("time"))
			this.ticksToFinish = tagCompound.getInteger("time");
		if (hasWorldObj()) {
			IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
			if (tagCompound.hasKey("nodes") && node != null) {
				node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"));
				node.updateState();
			}
		}
	}

	@Override
	public void registerListener() {
		IStorageGrid storage = getStorageGrid();
		if (storage == null)
			return;
		postChange(storage.getFluidInventory(), null, null);
		storage.getFluidInventory().addListener(this, null);
	}

	@Override
	public void removeListener() {
		IStorageGrid storage = getStorageGrid();
		if (storage == null)
			return;
		storage.getFluidInventory().removeListener(this);
	}

	@Override
	public void securityBreak() {
		if (this.getWorldObj() != null)
			getWorldObj().func_147480_a(this.xCoord, this.yCoord, this.zCoord,
					true);
	}

	@Override
	public void updateEntity() {
		if (getWorldObj() == null || getWorldObj().provider == null)
			return;
		if (this.ticksToFinish > 0)
			this.ticksToFinish = this.ticksToFinish - 1;
		if (this.ticksToFinish <= 0 && this.returnStack != null) {
			IStorageGrid storage = getStorageGrid();
			if (storage == null)
				return;
			IAEItemStack toInject = AEApi.instance().storage()
					.createItemStack(this.returnStack);
			if (storage.getItemInventory().canAccept(toInject.copy())) {
				IAEItemStack nodAdded = storage.getItemInventory().injectItems(
						toInject.copy(), Actionable.SIMULATE,
						new MachineSource(this));
				if (nodAdded == null) {
					storage.getItemInventory().injectItems(toInject,
							Actionable.MODULATE, new MachineSource(this));
					this.returnStack = null;
				}
			}
		}
	}

	@Override
	public void updateGrid(IGrid oldGrid, IGrid newGrid) {
		if (oldGrid != null) {
			IStorageGrid storage = oldGrid.getCache(IStorageGrid.class);
			if (storage != null)
				storage.getFluidInventory().removeListener(this);
		}
		if (newGrid != null) {
			IStorageGrid storage = newGrid.getCache(IStorageGrid.class);
			if (storage != null)
				storage.getFluidInventory().addListener(this, null);;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (this.containerItem != null)
			tagCompound.setTag("container", this.containerItem.writeToNBT(new NBTTagCompound()));
		else
			tagCompound.setBoolean("isContainerEmpty", true);
		if (this.returnStack != null)
			tagCompound.setTag("return", this.returnStack.writeToNBT(new NBTTagCompound()));
		else
			tagCompound.setBoolean("isReturnEmpty", true);
		tagCompound.setInteger("time", this.ticksToFinish);
		if (!hasWorldObj())
			return;
		IGridNode node = getGridNode(ForgeDirection.UNKNOWN);
		if (node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tagCompound.setTag("nodes", nodeTag);
		}
	}
}

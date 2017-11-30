package extracells.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import appeng.api.networking.security.IActionSource;
import extracells.util.MachineSource;
import extracells.util.StorageChannels;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.container.fluid.ContainerFluidFiller;
import extracells.gridblock.ECFluidGridBlock;
import extracells.gui.fluid.GuiFluidFiller;
import extracells.network.IGuiProvider;
import extracells.util.AEUtils;
import extracells.util.FluidHelper;

public class TileEntityFluidFiller extends TileBase implements IActionHost, ICraftingProvider, IECTileEntity, IMEMonitorHandlerReceiver<IAEFluidStack>, IListenerTile, ITickable, IGuiProvider {

	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	List<Fluid> fluids = new ArrayList<Fluid>();
	public ItemStack containerItem = new ItemStack(Items.BUCKET);
	ItemStack returnStack = null;
	int ticksToFinish = 0;

	private boolean isFirstGetGridNode = true;

	private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orElse(null);

	public TileEntityFluidFiller() {
		super();
		this.gridBlock = new ECFluidGridBlock(this);
	}

	@MENetworkEventSubscribe
	public void cellUpdate(MENetworkCellArrayUpdate event) {
		IStorageGrid storage = getStorageGrid();
		if (storage != null) {
			postChange(storage.getInventory(StorageChannels.FLUID()), null, null);
		}
	}

	@Override
	public IGridNode getActionableNode() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return null;
		}
		if (this.node == null) {
			this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
		}
		return this.node;
	}

	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.DENSE_SMART;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public IGridNode getGridNode(AEPartLocation location) {
		if (FMLCommonHandler.instance().getSide().isClient()
			&& (world == null || world.isRemote)) {
			return null;
		}
		if (this.isFirstGetGridNode) {
			this.isFirstGetGridNode = false;
			getActionableNode().updateState();
			IStorageGrid storage = getStorageGrid();
			storage.getInventory(StorageChannels.FLUID()).addListener(this, null);
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
		this.node = getGridNode(AEPartLocation.INTERNAL);
		if (this.node == null) {
			return null;
		}
		IGrid grid = this.node.getGrid();
		if (grid == null) {
			return null;
		}
		return grid.getCache(IStorageGrid.class);
	}

	@Override
	public boolean isBusy() {
		return this.returnStack != null && !this.returnStack.isEmpty();
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void onListUpdate() {
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, IActionSource actionSource) {
		List<Fluid> oldFluids = new ArrayList<Fluid>(this.fluids);
		boolean mustUpdate = false;
		this.fluids.clear();
		for (IAEFluidStack fluid : ((IMEMonitor<IAEFluidStack>) monitor)
			.getStorageList()) {
			if (!oldFluids.contains(fluid.getFluid())) {
				mustUpdate = true;
			} else {
				oldFluids.remove(fluid.getFluid());
			}
			this.fluids.add(fluid.getFluid());
		}
		if (!(oldFluids.isEmpty() && !mustUpdate)) {
			if (getGridNode(AEPartLocation.INTERNAL) != null
				&& getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
				getGridNode(AEPartLocation.INTERNAL).getGrid().postEvent(
					new MENetworkCraftingPatternChange(this,
						getGridNode(AEPartLocation.INTERNAL)));
			}
		}
	}

	public void postUpdateEvent() {
		if (getGridNode(AEPartLocation.INTERNAL) != null
			&& getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
			getGridNode(AEPartLocation.INTERNAL).getGrid().postEvent(
				new MENetworkCraftingPatternChange(this,
					getGridNode(AEPartLocation.INTERNAL)));
		}
	}

	@MENetworkEventSubscribe
	public void powerUpdate(MENetworkPowerStatusChange event) {
		IStorageGrid storage = getStorageGrid();
		if (storage != null) {
			postChange(storage.getInventory(StorageChannels.FLUID()), null, null);
		}
	}

	HashMap<ICraftingPatternDetails, FluidStack> patternFluids = new HashMap<ICraftingPatternDetails, FluidStack>();

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		patternFluids.clear();
		IStorageGrid storage = getStorageGrid();
		if (storage == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> fluidStorage = storage.getInventory(StorageChannels.FLUID());
		for (IAEFluidStack fluidStack : fluidStorage.getStorageList()) {
			Fluid fluid = fluidStack.getFluid();
			if (fluid == null) {
				continue;
			}
			int maxCapacity = FluidHelper.getCapacity(this.containerItem);
			if (maxCapacity == 0) {
				continue;
			}
			Pair<Integer, ItemStack> filled = FluidHelper.fillStack(
				this.containerItem.copy(), new FluidStack(fluid,
					maxCapacity));
			if (filled.getRight() == null) {
				continue;
			}
			ItemStack pattern = getPattern(this.containerItem, filled.getRight());
			ICraftingPatternItem patter = (ICraftingPatternItem) pattern
				.getItem();
			ICraftingPatternDetails details = patter.getPatternForItem(pattern, world);
			if(details == null)
				continue;
			patternFluids.put(details, new FluidStack(fluid, filled.getLeft()));
			craftingTracker.addCraftingOption(this, details);
		}

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		if (this.returnStack != null && !this.returnStack.isEmpty()) {
			return false;
		}
		ItemStack filled = patternDetails.getCondensedOutputs()[0].getDefinition();

		if (!patternFluids.containsKey(patternDetails))
			return false;

		FluidStack fluid = patternFluids.get(patternDetails);
		IStorageGrid storage = getStorageGrid();
		if (storage == null || fluid == null) {
			return false;
		}
		IAEFluidStack fluidStack = AEUtils.createFluidStack(new FluidStack(fluid.getFluid(),
				FluidHelper.getCapacity(patternDetails.getCondensedInputs()[0].getDefinition())));
		IAEFluidStack extracted = storage.getInventory(StorageChannels.FLUID())
			.extractItems(fluidStack.copy(), Actionable.SIMULATE, new MachineSource(this));
		if (extracted == null || extracted.getStackSize() != fluidStack.getStackSize()) {
			return false;
		}
		storage.getInventory(StorageChannels.FLUID()).extractItems(fluidStack, Actionable.MODULATE, new MachineSource(this));
		this.returnStack = filled;
		this.ticksToFinish = 40;
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		if (tagCompound.hasKey("container")) {
			this.containerItem = new ItemStack(tagCompound
				.getCompoundTag("container"));
		} else if (tagCompound.hasKey("isContainerEmpty")
			&& tagCompound.getBoolean("isContainerEmpty")) {
			this.containerItem = null;
		}
		if (tagCompound.hasKey("return")) {
			this.returnStack = new ItemStack(tagCompound
				.getCompoundTag("return"));
		} else if (tagCompound.hasKey("isReturnEmpty")
			&& tagCompound.getBoolean("isReturnEmpty")) {
			this.returnStack = null;
		}
		if (tagCompound.hasKey("time")) {
			this.ticksToFinish = tagCompound.getInteger("time");
		}
		if (hasWorld()) {
			IGridNode node = getGridNode(AEPartLocation.INTERNAL);
			if (tagCompound.hasKey("nodes") && node != null) {
				node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"));
				node.updateState();
			}
		}
	}

	@Override
	public void registerListener() {
		IStorageGrid storage = getStorageGrid();
		if (storage == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(StorageChannels.FLUID());
		postChange(fluidInventory, null, null);
		fluidInventory.addListener(this, null);
	}

	@Override
	public void removeListener() {
		IStorageGrid storage = getStorageGrid();
		if (storage == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(StorageChannels.FLUID());
		fluidInventory.removeListener(this);
	}

	@Override
	public void securityBreak() {
		//TODO: Find out what func_147480_a is
		/*if (this.getWorldObj() != null)
			getWorldObj().func_147480_a(this.xCoord, this.yCoord, this.zCoord, true);*/
	}

	@Override
	public void update() {
		if (!hasWorld()) {
			return;
		}
		if (this.ticksToFinish > 0) {
			this.ticksToFinish = this.ticksToFinish - 1;
		}
		if (this.ticksToFinish <= 0 && this.returnStack != null && !this.returnStack.isEmpty()) {
			IStorageGrid storage = getStorageGrid();
			if (storage == null) {
				return;
			}
			IAEItemStack toInject = StorageChannels.ITEM().createStack(this.returnStack);
			if (storage.getInventory(StorageChannels.ITEM()).canAccept(toInject.copy())) {
				IAEItemStack nodAdded = storage.getInventory(StorageChannels.ITEM()).injectItems(
					toInject.copy(), Actionable.SIMULATE,
					new MachineSource(this));
				if (nodAdded == null) {
					storage.getInventory(StorageChannels.ITEM()).injectItems(toInject,
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
			if (storage != null) {
				storage.getInventory(StorageChannels.FLUID()).removeListener(this);
			}
		}
		if (newGrid != null) {
			IStorageGrid storage = newGrid.getCache(IStorageGrid.class);
			if (storage != null) {
				storage.getInventory(StorageChannels.FLUID()).addListener(this, null);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		if (this.containerItem != null) {
			tagCompound.setTag("container", this.containerItem.writeToNBT(new NBTTagCompound()));
		} else {
			tagCompound.setBoolean("isContainerEmpty", true);
		}
		if (this.returnStack != null && !this.returnStack.isEmpty()) {
			tagCompound.setTag("return", this.returnStack.writeToNBT(new NBTTagCompound()));
		} else {
			tagCompound.setBoolean("isReturnEmpty", true);
		}
		tagCompound.setInteger("time", this.ticksToFinish);
		if (!hasWorld()) {
			return tagCompound;
		}
		IGridNode node = getGridNode(AEPartLocation.INTERNAL);
		if (node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tagCompound.setTag("nodes", nodeTag);
		}
		return tagCompound;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiFluidFiller(player, this);
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerFluidFiller(player.inventory, this);
	}
}

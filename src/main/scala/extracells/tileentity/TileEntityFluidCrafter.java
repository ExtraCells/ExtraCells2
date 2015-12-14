package extracells.tileentity;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.*;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import extracells.api.IECTileEntity;
import extracells.crafting.CraftingPattern;
import extracells.gridblock.ECFluidGridBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEntityFluidCrafter extends TileBase implements IActionHost,
		ICraftingProvider, ICraftingWatcherHost, IECTileEntity {

	private class FluidCrafterInventory implements IInventory {

		private ItemStack[] inv = new ItemStack[9];

		@Override
		public void closeInventory() {}

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
			TileEntityFluidCrafter.this.update = true;
			return stack;
		}

		@Override
		public String getInventoryName() {
			return "inventory.fluidCrafter";
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public int getSizeInventory() {
			return this.inv.length;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return this.inv[slot];
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int slot) {
			return null;
		}

		@Override
		public boolean hasCustomInventoryName() {
			return false;
		}

		@Override
		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			if (stack.getItem() instanceof ICraftingPatternItem) {
				ICraftingPatternDetails details = ((ICraftingPatternItem) stack
						.getItem()).getPatternForItem(stack, getWorldObj());
				return details != null && details.isCraftable();
			}
			return false;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void markDirty() {}

		@Override
		public void openInventory() {}

		public void readFromNBT(NBTTagCompound tagCompound) {

			NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if (slot >= 0 && slot < this.inv.length) {
					this.inv[slot] = ItemStack.loadItemStackFromNBT(tag);
				}
			}
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			this.inv[slot] = stack;
			if (stack != null && stack.stackSize > getInventoryStackLimit()) {
				stack.stackSize = getInventoryStackLimit();
			}
			TileEntityFluidCrafter.this.update = true;
		}

		public void writeToNBT(NBTTagCompound tagCompound) {

			NBTTagList itemList = new NBTTagList();
			for (int i = 0; i < this.inv.length; i++) {
				ItemStack stack = this.inv[i];
				if (stack != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setByte("Slot", (byte) i);
					stack.writeToNBT(tag);
					itemList.appendTag(tag);
				}
			}
			tagCompound.setTag("Inventory", itemList);
		}

	}

	private ECFluidGridBlock gridBlock;
	private IGridNode node = null;
	private List<ICraftingPatternDetails> patternHandlers = new ArrayList<ICraftingPatternDetails>();
	private List<IAEItemStack> requestedItems = new ArrayList<IAEItemStack>();
	private List<IAEItemStack> removeList = new ArrayList<IAEItemStack>();
	private ICraftingPatternDetails[] patternHandlerSlot = new ICraftingPatternDetails[9];
	private ItemStack[] oldStack = new ItemStack[9];
	private boolean isBusy = false;

	private ICraftingWatcher watcher = null;

	private boolean isFirstGetGridNode = true;

	public final FluidCrafterInventory inventory;
	private Long finishCraftingTime = 0L;
	private ItemStack returnStack = null;

	private ItemStack[] optionalReturnStack = new ItemStack[0];

	private boolean update = false;

	private final TileEntityFluidCrafter instance;

	public TileEntityFluidCrafter() {
		super();
		this.gridBlock = new ECFluidGridBlock(this);
		this.inventory = new FluidCrafterInventory();
		this.instance = this;
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
		return AECableType.SMART;
	}

	public IGridNode getGridNode() {
		return getGridNode(ForgeDirection.UNKNOWN);
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (FMLCommonHandler.instance().getSide().isClient()
				&& (getWorldObj() == null || getWorldObj().isRemote))
			return null;
		if (this.isFirstGetGridNode) {
			this.isFirstGetGridNode = false;
			getActionableNode().updateState();
		}
		return this.node;
	}

	public IInventory getInventory() {
		return this.inventory;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(this);
	}

	@Override
	public double getPowerUsage() {
		return 0;
	}

	@Override
	public boolean isBusy() {
		return this.isBusy;
	}

	@Override
	public void onRequestChange(ICraftingGrid craftingGrid, IAEItemStack what) {
		if (craftingGrid.isRequesting(what)) {
			if (!this.requestedItems.contains(what)) {
				this.requestedItems.add(what);
			}
		} else if (this.requestedItems.contains(what)) {
			this.requestedItems.remove(what);
		}

	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.patternHandlers = new ArrayList<ICraftingPatternDetails>();
		ICraftingPatternDetails[] oldHandler = patternHandlerSlot;
		patternHandlerSlot = new ICraftingPatternDetails[9];
		for (int i = 0; this.inventory.inv.length > i; i++) {
			ItemStack currentPatternStack = this.inventory.inv[i];
			ItemStack oldItem = this.oldStack[i];
			if(currentPatternStack != null && oldItem != null && ItemStack.areItemStacksEqual(currentPatternStack, oldItem)){
				ICraftingPatternDetails pa = oldHandler[i];
				if(pa != null){
					patternHandlerSlot[i] = pa;
					patternHandlers.add(pa);
					if (pa.getCondensedInputs().length == 0) {
						craftingTracker.setEmitable(pa.getCondensedOutputs()[0]);
					} else {
						craftingTracker.addCraftingOption(this, pa);
					}
					continue;
				}
			}
			if (currentPatternStack != null
					&& currentPatternStack.getItem() != null
					&& currentPatternStack.getItem() instanceof ICraftingPatternItem) {
				ICraftingPatternItem currentPattern = (ICraftingPatternItem) currentPatternStack
						.getItem();

				if (currentPattern != null
						&& currentPattern.getPatternForItem(
								currentPatternStack, getWorldObj()) != null
						&& currentPattern.getPatternForItem(
								currentPatternStack, getWorldObj())
								.isCraftable()) {
					ICraftingPatternDetails pattern = new CraftingPattern(
							currentPattern.getPatternForItem(
									currentPatternStack, getWorldObj()));
					this.patternHandlers.add(pattern);
					this.patternHandlerSlot[i] = pattern;
					if (pattern.getCondensedInputs().length == 0) {
						craftingTracker.setEmitable(pattern
								.getCondensedOutputs()[0]);
					} else {
						craftingTracker.addCraftingOption(this, pattern);
					}
				}
			}
			oldStack[i] = currentPatternStack;
		}
		updateWatcher();
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails,
			InventoryCrafting table) {
		if (this.isBusy)
			return false;
		if (patternDetails instanceof CraftingPattern) {
			CraftingPattern patter = (CraftingPattern) patternDetails;
			HashMap<Fluid, Long> fluids = new HashMap<Fluid, Long>();
			for (IAEFluidStack stack : patter.getCondensedFluidInputs()) {
				if (fluids.containsKey(stack.getFluid())) {
					Long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
					fluids.remove(stack.getFluid());
					fluids.put(stack.getFluid(), amount);
				} else {
					fluids.put(stack.getFluid(), stack.getStackSize());
				}
			}
			IGrid grid = this.node.getGrid();
			if (grid == null)
				return false;
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if (storage == null)
				return false;
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, (int) (amount + 0))), Actionable.SIMULATE, new MachineSource(this));
				if (extractFluid == null || extractFluid.getStackSize() != amount) {
					return false;
				}
			}
			for (Fluid fluid : fluids.keySet()) {
				Long amount = fluids.get(fluid);
				IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, (int) (amount + 0))), Actionable.MODULATE, new MachineSource(this));
			}
			this.finishCraftingTime = System.currentTimeMillis() + 1000;

			this.returnStack = patter.getOutput(table, getWorldObj());

			this.optionalReturnStack = new ItemStack[9];
			for (int i = 0; i < 9; i++) {
				ItemStack s = table.getStackInSlot(i);
				if (s != null && s.getItem() != null) {
					this.optionalReturnStack[i] = s.getItem().getContainerItem(s.copy());
				}
			}

			this.isBusy = true;
		}
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.inventory.readFromNBT(tagCompound);
		if (hasWorldObj()) {
			IGridNode node = getGridNode();
			if (tagCompound.hasKey("nodes") && node != null) {
				node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"));
				node.updateState();
			}
		}
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public void updateEntity() {
		if (getWorldObj() == null || getWorldObj().provider == null)
			return;
		if (this.update) {
			this.update = false;
			if (getGridNode() != null && getGridNode().getGrid() != null) {
				getGridNode().getGrid().postEvent(new MENetworkCraftingPatternChange(this.instance, getGridNode()));
			}
		}
		if (this.isBusy && this.finishCraftingTime <= System.currentTimeMillis() && getWorldObj() != null && !getWorldObj().isRemote) {
			if (this.node == null || this.returnStack == null)
				return;
			IGrid grid = this.node.getGrid();
			if (grid == null)
				return;
			IStorageGrid storage = grid.getCache(IStorageGrid.class);
			if (storage == null)
				return;
			storage.getItemInventory().injectItems(AEApi.instance().storage().createItemStack(this.returnStack), Actionable.MODULATE, new MachineSource(this));
			for (ItemStack s : this.optionalReturnStack) {
				if (s == null)
					continue;
				storage.getItemInventory().injectItems(AEApi.instance().storage().createItemStack(s), Actionable.MODULATE, new MachineSource(this));
			}
			this.optionalReturnStack = new ItemStack[0];
			this.isBusy = false;
			this.returnStack = null;
		}
		if (!this.isBusy && getWorldObj() != null && !getWorldObj().isRemote) {
			for (IAEItemStack stack : this.removeList) {
				this.requestedItems.remove(stack);
			}
			this.removeList.clear();
			if (!this.requestedItems.isEmpty()) {
				for (IAEItemStack s : this.requestedItems) {
					IGrid grid = this.node.getGrid();
					if (grid == null)
						break;
					ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
					if (crafting == null)
						break;
					if (!crafting.isRequesting(s)) {
						this.removeList.add(s);
						continue;
					}
					for (ICraftingPatternDetails details : this.patternHandlers) {
						if (details.getCondensedOutputs()[0].equals(s)) {
							CraftingPattern patter = (CraftingPattern) details;
							HashMap<Fluid, Long> fluids = new HashMap<Fluid, Long>();
							for (IAEFluidStack stack : patter.getCondensedFluidInputs()) {
								if (fluids.containsKey(stack.getFluid())) {
									Long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
									fluids.remove(stack.getFluid());
									fluids.put(stack.getFluid(), amount);
								} else {
									fluids.put(stack.getFluid(), stack.getStackSize());
								}
							}
							IStorageGrid storage = grid.getCache(IStorageGrid.class);
							if (storage == null)
								break;
							boolean doBreak = false;
							for (Fluid fluid : fluids.keySet()) {
								Long amount = fluids.get(fluid);
								IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, (int) (amount + 0))), Actionable.SIMULATE, new MachineSource(this));
								if (extractFluid == null || extractFluid.getStackSize() != amount) {
									doBreak = true;
									break;
								}
							}
							if (doBreak)
								break;
							for (Fluid fluid : fluids.keySet()) {
								Long amount = fluids.get(fluid);
								IAEFluidStack extractFluid = storage.getFluidInventory().extractItems(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, (int) (amount + 0))), Actionable.MODULATE, new MachineSource(this));
							}
							this.finishCraftingTime = System.currentTimeMillis() + 1000;

							this.returnStack = patter.getCondensedOutputs()[0].getItemStack();
							this.isBusy = true;
							return;
						}
					}
				}
			}
		}
	}

	private void updateWatcher() {
		this.requestedItems = new ArrayList<IAEItemStack>();
		IGrid grid = null;
		IGridNode node = getGridNode();
		ICraftingGrid crafting = null;
		if (node != null) {
			grid = node.getGrid();
			if (grid != null) {
				crafting = grid.getCache(ICraftingGrid.class);
			}
		}
		for (ICraftingPatternDetails patter : this.patternHandlers) {
			this.watcher.clear();
			if (patter.getCondensedInputs().length == 0) {
				this.watcher.add(patter.getCondensedOutputs()[0]);

				if (crafting != null) {
					if (crafting.isRequesting(patter.getCondensedOutputs()[0])) {
						this.requestedItems
								.add(patter.getCondensedOutputs()[0]);
					}
				}
			}
		}
	}

	@Override
	public void updateWatcher(ICraftingWatcher newWatcher) {
		this.watcher = newWatcher;
		updateWatcher();
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		this.inventory.writeToNBT(tagCompound);
		if (!hasWorldObj())
			return;
		IGridNode node = getGridNode();
		if (node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			node.saveToNBT("node0", nodeTag);
			tagCompound.setTag("nodes", nodeTag);
		}
	}

}

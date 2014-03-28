package extracells.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiFluidStorage;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.inventory.HandlerItemStorageFluid;
import extracells.network.packet.part.PacketFluidStorage;
import extracells.util.FluidUtil;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

public class ContainerFluidStorage extends Container implements IMEMonitorHandlerReceiver<IAEFluidStack>, IFluidSelectorContainer, IInventoryUpdateReceiver
{
	private GuiFluidStorage guiFluidStorage;
	private IItemList<IAEFluidStack> fluidStackList;
	private Fluid selectedFluid;
	private IAEFluidStack selectedFluidStack;
	private EntityPlayer player;
	private IMEMonitor<IAEFluidStack> monitor;
	private HandlerItemStorageFluid storageFluid;
	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.item.fluid.storage", 2, 64, this)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemStack)
		{
			return FluidUtil.isFluidContainer(itemStack);
		}
	};

	public ContainerFluidStorage(IMEMonitor<IAEFluidStack> _monitor, EntityPlayer _player)
	{
		monitor = _monitor;
		player = _player;
		if (!player.worldObj.isRemote && monitor != null)
		{
			monitor.addListener(this, null);
			fluidStackList = monitor.getStorageList();
		} else
		{
			fluidStackList = AEApi.instance().storage().createFluidList();
		}

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(inventory, 0, 8, 74));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(player, inventory, 1, 26, 74));

		bindPlayerInventory(player.inventory);
	}

	public ContainerFluidStorage(EntityPlayer _player)
	{
		this(null, _player);
	}

	public void setGui(GuiFluidStorage _guiFluidStorage)
	{
		guiFluidStorage = _guiFluidStorage;
	}

	public Fluid getSelectedFluid()
	{
		return selectedFluid;
	}

	public IItemList<IAEFluidStack> getFluidStackList()
	{
		return fluidStackList;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 104));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 162));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public boolean isValid(Object verificationToken)
	{
		return true;
	}

	public void onContainerClosed(EntityPlayer entityPlayer)
	{
		super.onContainerClosed(entityPlayer);
		if (!entityPlayer.worldObj.isRemote)
		{
			monitor.removeListener(this);
			for (int i = 0; i < 2; i++)
				player.dropPlayerItemWithRandomChoice(((Slot) inventorySlots.get(i)).getStack(), false);
		}
	}

	@Override
	public void postChange(IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource actionSource)
	{
		fluidStackList = monitor.getStorageList();
		new PacketFluidStorage(player, fluidStackList).sendPacketToPlayer(player);
	}

	public void forceFluidUpdate()
	{
		if (monitor != null)
			new PacketFluidStorage(player, monitor.getStorageList()).sendPacketToPlayer(player);
	}

	public void updateFluidList(IItemList<IAEFluidStack> _fluidStackList)
	{
		fluidStackList = _fluidStackList;
		if (guiFluidStorage != null)
			guiFluidStorage.updateFluids();
	}

	public void setSelectedFluid(Fluid _selectedFluid)
	{
		new PacketFluidStorage(player, _selectedFluid).sendPacketToServer();
	}

	public void receiveSelectedFluid(Fluid _selectedFluid)
	{
		selectedFluid = _selectedFluid;
		if (selectedFluid != null)
		{
			for (IAEFluidStack stack : fluidStackList)
			{
				if (stack != null && stack.getFluid() == selectedFluid)
				{
					selectedFluidStack = stack;
					break;
				}
			}
		} else
		{
			selectedFluidStack = null;
		}
		guiFluidStorage.updateSelectedFluid();
	}

	public IAEFluidStack getSelectedFluidStack()
	{
		return selectedFluidStack;
	}

	public EntityPlayer getPlayer()
	{
		return player;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (inventory.isItemValidForSlot(0, itemstack1))
			{
				if (slotnumber == 0)
				{
					if (!mergeItemStack(itemstack1, 2, 36, false))
						return null;
				} else if (!mergeItemStack(itemstack1, 0, 1, false))
				{
					return null;
				}
				if (itemstack1.stackSize == 0)
				{
					slot.putStack(null);
				} else
				{
					slot.onSlotChanged();
				}
			} else
			{
				return null;
			}
		}
		return itemstack;
	}

	@Override
	public void onInventoryChanged()
	{

	}

	public void doWork()
	{
		ItemStack secondSlot = inventory.getStackInSlot(1);
		if (secondSlot != null && secondSlot.stackSize > 64)
			return;
		ItemStack container = inventory.getStackInSlot(0);
		if (!FluidUtil.isFluidContainer(container))
			return;
		if (monitor == null)
			return;

		if (FluidUtil.isEmpty(container))
		{
			if (selectedFluid == null)
				return;
			int capacity = FluidUtil.getCapacity(container);
			IAEFluidStack result = monitor.extractItems(FluidUtil.createAEFluidStack(selectedFluid, capacity), Actionable.SIMULATE, new PlayerSource(player, null));
			int proposedAmount = result == null ? 0 : (int) Math.min(capacity, result.getStackSize());
			MutablePair<Integer, ItemStack> filledContainer = FluidUtil.fillStack(container, new FluidStack(selectedFluid, proposedAmount));
			if (fillSecondSlot(filledContainer.getRight()))
			{
				monitor.extractItems(FluidUtil.createAEFluidStack(selectedFluid, filledContainer.getLeft()), Actionable.MODULATE, new PlayerSource(player, null));
				decreaseFirstSlot();
			}
		} else if (FluidUtil.isFilled(container))
		{
			FluidStack containerFluid = FluidUtil.getFluidFromContainer(container);
			IAEFluidStack notInjected = monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.SIMULATE, new PlayerSource(player, null));
			if (notInjected != null)
				return;
			MutablePair<Integer, ItemStack> drainedContainer = FluidUtil.drainStack(container, containerFluid);
			if (fillSecondSlot(drainedContainer.getRight()))
			{
				monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.MODULATE, new PlayerSource(player, null));
				decreaseFirstSlot();
			}
		}
	}

	public boolean fillSecondSlot(ItemStack itemStack)
	{
		if (itemStack == null)
			return false;
		ItemStack secondSlot = inventory.getStackInSlot(1);
		if (secondSlot == null)
		{
			inventory.setInventorySlotContents(1, itemStack);
			return true;
		} else
		{
			if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot))
				return false;
			inventory.incrStackSize(1, itemStack.stackSize);
			return true;
		}
	}

	public void decreaseFirstSlot()
	{
		ItemStack slot = inventory.getStackInSlot(0);
		if (slot == null)
			return;
		slot.stackSize--;
		if (slot.stackSize <= 0)
			inventory.setInventorySlotContents(0, null);
	}
}

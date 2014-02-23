package extracells.container;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiFluidStorage;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.inventoryHandler.HandlerItemStorageFluid;
import extracells.network.packet.PacketFluidStorage;
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
		setSelectedFluid(selectedFluid);
		if (guiFluidStorage != null)
			guiFluidStorage.updateFluids();
	}

	public void setSelectedFluid(Fluid _selectedFluid)
	{
		if (player.isClientWorld())
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
		} else
		{
			new PacketFluidStorage(player, selectedFluid).sendPacketToServer();
		}
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
					if (!mergeItemStack(itemstack1, 1, 36, false))
						return null;
				} else if (!mergeItemStack(itemstack1, 0, 0, false))
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
}

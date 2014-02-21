package extracells.container;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.PacketFluidTerminal;
import extracells.part.PartFluidTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

public class ContainerFluidTerminal extends Container implements IMEMonitorHandlerReceiver<IAEFluidStack>
{
	private PartFluidTerminal terminal;
	private IMEMonitor<IAEFluidStack> monitor;
	private IItemList<IAEFluidStack> fluidStackList = AEApi.instance().storage().createFluidList();
	private Fluid selectedFluid;
	private EntityPlayer player;
	private GuiFluidTerminal guiFluidTerminal;

	public ContainerFluidTerminal(PartFluidTerminal _terminal, EntityPlayer _player)
	{
		terminal = _terminal;
		player = _player;
		if (!player.worldObj.isRemote)
		{
			monitor = terminal.getGridBlock().getFluidMonitor();
			if (monitor != null)
			{
				monitor.addListener(this, null);
				fluidStackList = monitor.getStorageList();
			}
			terminal.addContainer(this);
		}

		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(terminal.getInventory(), 0, 8, 74));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(player, terminal.getInventory(), 1, 26, 74));
		bindPlayerInventory(player.inventory);
	}

	public PartFluidTerminal getTerminal()
	{
		return terminal;
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

	public void setGui(GuiFluidTerminal _guiFluidTerminal)
	{
		guiFluidTerminal = _guiFluidTerminal;
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
			terminal.removeContainer(this);
		}
	}

	@Override
	public void postChange(IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource actionSource)
	{
		fluidStackList = monitor.getStorageList();
		new PacketFluidTerminal(player, fluidStackList).sendPacketToPlayer(player);
	}

	public void forceFluidUpdate()
	{
		if (monitor != null)
			new PacketFluidTerminal(player, monitor.getStorageList()).sendPacketToPlayer(player);
	}

	public void updateFluidList(IItemList<IAEFluidStack> _fluidStackList)
	{
		fluidStackList = _fluidStackList;
		if (guiFluidTerminal != null)
			guiFluidTerminal.updateFluids();
	}

	public Fluid getSelectedFluid()
	{
		return selectedFluid;
	}

	public void setSelectedFluid(Fluid _selectedFluid)
	{
		if (player.worldObj.isRemote)
		{
			selectedFluid = _selectedFluid;
			guiFluidTerminal.updateSelectedFluid();
		} else
		{
			new PacketFluidTerminal(player, selectedFluid, terminal).sendPacketToServer();
		}
	}

	public IItemList<IAEFluidStack> getFluidStackList()
	{
		return fluidStackList;
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
			if (terminal.getInventory().isItemValidForSlot(0, itemstack1))
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
}

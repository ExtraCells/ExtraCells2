package extracells.container;

import appeng.api.AEApi;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import extracells.gui.GuiTerminalFluid;
import extracells.network.packet.PacketTerminalFluid;
import extracells.part.FluidTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

public class ContainerTerminalFluid extends Container implements IMEMonitorHandlerReciever<IAEFluidStack>
{
	private FluidTerminal terminal;
	private IMEMonitor<IAEFluidStack> monitor;
	private IItemList<IAEFluidStack> fluidStackList = AEApi.instance().storage().createItemList();
	private Fluid selectedFluid;
	private EntityPlayer player;
	private GuiTerminalFluid guiTerminalFluid;

	public ContainerTerminalFluid(FluidTerminal _terminal, EntityPlayer _player)
	{
		terminal = _terminal;
		player = _player;
		if (!player.worldObj.isRemote)
		{
			monitor = terminal.getGridBlock().getMonitor();
			if (monitor != null)
				monitor.addListener(this, null);
			terminal.addContainer(this);
		}/*/
		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(terminal.getInventory(), 0, 8, 74));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotFurnace(player, terminal.getInventory(), 1, 26, 74));//*/

		bindPlayerInventory(player.inventory);
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

	public void setGui(GuiTerminalFluid _guiTerminalFluid)
	{
		guiTerminalFluid = _guiTerminalFluid;
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
		PacketDispatcher.sendPacketToPlayer(new PacketTerminalFluid(fluidStackList).makePacket(), (Player) player);
	}

	public void updateFluidList(IItemList<IAEFluidStack> _fluidStackList)
	{
		fluidStackList = _fluidStackList;
		guiTerminalFluid.updateFluids();
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
			guiTerminalFluid.updateSelectedFluid();
		} else
		{
			PacketDispatcher.sendPacketToServer(new PacketTerminalFluid(selectedFluid, terminal).makePacket());
		}
	}

	public IItemList<IAEFluidStack> getFluidStackList()
	{
		return fluidStackList;
	}

	public Player getPlayer()
	{
		return (Player) player;
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
				if (slotnumber == 1 || slotnumber == 0)
				{
					if (!mergeItemStack(itemstack1, 3, 38, false))
						return null;
				} else if (slotnumber != 1 && slotnumber != 0)
				{
					if (!mergeItemStack(itemstack1, 0, 1, false))
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

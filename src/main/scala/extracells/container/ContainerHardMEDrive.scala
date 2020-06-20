package extracells.container

import appeng.api.AEApi
import extracells.container.slot.SlotRespective
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{Container, Slot}
import net.minecraft.item.ItemStack


class ContainerHardMEDrive(inventory: InventoryPlayer, tile: TileEntityHardMeDrive) extends Container{


  for (i <- 0 to 2)
  {
    addSlotToContainer(new SlotRespective(tile.getInventory, i, 80, 17 + i * 18)
    {
      override def isItemValid(item: ItemStack) =  AEApi.instance.registries.cell().isCellHandled(item)
    });
  }

  bindPlayerInventory()

  protected def bindPlayerInventory(): Unit =
  {
    for (i <- 0 to 2)
    {
      for (j <- 0 to 8)
      {
        addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
      }
    }

    for (i <- 0 to 8)
    {
      addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 142));
    }
  }


  override def transferStackInSlot(p: EntityPlayer, i: Int): ItemStack = {
    var itemstack: ItemStack = null
    val slot = inventorySlots.get(i).asInstanceOf[Slot]
    if (slot != null && slot.getHasStack())
    {
      val itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      if (AEApi.instance.registries.cell().isCellHandled(itemstack))
      {
        if (i < 3)
        {
          if (!mergeItemStack(itemstack1, 3, 38, false))
          {
            return null
          }
        } else if (!mergeItemStack(itemstack1, 0, 3, false))
        {
          return null
        }
        if (itemstack1.stackSize == 0)
        {
          slot.putStack(null)
        } else
        {
          slot.onSlotChanged()
        }
      }
    }
    itemstack
  }

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = {
    if (tile.hasWorldObj)
      tile.getWorldObj.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == this.tile
    else
      false
  }
}

package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;

import extracells.network.packet.part.PacketOreDictExport;
import extracells.part.PartOreDictExporter;
import extracells.util.NetworkUtil;

public class ContainerOreDictExport extends Container {
	public PartOreDictExporter part;
	EntityPlayer player;

	public ContainerOreDictExport(EntityPlayer player, PartOreDictExporter _part) {
		this.player = player;
		this.part = _part;
		bindPlayerInventory(player.inventory);
		TileEntity tile = this.part.getHostTile();
		if (tile != null && tile.hasWorld() && !tile.getWorld().isRemote) {
			NetworkUtil.sendToPlayer(new PacketOreDictExport(this.part.getFilter()), player);
		}
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(inventoryPlayer,
					j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18,
				142));
		}
	}

	public void setFilter(String filter) {
		part.setFilter(filter);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

//	@Override
//	protected void retrySlotClick(int p_75133_1_, int p_75133_2_,
//		boolean p_75133_3_, EntityPlayer p_75133_4_) {
//
//	}

}

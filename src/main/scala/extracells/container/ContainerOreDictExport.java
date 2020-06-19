package extracells.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.gui.GuiFluidInterface;
import extracells.network.packet.part.PacketOreDictExport;
import extracells.part.PartOreDictExporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;

public class ContainerOreDictExport extends Container {
	public PartOreDictExporter part;
	@SideOnly(Side.CLIENT)
	public GuiFluidInterface gui;
	EntityPlayer player;

	public ContainerOreDictExport(EntityPlayer player, PartOreDictExporter _part) {
		this.player = player;
		this.part = _part;
		bindPlayerInventory(player.inventory);
		TileEntity tile = this.part.getHostTile();
		if (tile != null && tile.hasWorldObj() && !tile.getWorldObj().isRemote) {
			new PacketOreDictExport(player, this.part.getFilter(), Side.CLIENT)
					.sendPacketToPlayer(player);
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

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return this.part.isValid();
	}

	@Override
	protected void retrySlotClick(int p_75133_1_, int p_75133_2_,
			boolean p_75133_3_, EntityPlayer p_75133_4_) {

	}

}

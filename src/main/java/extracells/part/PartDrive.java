package extracells.part;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.*;
import extracells.container.ContainerDrive;
import extracells.gui.GuiDrive;
import extracells.render.TextureManager;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartDrive extends PartECBase implements ICellContainer, IInventoryUpdateReceiver
{
	private int priority = 0; // TODO
	private short[] blinkTimers; // TODO
	private byte[] cellStati = new byte[6];
	List<IMEInventoryHandler> fluidHandlers = new ArrayList<IMEInventoryHandler>();
	List<IMEInventoryHandler> itemHandlers = new ArrayList<IMEInventoryHandler>();
	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.drive", 6, 1, this)
	{
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();

		public boolean isItemValidForSlot(int i, ItemStack itemStack)
		{
			return cellRegistry.isCellHandled(itemStack);
		}
	};

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 15.999F);
		rh.renderInventoryFace(front[3], ForgeDirection.SOUTH, renderer);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 15.999F);
		rh.renderFace(x, y, z, front[3], ForgeDirection.SOUTH, renderer);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderBlock(x, y, z, renderer);

		ts.setColorOpaque_I(0xFFFFFF);
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				if (cellStati[j + i * 3] > 0)
				{
					rh.setBounds(4 + i * 5, 12 - j * 3, 14, 7 + i * 5, 10 - j * 3, 16);
					rh.renderFace(x, y, z, front[1], ForgeDirection.SOUTH, renderer);
				}
			}
		}

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				rh.setBounds(4 + i * 5, 12 - j * 3, 14, 7 + i * 5, 10 - j * 3, 16);
				ts.setColorOpaque_I(getColorByStatus(cellStati[j + i * 3]));
				ts.setBrightness(13 << 20 | 13 << 4);
				rh.renderFace(x, y, z, front[2], ForgeDirection.SOUTH, renderer);
			}
		}
		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{
		super.writeToStream(data);
		for (int i = 0; i < cellStati.length; i++)
			data.writeByte(cellStati[i]);
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		super.readFromStream(data);
		for (int i = 0; i < cellStati.length; i++)
			cellStati[i] = data.readByte();
		return true;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
	}

	public int getColorByStatus(int status)
	{
		switch (status)
		{
		case 1:
			return 0x00FF00;
		case 2:
			return 0xFFFF00;
		case 3:
			return 0xFF0000;
		default:
			return 0x000000;
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 2;
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT(data);
		data.setTag("inventory", inventory.writeToNBT());
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT(data);
		inventory.readFromNBT(data.getTagList("inventory", 10));
		onInventoryChanged();
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		return channel == StorageChannel.ITEMS ? itemHandlers : fluidHandlers;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void blinkCell(int slot)
	{
		if (slot > 0 && slot < blinkTimers.length)
			blinkTimers[slot] = 15;
	}

	@Override
	public void onInventoryChanged()
	{
		itemHandlers = updateHandlers(StorageChannel.ITEMS);
		fluidHandlers = updateHandlers(StorageChannel.FLUIDS);
		IGridNode node = getGridNode();
		for (int i = 0; i < cellStati.length; i++)
		{
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			IMEInventoryHandler inventoryHandler = AEApi.instance().registries().cell().getCellInventory(stackInSlot, StorageChannel.ITEMS);
			if (inventoryHandler == null)
				inventoryHandler = AEApi.instance().registries().cell().getCellInventory(stackInSlot, StorageChannel.FLUIDS);

			ICellHandler cellHandler = AEApi.instance().registries().cell().getHandler(stackInSlot);
			if (cellHandler == null || inventoryHandler == null)
			{
				cellStati[i] = 0;
			} else
			{
				cellStati[i] = (byte) cellHandler.getStatusForCell(stackInSlot, inventoryHandler);
			}
		}
		if (node != null)
		{
			IGrid grid = node.getGrid();
			if (grid != null)
			{
				grid.postEvent(new MENetworkCellArrayUpdate());
			}
			host.markForUpdate();
		}
	}

	private List<IMEInventoryHandler> updateHandlers(StorageChannel channel)
	{
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		List<IMEInventoryHandler> handlers = new ArrayList<IMEInventoryHandler>();
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack cell = inventory.getStackInSlot(i);
			if (cellRegistry.isCellHandled(cell))
			{
				IMEInventoryHandler cellInventory = cellRegistry.getCellInventory(cell, channel);
				if (cellInventory != null)
					handlers.add(cellInventory);
			}
		}
		return handlers;
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	public Object getServerGuiElement(EntityPlayer player)
	{
		return new ContainerDrive(this, player);
	}

	public Object getClientGuiElement(EntityPlayer player)
	{
		return new GuiDrive(this, player);
	}
}

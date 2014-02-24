package extracells.part;

import appeng.api.AEApi;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.util.AEColor;
import extracells.render.TextureManager;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class PartDrive extends PartECBase implements ICellContainer, IInventoryUpdateReceiver
{
	private int priority = 0; //TODO
	private short[] blinkTimers;
	List<IMEInventoryHandler> fluidHandlers = new ArrayList<IMEInventoryHandler>();
	List<IMEInventoryHandler> itemHandlers = new ArrayList<IMEInventoryHandler>();
	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.drive", 6, 1)
	{
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();

		public boolean isItemValidForSlot(int i, ItemStack itemStack)
		{
			return cellRegistry.isCellHandled(itemStack);
		}
	};

	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderInventoryBox(renderer);
		rh.renderInventoryFace(front[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Green.blackVariant);
		Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
		rh.renderInventoryFace(front[2], ForgeDirection.SOUTH, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.renderFace((int) x, (int) y, (int) z, front[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Green.blackVariant);
		Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
		rh.renderFace((int) x, (int) y, (int) z, front[2], ForgeDirection.SOUTH, renderer);
		GL11.glPopMatrix();
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(2, 2, 14, 14, 14, 16);
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
}

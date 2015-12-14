package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerDrive;
import extracells.gui.GuiDrive;
import extracells.render.TextureManager;
import extracells.util.PermissionUtil;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartDrive extends PartECBase implements ICellContainer,
		IInventoryUpdateReceiver {

	private int priority = 0; // TODO
	private short[] blinkTimers; // TODO
	private byte[] cellStatuses = new byte[6];
	List<IMEInventoryHandler> fluidHandlers = new ArrayList<IMEInventoryHandler>();
	List<IMEInventoryHandler> itemHandlers = new ArrayList<IMEInventoryHandler>();
	private ECPrivateInventory inventory = new ECPrivateInventory(
			"extracells.part.drive", 6, 1, this) {

		ICellRegistry cellRegistry = AEApi.instance().registries().cell();

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return this.cellRegistry.isCellHandled(itemStack);
		}
	};

	@Override
	public void addToWorld() {
		super.addToWorld();
		onInventoryChanged();
	}

	@Override
	public void blinkCell(int slot) {
		if (slot > 0 && slot < this.blinkTimers.length)
			this.blinkTimers[slot] = 15;
	}

	@Override
	public int cableConnectionRenderTo() {
		return 2;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
		if (!isActive())
			return new ArrayList<IMEInventoryHandler>();
		return channel == StorageChannel.ITEMS ? this.itemHandlers
				: this.fluidHandlers;
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiDrive(this, player);
	}

	public int getColorByStatus(int status) {
		switch (status) {
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
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		if (!wrenched)
			for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
				ItemStack cell = this.inventory.getStackInSlot(i);
				if (cell != null)
					drops.add(cell);
			}
	}

	public ECPrivateInventory getInventory() {
		return this.inventory;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerDrive(this, player);
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
				(IPart) this)) {
			return super.onActivate(player, pos);
		}
		return false;
	}

	@Override
	public void onInventoryChanged() {
		this.itemHandlers = updateHandlers(StorageChannel.ITEMS);
		this.fluidHandlers = updateHandlers(StorageChannel.FLUIDS);
		for (int i = 0; i < this.cellStatuses.length; i++) {
			ItemStack stackInSlot = this.inventory.getStackInSlot(i);
			IMEInventoryHandler inventoryHandler = AEApi.instance()
					.registries().cell()
					.getCellInventory(stackInSlot, null, StorageChannel.ITEMS);
			if (inventoryHandler == null)
				inventoryHandler = AEApi
						.instance()
						.registries()
						.cell()
						.getCellInventory(stackInSlot, null,
								StorageChannel.FLUIDS);

			ICellHandler cellHandler = AEApi.instance().registries().cell()
					.getHandler(stackInSlot);
			if (cellHandler == null || inventoryHandler == null) {
				this.cellStatuses[i] = 0;
			} else {
				this.cellStatuses[i] = (byte) cellHandler.getStatusForCell(
						stackInSlot, inventoryHandler);
			}
		}
		IGridNode node = getGridNode();
		if (node != null) {
			IGrid grid = node.getGrid();
			if (grid != null) {
				grid.postEvent(new MENetworkCellArrayUpdate());
			}
			getHost().markForUpdate();
		}
		saveData();
	}

	@MENetworkEventSubscribe
	public void powerChange(MENetworkPowerStatusChange event) {
		IGridNode node = getGridNode();
		if (node != null) {
			boolean isNowActive = node.isActive();
			if (isNowActive != isActive()) {
				setActive(isNowActive);
				onNeighborChanged();
				getHost().markForUpdate();
			}
		}
		node.getGrid().postEvent(new MENetworkCellArrayUpdate());
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.inventory.readFromNBT(data.getTagList("inventory", 10));
		onInventoryChanged();
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		for (int i = 0; i < this.cellStatuses.length; i++)
			this.cellStatuses[i] = data.readByte();
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		IIcon side = TextureManager.DRIVE_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 15.999F);
		rh.renderInventoryFace(front[3], ForgeDirection.SOUTH, renderer);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;
		IIcon side = TextureManager.DRIVE_SIDE.getTexture();
		IIcon[] front = TextureManager.DRIVE_FRONT.getTextures();
		rh.setBounds(2, 2, 14, 14, 14, 15.999F);
		rh.renderFace(x, y, z, front[3], ForgeDirection.SOUTH, renderer);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.setTexture(side, side, side, front[0], side, side);
		rh.renderBlock(x, y, z, renderer);

		ts.setColorOpaque_I(0xFFFFFF);
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				if (this.cellStatuses[j + i * 3] > 0) {
					if (getSide() == ForgeDirection.EAST
							|| getSide() == ForgeDirection.WEST ? i == 1
							: i == 0)
						rh.setBounds(8, 12 - j * 3, 14, 13, 10 - j * 3, 16);
					else
						rh.setBounds(3, 12 - j * 3, 14, 8, 10 - j * 3, 16);
					rh.renderFace(x, y, z, front[1], ForgeDirection.SOUTH,
							renderer);
				}
			}
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				if (getSide() == ForgeDirection.EAST
						|| getSide() == ForgeDirection.WEST ? i == 1 : i == 0)
					rh.setBounds(8, 12 - j * 3, 14, 13, 10 - j * 3, 16);
				else
					rh.setBounds(3, 12 - j * 3, 14, 8, 10 - j * 3, 16);
				ts.setColorOpaque_I(getColorByStatus(this.cellStatuses[j + i
						* 3]));
				ts.setBrightness(13 << 20 | 13 << 4);
				rh.renderFace(x, y, z, front[2], ForgeDirection.SOUTH, renderer);
			}
		}
		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	@Override
	public void saveChanges(IMEInventory cellInventory) {
		getHost().markForSave();
	}

	@Override
	public void setPartHostInfo(ForgeDirection _side, IPartHost _host,
			TileEntity _tile) {
		super.setPartHostInfo(_side, _host, _tile);
		onInventoryChanged();
	}

	@MENetworkEventSubscribe
	public void updateChannels(MENetworkChannelsChanged channel) {
		IGridNode node = getGridNode();
		if (node != null) {
			boolean isNowActive = node.isActive();
			if (isNowActive != isActive()) {
				setActive(isNowActive);
				onNeighborChanged();
				getHost().markForUpdate();
			}
		}
		node.getGrid().postEvent(new MENetworkCellArrayUpdate());
	}

	private List<IMEInventoryHandler> updateHandlers(StorageChannel channel) {
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		List<IMEInventoryHandler> handlers = new ArrayList<IMEInventoryHandler>();
		for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
			ItemStack cell = this.inventory.getStackInSlot(i);
			if (cellRegistry.isCellHandled(cell)) {
				IMEInventoryHandler cellInventory = cellRegistry
						.getCellInventory(cell, null, channel);
				if (cellInventory != null)
					handlers.add(cellInventory);
			}
		}
		return handlers;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("inventory", this.inventory.writeToNBT());
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		for (byte aCellStati : this.cellStatuses) {
			data.writeByte(aCellStati);
		}
	}
}

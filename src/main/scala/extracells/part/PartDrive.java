package extracells.part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

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
import appeng.api.parts.IPartModel;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import extracells.container.ContainerDrive;
import extracells.gui.GuiDrive;
import extracells.inventory.IInventoryListener;
import extracells.inventory.InventoryPartDrive;
import extracells.inventory.InventoryPlain;
import extracells.models.PartModels;
import extracells.models.drive.DriveSlotsState;
import extracells.models.drive.IECDrive;
import extracells.util.AEUtils;
import extracells.util.PermissionUtil;
import io.netty.buffer.ByteBuf;

public class PartDrive extends PartECBase implements ICellContainer, IInventoryListener, IECDrive {

	public static DriveSlotsState tempDriveState;

	private final byte[] cellStatuses = new byte[6];
	private final InventoryPartDrive inventory = new InventoryPartDrive(this);
	private int priority = 0; // TODO
	private short[] blinkTimers; // TODO
	private List<IMEInventoryHandler> fluidHandlers = new ArrayList<IMEInventoryHandler>();
	private List<IMEInventoryHandler> itemHandlers = new ArrayList<IMEInventoryHandler>();

	@Override
	public void addToWorld() {
		super.addToWorld();
		onInventoryChanged();
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		if (!wrenched) {
			for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
				ItemStack cell = this.inventory.getStackInSlot(i);
				if (cell != null) {
					drops.add(cell);
				}
			}
		}
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiDrive(this, player);
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerDrive(this, player);
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
			(IPart) this)) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	/* NETWORK */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.inventory.readFromNBT(data.getTagList("inventory", 10));
		onInventoryChanged();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("inventory", this.inventory.writeToNBT());
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		for (int i = 0; i < this.cellStatuses.length; i++) {
			this.cellStatuses[i] = data.readByte();
		}
		return true;
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		for (byte aCellStati : this.cellStatuses) {
			data.writeByte(aCellStati);
		}
	}

	@Override
	public void setPartHostInfo(AEPartLocation location, IPartHost iPartHost, TileEntity tileEntity) {
		super.setPartHostInfo(location, iPartHost, tileEntity);
		onInventoryChanged();
	}

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 2.0F;
	}

	@Override
	public IPartModel getStaticModels() {
		tempDriveState = DriveSlotsState.createState(this);
		if (isActive() && isPowered()) {
			return PartModels.DRIVE_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.DRIVE_ON;
		} else {
			return PartModels.DRIVE_OFF;
		}
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	public InventoryPlain getInventory() {
		return this.inventory;
	}

	@Override
	public void onInventoryChanged() {
		this.itemHandlers = updateHandlers(AEUtils.getItemChannel());
		this.fluidHandlers = updateHandlers(AEUtils.getFluidChannel());
		for (int i = 0; i < this.cellStatuses.length; i++) {
			ItemStack stackInSlot = this.inventory.getStackInSlot(i);
			IMEInventoryHandler inventoryHandler = AEUtils.cell().getCellInventory(stackInSlot, null, AEUtils.getItemChannel());
			if (inventoryHandler == null) {
				inventoryHandler = AEUtils.cell().getCellInventory(stackInSlot, null, AEUtils.getFluidChannel());
			}

			ICellHandler cellHandler = AEUtils.cell().getHandler(stackInSlot);
			if (cellHandler == null || inventoryHandler == null) {
				this.cellStatuses[i] = 0;
			} else {
				this.cellStatuses[i] = (byte) cellHandler.getStatusForCell(stackInSlot, inventoryHandler);
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

	@Override
	public void saveChanges(IMEInventory cellInventory) {
		getHost().markForSave();
	}

	private List<IMEInventoryHandler> updateHandlers(StorageChannel channel) {
		List<IMEInventoryHandler> handlers = new ArrayList<IMEInventoryHandler>();
		for (int i = 0; i < this.inventory.getSizeInventory(); i++) {
			ItemStack cell = this.inventory.getStackInSlot(i);
			if (AEUtils.cell().isCellHandled(cell)) {
				IMEInventoryHandler cellInventory = AEUtils.cell().getCellInventory(cell, null, channel);
				if (cellInventory != null) {
					handlers.add(cellInventory);
				}
			}
		}
		return handlers;
	}

	/* CELLS */
	@Override
	public int getCellCount() {
		return 6;
	}

	@Override
	public int getCellStatus(int index) {
		return cellStatuses[index];
	}

	@Override
	public void blinkCell(int slot) {
		if (slot > 0 && slot < this.blinkTimers.length) {
			this.blinkTimers[slot] = 15;
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
		if (!isActive()) {
			return new ArrayList();
		}
		return AEUtils.isItemChannel(channel) ? this.itemHandlers : this.fluidHandlers;
	}

	/* EVENT HANDLERS*/
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
}

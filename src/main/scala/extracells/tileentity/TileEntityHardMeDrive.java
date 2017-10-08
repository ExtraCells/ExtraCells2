package extracells.tileentity;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.container.ContainerHardMEDrive;
import extracells.gridblock.ECGridBlockHardMEDrive;
import extracells.gui.GuiHardMEDrive;
import extracells.inventory.ECPrivateInventory;
import extracells.inventory.IInventoryListener;
import extracells.models.drive.IDrive;
import extracells.network.IGuiProvider;

public class TileEntityHardMeDrive extends TileBase implements IActionHost, IECTileEntity, ICellContainer, IInventoryListener, IDrive, IGuiProvider {

	private int priority = 0;
	private boolean isPowerd;

    boolean isFirstGridNode = true;
    byte[] cellStatuses = new byte[3];
    List<IMEInventoryHandler> fluidHandlers = new ArrayList<IMEInventoryHandler>();
    List<IMEInventoryHandler> itemHandlers = new ArrayList<IMEInventoryHandler>();
    private final ECGridBlockHardMEDrive gridBlock = new ECGridBlockHardMEDrive(this);

	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.drive", 3, 1, this) {

        ICellRegistry cellRegistry = AEApi.instance().registries().cell();

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return this.cellRegistry.isCellHandled(itemStack);
        }
    };

    public IInventory getInventory(){
        return inventory;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.pos) == this && player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    IGridNode node = null;


    @Override
    public void blinkCell(int i) {

    }

    @Override
    public IGridNode getActionableNode() {
        return getGridNode(AEPartLocation.INTERNAL);
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (!isActive())
            return new ArrayList<IMEInventoryHandler>();
        return channel == StorageChannel.ITEMS ? this.itemHandlers
                : this.fluidHandlers;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public double getPowerUsage() {
        return 0;
    }

    @Override
    public IGridNode getGridNode(AEPartLocation location) {
        if (isFirstGridNode && hasWorldObj() && !worldObj.isRemote){
            isFirstGridNode = false;
            try{
                node = AEApi.instance().createGridNode(gridBlock);
                node.updateState();
            }catch (Exception e){
                isFirstGridNode = true;
            }
        }

        return node;
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation location) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public void saveChanges(IMEInventory imeInventory) {

    }

    //TODO
    boolean isActive(){
		return true;
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
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node != null) {
            IGrid grid = node.getGrid();
            if (grid != null) {
                grid.postEvent(new MENetworkCellArrayUpdate());
            }
            updateBlock();
        }
		if (worldObj.isRemote) {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
		}
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
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        inventory.readFromNBT(tag.getTagList("inventory", 10));
        onInventoryChanged();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setTag("inventory", inventory.writeToNBT());
        return tag;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = writeToNBT(new NBTTagCompound());
        int i = 0;
        for (byte aCellStati : this.cellStatuses) {
            tag.setByte("status#" + i, aCellStati);
            i++;
        }
		tag.setBoolean("isPowerd", isPowerd);
		return tag;
	}

	@Override
	public int getCellCount() {
		return 3;
	}

	@Override
	public int getCellStatus(int index) {
		return cellStatuses[index];
	}

	@Override
	public boolean isPowered() {
		return isPowerd;
	}

	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiHardMEDrive(player.inventory, this);
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerHardMEDrive(player.inventory, this);
	}

	@MENetworkEventSubscribe
	@SuppressWarnings("unused")
	public void setPower(MENetworkPowerStatusChange notUsed) {
		if (this.node != null) {
			IGrid grid = this.node.getGrid();
			if (grid != null) {
				IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
				if (energy != null) {
					this.isPowerd = energy.isNetworkPowered();
				}
			}
			updateBlock();
		}
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		isPowerd = tag.getBoolean("isPowerd");
	}
}

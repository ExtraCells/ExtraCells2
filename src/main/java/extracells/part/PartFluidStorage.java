package extracells.part;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.GuiBusFluidStorage;
import extracells.inventory.HandlerPartStorageFluid;
import extracells.network.packet.other.IFluidSlotPart;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.render.TextureManager;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartFluidStorage extends PartECBase implements ICellContainer, IInventoryUpdateReceiver, IFluidSlotPart {

    private int priority = 0;
    private HandlerPartStorageFluid handler = new HandlerPartStorageFluid(this);
    private Fluid[] filterFluids = new Fluid[54];
    private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 1, 1, this) {

        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            if (itemStack == null)
                return false;
            if (AEApi.instance().materials().materialCardInverter.sameAsStack(itemStack))
                return true;
            return false;
        }
    };

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.STORAGE_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.STORAGE_FRONT.getTextures()[0], side, side);
        rh.setBounds(2, 2, 15, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.setBounds(4, 4, 14, 12, 12, 15);
        rh.renderInventoryBox(renderer);
        rh.setBounds(2, 2, 15, 14, 14, 16);
        rh.setInvColor(AEColor.Cyan.blackVariant);
        ts.setBrightness(15 << 20 | 15 << 4);
        rh.renderInventoryFace(TextureManager.STORAGE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderInventoryBusLights(rh, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.STORAGE_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.STORAGE_FRONT.getTexture(), side, side);
        rh.setBounds(2, 2, 15, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        ts.setColorOpaque_I(getHost().getColor().blackVariant);
        if (isActive())
            ts.setBrightness(15 << 20 | 15 << 4);
        rh.renderFace(x, y, z, TextureManager.STORAGE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.setBounds(4, 4, 14, 12, 12, 15);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderStaticBusLights(x, y, z, rh, renderer);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("priority", priority);
        for (int i = 0; i < filterFluids.length; i++) {
            Fluid fluid = filterFluids[i];
            if (fluid != null)
                data.setString("FilterFluid#" + i, fluid.getName());
            else
                data.setString("FilterFluid#" + i, "");
        }
        data.setTag("upgradeInventory", upgradeInventory.writeToNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        priority = data.getInteger("priority");
        for (int i = 0; i < 9; i++) {
            filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
        }
        upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));
        onInventoryChanged();
        onNeighborChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 15, 14, 14, 16);
        bch.addBox(4, 4, 14, 12, 12, 15);
        bch.addBox(5, 5, 13, 11, 11, 14);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 3;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
        if (channel == StorageChannel.FLUIDS) {
            list.add(handler);
        }
        return list;
    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void blinkCell(int slot) {
    }

    @Override
    public void onNeighborChanged() {
        handler.onNeighborChange();
        IGridNode node = getGridNode();
        if (node != null) {
            IGrid grid = node.getGrid();
            if (grid != null) {
                grid.postEvent(new MENetworkCellArrayUpdate());
                node.getGrid().postEvent(new MENetworkStorageEvent(getGridBlock().getFluidMonitor(), StorageChannel.FLUIDS));
            }
            getHost().markForUpdate();
        }
    }

    public ECPrivateInventory getUpgradeInventory() {
        return upgradeInventory;
    }

    @Override
    public void onInventoryChanged() {
        handler.setInverted(AEApi.instance().materials().materialCardInverter.sameAsStack(upgradeInventory.getStackInSlot(0)));
        saveData();
    }

    public void sendInformation(EntityPlayer player) {
        new PacketFluidSlot(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerBusFluidStorage(this, player);
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiBusFluidStorage(this, player);
    }

    @Override
    public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
        filterFluids[_index] = _fluid;
        handler.setPrioritizedFluids(filterFluids);
        sendInformation(_player);
        saveData();
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
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {
        saveData();
    }
}

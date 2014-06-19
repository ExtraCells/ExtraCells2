package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.*;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.gridblock.ECBaseGridBlock;
import extracells.network.GuiHandler;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.render.TextureManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public abstract class PartECBase implements IPart, IGridHost, IActionHost {

    protected IGridNode node;
    protected ForgeDirection side;
    protected IPartHost host;
    protected TileEntity tile;
    protected ECBaseGridBlock gridBlock;
    protected double powerUsage;
    protected TileEntity hostTile;
    protected IFluidHandler facingTank;
    protected boolean redstonePowered;
    protected boolean isActive;

    public void initializePart(ItemStack partStack) {
        if (partStack.hasTagCompound()) {
            readFromNBT(partStack.getTagCompound());
        }
    }

    @MENetworkEventSubscribe
    @SuppressWarnings("unused")
    public void setPower(MENetworkPowerStatusChange notUsed) {
        if (node != null) {
            isActive = node.isActive();
            host.markForUpdate();
        }
    }

    @Override
    public IIcon getBreakingTexture() {
        return TextureManager.BUS_SIDE.getTexture();
    }

    protected boolean isActive() {
        return node != null ? node.isActive() : isActive;
    }

    @Override
    public ItemStack getItemStack(PartItemStack type) {
        ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1, PartEnum.getPartID(this));
        if (type != PartItemStack.Break) {
            NBTTagCompound itemNbt = new NBTTagCompound();
            writeToNBT(itemNbt);
            is.setTagCompound(itemNbt);
        }
        return is;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

    @Override
    @SideOnly(Side.CLIENT)
    public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer) {
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
    }

    @Override
    public int getLightLevel() {
        return isActive() ? 15 : 0;
    }

    @Override
    public boolean isLadder(EntityLivingBase entity) {
        return false;
    }

    @Override
    public void addToWorld() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;
        gridBlock = new ECBaseGridBlock(this);
        node = AEApi.instance().createGridNode(gridBlock);
        setPower(null);
        onNeighborChanged();
    }

    @Override
    public void onNeighborChanged() {
        if (hostTile == null)
            return;
        World world = hostTile.getWorldObj();
        int x = hostTile.xCoord;
        int y = hostTile.yCoord;
        int z = hostTile.zCoord;
        TileEntity tileEntity = world.getTileEntity(x + side.offsetX, y + side.offsetY, z + side.offsetZ);
        facingTank = null;
        if (tileEntity instanceof IFluidHandler)
            facingTank = (IFluidHandler) tileEntity;
        redstonePowered = world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);
    }

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public IGridNode getGridNode() {
        return node;
    }

    @Override
    public void onEntityCollision(Entity entity) {
    }

    @Override
    public void removeFromWorld() {
        if (node != null)
            node.destroy();
    }

    @Override
    public final IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(ForgeDirection _side, IPartHost _host, TileEntity _tile) {
        side = _side;
        host = _host;
        tile = _tile;
        hostTile = _tile;
        setPower(null);
    }

    @Override
    public abstract void getBoxes(IPartCollsionHelper bch);

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        if (player != null && player instanceof EntityPlayerMP)
            GuiHandler.launchGui(GuiHandler.getGuiId(this), player, hostTile.getWorldObj(), hostTile.xCoord, hostTile.yCoord, hostTile.zCoord);
        return true;
    }

    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        data.writeBoolean(node != null && node.isActive());
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        isActive = data.readBoolean();
        return true;
    }

    @Override
    public boolean onShiftActivate(EntityPlayer player, Vec3 pos) {
        return false;
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
    }

    @Override
    public abstract int cableConnectionRenderTo();

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random r) {
    }

    @Override
    public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side) {
    }

    @Override
    public boolean canBePlacedOn(BusSupport what) {
        return what != BusSupport.DENSE_CABLE;
    }

    @Override
    public final IGridNode getGridNode(ForgeDirection dir) {
        return node;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
    }

    public IPartHost getHost() {
        return host;
    }

    public ForgeDirection getSide() {
        return side;
    }

    public double getPowerUsage() {
        return powerUsage;
    }

    public ECBaseGridBlock getGridBlock() {
        return gridBlock;
    }

    @Override
    public final IGridNode getActionableNode() {
        return node;
    }

    protected final IAEFluidStack injectFluid(IAEFluidStack toInject, Actionable action) {
        if (gridBlock == null || facingTank == null)
            return null;
        IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
        if (monitor == null)
            return null;
        return monitor.injectItems(toInject, action, new MachineSource(this));
    }

    protected final IAEFluidStack extractFluid(IAEFluidStack toExtract, Actionable action) {
        if (gridBlock == null || facingTank == null)
            return null;
        IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
        if (monitor == null)
            return null;
        return monitor.extractItems(toExtract, action, new MachineSource(this));
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return null;
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void renderStaticBusLights(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon otherIcon = TextureManager.BUS_COLOR.getTextures()[0];
        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon);
        rh.renderBlock(x, y, z, renderer);

        if (isActive()) {
            ts.setBrightness(13 << 20 | 13 << 4);
            ts.setColorOpaque_I(host.getColor().blackVariant);
        } else {
            ts.setColorOpaque_I(0x000000);
        }
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.UP, renderer);
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.DOWN, renderer);
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.NORTH, renderer);
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.EAST, renderer);
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.WEST, renderer);
    }

    @SideOnly(Side.CLIENT)
    public void renderInventoryBusLights(IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        rh.setInvColor(0xFFFFFF);
        IIcon otherIcon = TextureManager.BUS_COLOR.getTextures()[0];
        IIcon side = TextureManager.BUS_SIDE.getTexture();
        rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon);
        rh.renderInventoryBox(renderer);

        ts.setBrightness(13 << 20 | 13 << 4);
        rh.setInvColor(AEColor.Transparent.blackVariant);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.UP, renderer);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.DOWN, renderer);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.NORTH, renderer);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.EAST, renderer);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.renderInventoryFace(TextureManager.BUS_COLOR.getTextures()[1], ForgeDirection.WEST, renderer);
    }

    public final DimensionalCoord getLocation() {
        return new DimensionalCoord(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
    }
}

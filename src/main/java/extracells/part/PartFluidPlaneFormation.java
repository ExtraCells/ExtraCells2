package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerPlaneFormation;
import extracells.gui.GuiFluidPlaneFormation;
import extracells.network.packet.other.IFluidSlotPart;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.render.TextureManager;
import extracells.util.ColorUtil;
import extracells.util.FluidUtil;
import extracells.util.inventory.ECPrivateInventory;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class PartFluidPlaneFormation extends PartECBase implements IFluidSlotPart {

    private Fluid fluid;
    //TODO redstone control
    private RedstoneMode redstoneMode;
    private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 1, 1) {

        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return AEApi.instance().materials().materialCardRedstone.sameAsStack(itemStack);
        }
    };

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        IIcon side = TextureManager.PANE_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);
        rh.setBounds(3, 3, 14, 13, 13, 16);
        rh.setInvColor(AEColor.Cyan.blackVariant);
        rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
        Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
        rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.mediumVariant));
        rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.whiteVariant));
        rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderInventoryBusLights(rh, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;
        IIcon side = TextureManager.PANE_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);
        rh.setBounds(3, 3, 14, 13, 13, 16);
        if (host != null) {
            ts.setColorOpaque_I(host.getColor().blackVariant);
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
            if (isActive())
                ts.setBrightness(13 << 20 | 13 << 4);
            ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.getColor().mediumVariant));
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
            ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.getColor().whiteVariant));
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);
        }

        rh.setBounds(5, 5, 13, 11, 11, 14);
        renderStaticBusLights(x, y, z, rh, renderer);
    }

    @Override
    public void getBoxes(IPartCollsionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 13, 11, 11, 14);
    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    public ECPrivateInventory getUpgradeInventory() {
        return upgradeInventory;
    }

    @Override
    public void onNeighborChanged() {
        if (fluid == null || hostTile == null || gridBlock == null)
            return;
        IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
        if (monitor == null)
            return;
        World world = hostTile.getWorldObj();
        int x = hostTile.xCoord;
        int y = hostTile.yCoord;
        int z = hostTile.zCoord;
        Block worldBlock = world.getBlock(x + side.offsetX, y + side.offsetY, z + side.offsetZ);
        if (worldBlock != null)
            return;
        IAEFluidStack canDrain = monitor.extractItems(FluidUtil.createAEFluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), Actionable.SIMULATE, new MachineSource(this));
        if (canDrain == null || canDrain.getStackSize() < FluidContainerRegistry.BUCKET_VOLUME)
            return;
        monitor.extractItems(FluidUtil.createAEFluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), Actionable.MODULATE, new MachineSource(this));
        Block fluidWorldBlock = fluid.getBlock();
        world.setBlock(x, y, z, fluidWorldBlock);

    }

    @Override
    public int cableConnectionRenderTo() {
        return 2;
    }

    @Override
    public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
        fluid = _fluid;
        new PacketFluidSlot(Lists.newArrayList(fluid)).sendPacketToPlayer(_player);
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerPlaneFormation(this, player);
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiFluidPlaneFormation(this, player);
    }

    public void sendInformation(EntityPlayer _player) {
        new PacketFluidSlot(Lists.newArrayList(fluid)).sendPacketToPlayer(_player);
    }
}

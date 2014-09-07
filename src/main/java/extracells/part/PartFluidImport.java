package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class PartFluidImport extends PartFluidIO implements IFluidHandler {

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.IMPORT_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.IMPORT_FRONT.getTexture(), side, side);
        rh.setBounds(4, 4, 14, 12, 12, 16);
        rh.renderInventoryBox(renderer);

        rh.setTexture(side);
        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderInventoryBox(renderer);
        rh.setBounds(6, 6, 12, 10, 10, 13);
        rh.renderInventoryBox(renderer);

        rh.setBounds(4, 4, 14, 12, 12, 16);
        rh.setInvColor(AEColor.Cyan.blackVariant);
        ts.setBrightness(15 << 20 | 15 << 4);
        rh.renderInventoryFace(TextureManager.IMPORT_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);


        rh.setBounds(6, 6, 11, 10, 10, 12);
        renderInventoryBusLights(rh, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.IMPORT_SIDE.getTexture();
        rh.setTexture(side, side, side, TextureManager.IMPORT_FRONT.getTextures()[0], side, side);
        rh.setBounds(4, 4, 14, 12, 12, 16);
        rh.renderBlock(x, y, z, renderer);

        ts.setColorOpaque_I(getHost().getColor().blackVariant);
        if (isActive())
            ts.setBrightness(15 << 20 | 15 << 4);
        rh.renderFace(x, y, z, TextureManager.IMPORT_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);

        rh.setTexture(side);
        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderBlock(x, y, z, renderer);
        rh.setBounds(6, 6, 12, 10, 10, 13);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(6, 6, 11, 10, 10, 12);
        renderStaticBusLights(x, y, z, rh, renderer);
    }

    @Override
    public void getBoxes(IPartCollsionHelper bch) {
        bch.addBox(4, 4, 14, 12, 12, 16);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(6, 6, 12, 10, 10, 13);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 5;
    }

    public boolean doWork(int rate, int TicksSinceLastCall) {
        if (getFacingTank() == null)
            return false;
        boolean empty = true;

        List<Fluid> filter = new ArrayList<Fluid>();
        filter.add(filterFluids[4]);

        if (filterSize >= 1) {
            for (byte i = 1; i < 9; i += 2) {
                if (i != 4) {
                    filter.add(filterFluids[i]);
                }
            }
        }

        if (filterSize >= 2) {
            for (byte i = 0; i < 9; i += 2) {
                if (i != 4) {
                    filter.add(filterFluids[i]);
                }
            }
        }

        for (Fluid fluid : filter) {
            if (fluid != null) {
                empty = false;

                if (fillToNetwork(fluid, rate * TicksSinceLastCall)) {
                    return true;
                }
            }
        }
        if (empty)
            return fillToNetwork(null, rate * TicksSinceLastCall);
        return false;
    }

    private boolean fillToNetwork(Fluid fluid, int toDrain) {
        FluidStack drained;
        IFluidHandler facingTank = getFacingTank();
        ForgeDirection side = getSide();
        if (fluid == null) {
            drained = facingTank.drain(side.getOpposite(), toDrain, false);
        } else {
            drained = facingTank.drain(side.getOpposite(), new FluidStack(fluid, toDrain), false);
        }

        if (drained == null || drained.amount <= 0 || drained.fluidID <= 0)
            return false;

        IAEFluidStack toFill = AEApi.instance().storage().createFluidStack(drained);
        IAEFluidStack notInjected = injectFluid(toFill, Actionable.MODULATE);

        if (notInjected != null) {
            int amount = (int) (toFill.getStackSize() - notInjected.getStackSize());
            if (amount > 0) {
                if (fluid == null)
                    facingTank.drain(side.getOpposite(), amount, true);
                else
                    facingTank.drain(side.getOpposite(), new FluidStack(toFill.getFluid(), amount), true);
                return true;
            } else {
                return false;
            }
        } else {
            if (fluid == null)
                facingTank.drain(side.getOpposite(), toFill.getFluidStack().amount, true);
            else
                facingTank.drain(side.getOpposite(), toFill.getFluidStack(), true);
            return true;
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        boolean redstonePowered = isRedstonePowered();
        if (resource == null || (redstonePowered && getRedstoneMode() == RedstoneMode.LOW_SIGNAL || !redstonePowered && getRedstoneMode() == RedstoneMode.HIGH_SIGNAL))
            return 0;

        FluidStack toFill = new FluidStack(resource.fluidID, 125 + speedState * 125);
        IAEFluidStack filled = injectFluid(AEApi.instance().storage().createFluidStack(toFill), Actionable.MODULATE);

        if (filled == null)
            return 125 + speedState * 125;
        return toFill.amount - (int) filled.getStackSize();
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[0];
    }
}

package extracells.tileentity;

import extracells.network.ChannelHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileEntityCertusTank extends TileEntity implements IFluidHandler {

    private FluidStack lastBeforeUpdate = null;
    public FluidTank tank = new FluidTank(32000) {

        public FluidTank readFromNBT(NBTTagCompound nbt) {
            if (!nbt.hasKey("Empty")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
                setFluid(fluid);
            } else {
                setFluid(null);
            }
            return this;
        }
    };

    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        readFromNBT(packet.func_148857_g());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readFromNBTWithoutCoords(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeToNBTWithoutCoords(tag);
    }

    public void writeToNBTWithoutCoords(NBTTagCompound tag) {
        tank.writeToNBT(tag);
    }

    public void readFromNBTWithoutCoords(NBTTagCompound tag) {
        tank.readFromNBT(tag);
    }

    /* IFluidHandler */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || (tank.getFluid() != null && resource.fluidID != tank.getFluid().fluidID))
            return 0;
        return fill(resource, doFill, true);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (tank.getFluid() == null || resource == null || resource.fluidID != tank.getFluid().fluidID)
            return null;

        return drain(resource, doDrain, true);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (tank.getFluid() == null)
            return null;

        return drain(from, new FluidStack(tank.getFluid(), maxDrain), doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return tank.getFluid() == null || tank.getFluid().getFluid() == fluid;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return tank.getFluid() == null || tank.getFluid().getFluid() == fluid;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return getTankInfo(true);
    }

    /* Multiblock stuff */
    public FluidStack drain(FluidStack fluid, boolean doDrain, boolean findMainTank) {
        if (findMainTank) {
            int yOff = 0;
            TileEntity offTE = worldObj.getTileEntity(xCoord, yCoord + yOff, zCoord);
            TileEntityCertusTank mainTank = this;
            while (true) {
                if (offTE != null && offTE instanceof TileEntityCertusTank) {
                    Fluid offFluid = ((TileEntityCertusTank) offTE).getFluid();
                    if (offFluid != null && offFluid == fluid.getFluid()) {
                        mainTank = (TileEntityCertusTank) worldObj.getTileEntity(xCoord, yCoord + yOff, zCoord);
                        yOff++;
                        offTE = worldObj.getTileEntity(xCoord, yCoord + yOff, zCoord);
                        continue;
                    }
                }
                break;
            }

            return mainTank != null ? mainTank.drain(fluid, doDrain, false) : null;
        }

        FluidStack drained = tank.drain(fluid.amount, doDrain);
        compareAndUpdate();

        if (drained == null || drained.amount < fluid.amount) {
            TileEntity offTE = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
            if (offTE instanceof TileEntityCertusTank) {
                TileEntityCertusTank tank = (TileEntityCertusTank) offTE;
                FluidStack externallyDrained = tank.drain(new FluidStack(fluid.fluidID, fluid.amount - (drained != null ? drained.amount : 0)), doDrain, false);

                if (externallyDrained != null)
                    return new FluidStack(fluid.fluidID, (drained != null ? drained.amount : 0) + externallyDrained.amount);
                else
                    return drained;
            }
        }

        return drained;
    }

    public int fill(FluidStack fluid, boolean doFill, boolean findMainTank) {
        if (findMainTank) {
            int yOff = 0;
            TileEntity offTE = worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
            TileEntityCertusTank mainTank = this;
            while (true) {
                if (offTE != null && offTE instanceof TileEntityCertusTank) {
                    Fluid offFluid = ((TileEntityCertusTank) offTE).getFluid();
                    if (offFluid == null || offFluid == fluid.getFluid()) {
                        mainTank = (TileEntityCertusTank) worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
                        yOff++;
                        offTE = worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
                        continue;
                    }
                }
                break;
            }

            return mainTank != null ? mainTank.fill(fluid, doFill, false) : 0;
        }

        int filled = tank.fill(fluid, doFill);
        compareAndUpdate();

        if (filled < fluid.amount) {
            TileEntity offTE = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
            if (offTE instanceof TileEntityCertusTank) {
                TileEntityCertusTank tank = (TileEntityCertusTank) offTE;
                return filled + tank.fill(new FluidStack(fluid.fluidID, fluid.amount - filled), doFill, false);
            }
        }

        return filled;
    }

    public FluidTankInfo[] getTankInfo(boolean goToMainTank) {
        if (!goToMainTank)
            return new FluidTankInfo[]{tank.getInfo()};

        int amount = 0, capacity = 0;
        Fluid fluid = null;

        int yOff = 0;
        TileEntity offTE = worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
        TileEntityCertusTank mainTank = this;
        while (true) {
            if (offTE != null && offTE instanceof TileEntityCertusTank) {
                if ((((TileEntityCertusTank) offTE).getFluid() == null || ((TileEntityCertusTank) offTE).getFluid() == getFluid())) {
                    mainTank = (TileEntityCertusTank) worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
                    yOff++;
                    offTE = worldObj.getTileEntity(xCoord, yCoord - yOff, zCoord);
                    continue;
                }
            }
            break;
        }

        yOff = 0;
        offTE = worldObj.getTileEntity(xCoord, yCoord + yOff, zCoord);
        while (true) {
            if (offTE != null && offTE instanceof TileEntityCertusTank) {
                mainTank = (TileEntityCertusTank) offTE;
                if ((mainTank.getFluid() == null || mainTank.getFluid() == getFluid())) {
                    FluidTankInfo info = mainTank.getTankInfo(false)[0];
                    if (info != null) {
                        capacity += info.capacity;
                        if (info.fluid != null) {
                            amount += info.fluid.amount;
                            if (info.fluid.getFluid() != null)
                                fluid = info.fluid.getFluid();
                        }
                    }
                    yOff++;
                    offTE = worldObj.getTileEntity(xCoord, yCoord + yOff, zCoord);
                    continue;
                }
            }
            break;
        }

        return new FluidTankInfo[]{new FluidTankInfo(fluid != null ? new FluidStack(fluid, amount) : null, capacity)};
    }

    public Fluid getFluid() {
        FluidStack tankFluid = tank.getFluid();
        return tankFluid != null && tankFluid.amount > 0 ? tankFluid.getFluid() : null;
    }

    public void compareAndUpdate() {
        if (!worldObj.isRemote) {
            FluidStack current = tank.getFluid();

            if (current != null) {
                if (lastBeforeUpdate != null) {
                    if (Math.abs(current.amount - lastBeforeUpdate.amount) >= 500) {
                        ChannelHandler.sendPacketToAllPlayers(getDescriptionPacket(), worldObj);
                        lastBeforeUpdate = current.copy();
                    } else if (lastBeforeUpdate.amount < tank.getCapacity() && current.amount == tank.getCapacity() || lastBeforeUpdate.amount == tank.getCapacity() && current.amount < tank.getCapacity()) {
                        ChannelHandler.sendPacketToAllPlayers(getDescriptionPacket(), worldObj);
                        lastBeforeUpdate = current.copy();
                    }
                } else {
                    ChannelHandler.sendPacketToAllPlayers(getDescriptionPacket(), worldObj);
                    lastBeforeUpdate = current.copy();
                }
            } else if (lastBeforeUpdate != null) {
                ChannelHandler.sendPacketToAllPlayers(getDescriptionPacket(), worldObj);
                lastBeforeUpdate = null;
            }
        }
    }

    public Fluid getRenderFluid() {
        return tank.getFluid() != null ? tank.getFluid().getFluid() : null;
    }

    public float getRenderScale() {
        return (float) tank.getFluidAmount() / tank.getCapacity();
    }
}

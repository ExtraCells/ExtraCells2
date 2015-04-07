package extracells.tileentity;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.gridblock.ECFluidGridBlock;
import extracells.util.FuelBurnTime;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileEntityVibrationChamberFluid extends TileEntity implements IECTileEntity, IFluidHandler, IActionHost {

    boolean isFirstGridNode = true;
    private final ECFluidGridBlock gridBlock = new ECFluidGridBlock(this);
    IGridNode node = null;
    private int burnTime, burnTimeTotal;
    private int timer, timerEnergy;
    private double energyLeft;

    public FluidTank tank = new FluidTank(16000) {

        @Override
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

    @Override
    public void updateEntity() {
        super.updateEntity();

        if(!hasWorldObj())
            return;
        FluidStack fluidStack1 = tank.getFluid();
        if(fluidStack1 != null)
            fluidStack1 = fluidStack1.copy();

        if(worldObj.isRemote)return;
        if(burnTime == burnTimeTotal) {

            if (timer >= 40) {
                worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                FluidStack fluidStack = tank.getFluid();
                int bTime;
                if(fluidStack != null)
                    bTime = FuelBurnTime.getBurnTime(fluidStack.getFluid());
                else
                    bTime = 0;
                System.out.println(bTime +  " " + energyLeft);
                if (fluidStack != null && bTime  > 0) {
                    if (tank.getFluid().amount >= 250) {
                        if(energyLeft <= 0) {
                            burnTime = 0;
                            burnTimeTotal = bTime / 4;
                            tank.drain(250, true);
                        }
                    }
                }
                timer = 0;
            } else {
                timer++;
            }
        }
        else
        {
            burnTime++;
            if(timerEnergy == 4)
            {
                if(energyLeft == 0) {
                    IEnergyGrid energy = getGridNode(ForgeDirection.UNKNOWN).getGrid().getCache(IEnergyGrid.class);
                    energyLeft = energy.injectPower(24.0D, Actionable.MODULATE);
                }
                else
                {
                    IEnergyGrid energy = getGridNode(ForgeDirection.UNKNOWN).getGrid().getCache(IEnergyGrid.class);
                    energyLeft = energy.injectPower(energyLeft, Actionable.MODULATE);
                }
                timerEnergy = 0;
            }
            else
            {
                timerEnergy++;
            }
        }

        if(fluidStack1 == null  && tank.getFluid() == null)
            return;
        if(fluidStack1 == null || tank.getFluid() == null){
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return;
        }
        if(!fluidStack1.equals(tank.getFluid())){
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return;
        }
        if(fluidStack1.amount != tank.getFluid().amount){
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return;
        }

    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public double getPowerUsage(){return 0;}

    @Override
    public IGridNode getGridNode(ForgeDirection forgeDirection) {
        if (isFirstGridNode && hasWorldObj() && !getWorldObj().isRemote){
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
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if(resource == null || resource.getFluid() == null || FuelBurnTime.getBurnTime(resource.getFluid()) == 0)
            return  0;
        int filled = tank.fill(resource, doFill);
        if(filled != 0 && hasWorldObj())
            getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
        return filled;
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
        if(fluid == null || FuelBurnTime.getBurnTime(fluid) == 0)
            return false;
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] {tank.getInfo()};
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("BurnTime", this.burnTime);
        nbt.setInteger("BurnTimeTotal", this.burnTimeTotal);
        nbt.setInteger("timer", this.timer);
        nbt.setInteger("timerEnergy", this.timerEnergy);
        nbt.setDouble("energyLeft", this.energyLeft);

        tank.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if(nbt.hasKey("BurnTime"))
            this.burnTime = nbt.getInteger("BurnTime");
        if(nbt.hasKey("BurnTimeTotal"))
            this.burnTimeTotal = nbt.getInteger("BurnTimeTotal");
        if(nbt.hasKey("timer"))
            this.timer = nbt.getInteger("timer");
        if(nbt.hasKey("timerEnergy"))
            this.timerEnergy = nbt.getInteger("timerEnergy");
        if(nbt.hasKey("energyLeft"))
            this.energyLeft = nbt.getDouble("energyLeft");
//for(IAEFluidStack stack : getFluidInventoryNetwork().getAvailableItems(AEApi.instance().storage().createFluidList())){
//
//        }
        tank.readFromNBT(nbt);
    }

    public IMEInventory<IAEFluidStack> getFluidInventoryNetwork(){
        IGridNode n = getGridNode(ForgeDirection.UNKNOWN);
        if (n == null)
            return null;
        IGrid grid = n.getGrid();
        if (grid == null)
            return null;
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if(storageGrid == null)
            return null;
        return storageGrid.getFluidInventory();
    }

    public int getBurntTimeScaled(int scal)
    {
        return burnTime != 0 ? burnTime *  scal / burnTimeTotal : 0;
    }

    @Override
    public IGridNode getActionableNode() {
        return getGridNode(ForgeDirection.UNKNOWN);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord,
                this.zCoord, this.getBlockMetadata(), nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getBurnTimeTotal() {
        return burnTimeTotal;
    }
}


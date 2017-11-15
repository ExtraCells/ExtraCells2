package extracells.tileentity;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityCertusTank extends TileBase {

	private FluidStack lastBeforeUpdate = null;
	public FluidTank tank = new FluidTank(32000) {

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
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandler());
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	public void compareAndUpdate() {
		if (!this.world.isRemote) {
			FluidStack current = this.tank.getFluid();

			if (current != null) {
				if (this.lastBeforeUpdate != null) {
					if (Math.abs(current.amount - this.lastBeforeUpdate.amount) >= 500) {
						updateBlock();
						this.lastBeforeUpdate = current.copy();
					} else if (this.lastBeforeUpdate.amount < this.tank
						.getCapacity()
						&& current.amount == this.tank.getCapacity()
						|| this.lastBeforeUpdate.amount == this.tank
						.getCapacity()
						&& current.amount < this.tank.getCapacity()) {
						updateBlock();
						this.lastBeforeUpdate = current.copy();
					}
				} else {
					updateBlock();
					this.lastBeforeUpdate = current.copy();
				}
			} else if (this.lastBeforeUpdate != null) {
				updateBlock();
				this.lastBeforeUpdate = null;
			}
		}
	}

	/* Multiblock stuff */
	public FluidStack drain(FluidStack fluid, boolean doDrain,
		boolean findMainTank) {
		if (findMainTank) {
			int yOff = 0;
			TileEntity offTE = this.world.getTileEntity(pos);
			TileEntityCertusTank mainTank = this;
			while (true) {
				if (offTE != null && offTE instanceof TileEntityCertusTank) {
					Fluid offFluid = ((TileEntityCertusTank) offTE).getFluid();
					if (offFluid != null && offFluid == fluid.getFluid()) {
						mainTank = (TileEntityCertusTank) this.world
							.getTileEntity(pos.up(yOff));
						yOff++;
						offTE = this.world.getTileEntity(pos.up(yOff));
						continue;
					}
				}
				break;
			}

			return mainTank != null ? mainTank.drain(fluid, doDrain, false)
				: null;
		}

		FluidStack drained = this.tank.drain(fluid.amount, doDrain);
		compareAndUpdate();

		if (drained == null || drained.amount < fluid.amount) {
			TileEntity offTE = this.world.getTileEntity(pos.down());
			if (offTE instanceof TileEntityCertusTank) {
				TileEntityCertusTank tank = (TileEntityCertusTank) offTE;
				FluidStack externallyDrained = tank.drain(new FluidStack(
						fluid.getFluid(), fluid.amount
						- (drained != null ? drained.amount : 0)),
					doDrain, false);

				if (externallyDrained != null) {
					return new FluidStack(fluid.getFluid(),
						(drained != null ? drained.amount : 0)
							+ externallyDrained.amount);
				} else {
					return drained;
				}
			}
		}

		return drained;
	}

	public int fill(FluidStack fluid, boolean doFill, boolean findMainTank) {
		if (findMainTank) {
			int yOff = 0;
			TileEntity offTE = this.world.getTileEntity(pos);
			TileEntityCertusTank mainTank = this;
			while (true) {
				if (offTE != null && offTE instanceof TileEntityCertusTank) {
					Fluid offFluid = ((TileEntityCertusTank) offTE).getFluid();
					if (offFluid == null || offFluid == fluid.getFluid()) {
						mainTank = (TileEntityCertusTank) this.world
							.getTileEntity(pos.down(yOff));
						yOff++;
						offTE = this.world.getTileEntity(pos.down(yOff));
						continue;
					}
				}
				break;
			}

			return mainTank != null ? mainTank.fill(fluid, doFill, false) : 0;
		}

		int filled = this.tank.fill(fluid, doFill);
		compareAndUpdate();

		if (filled < fluid.amount) {
			TileEntity offTE = this.world.getTileEntity(pos.up());
			if (offTE instanceof TileEntityCertusTank) {
				TileEntityCertusTank tank = (TileEntityCertusTank) offTE;
				return filled
					+ tank.fill(new FluidStack(fluid.getFluid(), fluid.amount
					- filled), doFill, false);
			}
		}

		return filled;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public Fluid getFluid() {
		FluidStack tankFluid = this.tank.getFluid();
		return tankFluid != null && tankFluid.amount > 0 ? tankFluid.getFluid()
			: null;
	}

	public Fluid getRenderFluid() {
		return this.tank.getFluid() != null ? this.tank.getFluid().getFluid()
			: null;
	}

	public float getRenderScale() {
		return (float) this.tank.getFluidAmount() / this.tank.getCapacity();
	}

	public IFluidTankProperties[] getTankInfo(boolean goToMainTank) {
		if (!goToMainTank) {
			return this.tank.getTankProperties();
		}

		int amount = 0, capacity = 0;
		Fluid fluid = null;

		int yOff = 1;
		TileEntity offTE = this.world.getTileEntity(pos.offset(EnumFacing.DOWN, yOff));
		TileEntityCertusTank mainTank = this;
		while (true) {
			if (offTE != null && offTE instanceof TileEntityCertusTank) {
				if (((TileEntityCertusTank) offTE).getFluid() == null || getFluid() == null
					|| ((TileEntityCertusTank) offTE).getFluid() == getFluid()) {
					mainTank = (TileEntityCertusTank) this.world
						.getTileEntity(pos.offset(EnumFacing.DOWN, yOff));
					yOff++;
					offTE = this.world.getTileEntity(pos.offset(EnumFacing.DOWN, yOff));
					continue;
				}
			}
			break;
		}

		BlockPos posBaseTank = pos.offset(EnumFacing.DOWN, yOff - 1);

		yOff = 0;
		offTE = this.world.getTileEntity(posBaseTank.offset(EnumFacing.UP, yOff));
		while (true) {
			if (offTE != null && offTE instanceof TileEntityCertusTank) {
				mainTank = (TileEntityCertusTank) offTE;
				if (mainTank.getFluid() == null || getFluid() == null
					|| mainTank.getFluid() == getFluid()) {
					IFluidTankProperties info = mainTank.getTankInfo(false)[0];
					if (info != null) {
						capacity += info.getCapacity();
						if (info.getContents() != null) {
							amount += info.getContents().amount;
							if (info.getContents().getFluid() != null) {
								fluid = info.getContents().getFluid();
							}
						}
					}
					yOff++;
					offTE = this.world.getTileEntity(posBaseTank.offset(EnumFacing.UP, yOff));
					continue;
				}
			}
			break;
		}

		return new IFluidTankProperties[]{new FluidTankProperties(fluid != null ? new FluidStack(fluid, amount) : null, capacity)};
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		world.markBlockRangeForRenderUpdate(pos, pos);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readFromNBTWithoutCoords(tag);
	}

	public void readFromNBTWithoutCoords(NBTTagCompound tag) {
		this.tank.readFromNBT(tag);
	}

	public void setFluid(FluidStack fluidStack) {
		tank.setFluid(fluidStack);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeToNBTWithoutCoords(tag);
		return tag;
	}

	public void writeToNBTWithoutCoords(NBTTagCompound tag) {
		this.tank.writeToNBT(tag);
	}

	private class FluidHandler implements net.minecraftforge.fluids.capability.IFluidHandler {
		@Override
		public IFluidTankProperties[] getTankProperties() {
			return TileEntityCertusTank.this.getTankInfo(true);
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null || tank.getFluid() != null
				&& resource.getFluid() != tank.getFluid().getFluid()) {
				return 0;
			}
			markDirty();
			return TileEntityCertusTank.this.fill(resource, doFill, true);
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if (tank.getFluid() == null || resource == null
				|| resource.getFluid() != tank.getFluid().getFluid()) {
				return null;
			}
			markDirty();
			return TileEntityCertusTank.this.drain(resource, doDrain, true);
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			if (tank.getFluid() == null) {
				return null;
			}
			markDirty();
			return drain(new FluidStack(tank.getFluid(), maxDrain), doDrain);
		}
	}
}
package extracells.part;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import extracells.api.gas.IAEGasStack;
import extracells.integration.mekanism.gas.Capabilities;
import extracells.util.MachineSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import extracells.gridblock.ECBaseGridBlock;
import extracells.integration.Integration;
import extracells.network.GuiHandler;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.IGasHandler;

public abstract class PartECBase implements IPart, IGridHost, IActionHost, IPowerChannelState {

	@Nullable
	private IGridNode node;
	@Nullable
	private AEPartLocation side;
	@Nullable
	private IPartHost host;
	@Nullable
	private ECBaseGridBlock gridBlock;
	private double powerUsage;
	@Nullable
	private TileEntity hostTile;
	@Nullable
	private IFluidHandler facingTank;
	@Nullable
	private Object facingGasTank;
	private boolean redstonePowered;
	private boolean isActive;
	private boolean isPowerd = false;
	@Nullable
	private EntityPlayer owner;

	@Override
	public void addToWorld() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		this.gridBlock = new ECBaseGridBlock(this);
		this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
		if (this.node != null) {
			if (this.owner != null) {
				this.node.setPlayerID(AEApi.instance().registries().players().getID(this.owner));
			}
			this.node.updateState();
		}
		setPower(null);
		onNeighborChanged();
	}

	@Override
	public boolean canBePlacedOn(BusSupport what) {
		return what != BusSupport.DENSE_CABLE;
	}

	@Override
	public boolean canConnectRedstone() {
		return false;
	}

	protected final IAEFluidStack extractFluid(IAEFluidStack toExtract, Actionable action) {
		if (this.gridBlock == null || this.facingTank == null) {
			return null;
		}
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null) {
			return null;
		}
		return monitor.extractItems(toExtract, action, new MachineSource(this));
	}

	protected final IAEFluidStack injectFluid(IAEFluidStack toInject, Actionable action) {
		if (this.gridBlock == null || this.facingTank == null) {
			return toInject;
		}
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null) {
			return toInject;
		}
		return monitor.injectItems(toInject, action, new MachineSource(this));
	}

	protected final IAEGasStack extractGas(IAEGasStack toExtract, Actionable action) {
		if (this.gridBlock == null || this.facingGasTank == null) {
			return null;
		}
		IMEMonitor<IAEGasStack> monitor = this.gridBlock.getGasMonitor();
		if (monitor == null) {
			return null;
		}
		return monitor.extractItems(toExtract, action, new MachineSource(this));
	}

	protected final IAEGasStack injectGas(IAEGasStack toInject, Actionable action) {
		if (this.gridBlock == null || this.facingGasTank == null) {
			return toInject;
		}
		IMEMonitor<IAEGasStack> monitor = this.gridBlock.getGasMonitor();
		if (monitor == null) {
			return toInject;
		}
		return monitor.injectItems(toInject, action, new MachineSource(this));
	}

	@Override
	public final IGridNode getActionableNode() {
		return this.node;
	}

	@Override
	public abstract void getBoxes(IPartCollisionHelper bch);

	@Override
	public AECableType getCableConnectionType(AEPartLocation aePartLocation) {
		return AECableType.GLASS;
	}

	public Object getClientGuiElement(EntityPlayer player) {
		return null;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
	}

	@Override
	public final IGridNode getExternalFacingNode() {
		return null;
	}

	public IFluidHandler getFacingTank() {
		return this.facingTank;
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public IGasHandler getFacingGasTank() {
		if(facingGasTank == null)
			return null;
		else if(((TileEntity)facingGasTank).hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite()))
			return ((TileEntity)facingGasTank).getCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite());
		return null;
	}

	public ECBaseGridBlock getGridBlock() {
		return this.gridBlock;
	}

	@Override
	public IGridNode getGridNode() {
		return this.node;
	}

	@Override
	public IGridNode getGridNode(AEPartLocation aePartLocation) {
		return this.node;
	}

	public IPartHost getHost() {
		return this.host;
	}

	public TileEntity getHostTile() {
		return this.hostTile;
	}

	@Override
	public ItemStack getItemStack(PartItemStack type) {
		ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1,
			PartEnum.getPartID(this));
		if (type != PartItemStack.BREAK) {
			NBTTagCompound itemNbt = new NBTTagCompound();
			writeToNBT(itemNbt);
			if (itemNbt.hasKey("node")) {
				itemNbt.removeTag("node");
			}
			is.setTagCompound(itemNbt);
		}
		return is;
	}

	@Override
	public int getLightLevel() {
		return isActive() ? 15 : 0;
	}

	@Nullable
	public final DimensionalCoord getLocation() {
		if (hostTile == null || hostTile.getWorld() == null || hostTile.getWorld().provider == null) {
			return null;
		}
		return new DimensionalCoord(hostTile.getWorld(), hostTile.getPos());
	}

	public double getPowerUsage() {
		return this.powerUsage;
	}

	public Object getServerGuiElement(EntityPlayer player) {
		return null;
	}

	public EnumFacing getFacing() {
		return this.side != null ? this.side.getFacing() : null;
	}

	public AEPartLocation getSide() {
		return side;
	}

	public List<String> getWailaBodey(NBTTagCompound tag, List<String> oldList) {
		return oldList;
	}

	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		return tag;
	}

	public void initializePart(ItemStack partStack) {
		if (partStack.hasTagCompound()) {
			readFromNBT(partStack.getTagCompound());
		}
	}

	@Override
	public boolean isActive() {
		return this.node != null ? this.node.isActive() : this.isActive;
	}

	@Override
	public boolean isLadder(EntityLivingBase entity) {
		return false;
	}

	@Override
	public boolean isPowered() {
		return this.isPowerd;
	}

	@Override
	public int isProvidingStrongPower() {
		return 0;
	}

	@Override
	public int isProvidingWeakPower() {
		return 0;
	}

	protected boolean isRedstonePowered() {
		return this.redstonePowered;
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand enumHand, Vec3d pos) {
		if (player != null && player instanceof EntityPlayerMP) {
			BlockPos hostPos = hostTile.getPos();
			GuiHandler.launchGui(GuiHandler.getGuiId(this), player, this.hostTile.getWorld(), hostPos.getX(), hostPos.getY(), hostPos.getZ());
		}
		return true;
	}

	@Override
	public void onEntityCollision(Entity entity) {
	}

	public boolean isValid() {
		if (this.hostTile != null && this.hostTile.hasWorld()) {
			DimensionalCoord loc = this.getLocation();
			TileEntity host = this.hostTile.getWorld().getTileEntity(loc.getPos());
			if (host instanceof IPartHost) {
				return ((IPartHost) host).getPart(this.side) == this;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected void onNeighborChanged(){
		onNeighborChanged(null, null, null);
	}

	@Override
	public void onNeighborChanged(IBlockAccess var1, BlockPos var2, BlockPos var3) {
		if (hostTile == null) {
			return;
		}
		World world = hostTile.getWorld();
		if (world == null)
			return;
		BlockPos pos = hostTile.getPos();
		EnumFacing facing = side.getFacing();
		TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
		if (tileEntity != null) {
			if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
				facingTank = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
			} else {
				facingTank = null;
			}
			if (Integration.Mods.MEKANISMGAS.isEnabled()) {
				updateCheckGasTank(tileEntity);
			}
		}
		this.redstonePowered = world.isBlockIndirectlyGettingPowered(pos) > 0 || world.isBlockIndirectlyGettingPowered(pos.up()) > 0;
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	private void updateCheckGasTank(TileEntity tile) {
		if (tile.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite())) {
			this.facingGasTank = tile;
		} else {
			this.facingGasTank = null;
		}
	}

	@Override
	public void onPlacement(EntityPlayer player, EnumHand enumHand, ItemStack itemStack, AEPartLocation aePartLocation) {
		this.owner = player;
	}

	@Override
	public boolean onShiftActivate(EntityPlayer entityPlayer, EnumHand enumHand, Vec3d vec3d) {
		return false;
	}

	@Override
	public void randomDisplayTick(World world, BlockPos blockPos, Random random) {

	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if (data.hasKey("node") && this.node != null) {
			this.node.loadFromNBT("node0", data.getCompoundTag("node"));
			this.node.updateState();
		}
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		this.isActive = data.readBoolean();
		this.isPowerd = data.readBoolean();
		return true;
	}

	@Override
	public void removeFromWorld() {
		if (this.node != null) {
			this.node.destroy();
		}
	}

	@Override
	public boolean requireDynamicRender() {
		return false;
	}

	protected final void saveData() {
		if (this.host != null) {
			this.host.markForSave();
		}
	}

	@Override
	public void securityBreak() {
		if (host != null) {
			host.removePart(this.side, false); // TODO drop item
		}
	}

	protected void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public void setPartHostInfo(AEPartLocation location, IPartHost iPartHost, TileEntity tileEntity) {
		this.side = location;
		this.host = iPartHost;
		this.hostTile = tileEntity;
		setPower(null);
	}

	@MENetworkEventSubscribe
	@SuppressWarnings("unused")
	public void setPower(MENetworkPowerStatusChange notUsed) {
		if (this.node != null) {
			this.isActive = this.node.isActive();
			IGrid grid = this.node.getGrid();
			if (grid != null) {
				IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
				if (energy != null) {
					this.isPowerd = energy.isNetworkPowered();
				}
			}
			this.host.markForUpdate();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		if (this.node != null) {
			NBTTagCompound nodeTag = new NBTTagCompound();
			this.node.saveToNBT("node0", nodeTag);
			data.setTag("node", nodeTag);
		}
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		data.writeBoolean(this.node != null && this.node.isActive());
		data.writeBoolean(this.isPowerd);
	}
}

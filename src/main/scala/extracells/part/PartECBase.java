package extracells.part;

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
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.*;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.gridblock.ECBaseGridBlock;
import extracells.integration.Integration;
import extracells.network.GuiHandler;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.render.TextureManager;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.IGasHandler;
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

public abstract class PartECBase implements IPart, IGridHost, IActionHost,
		IPowerChannelState {

	private IGridNode node;
	private ForgeDirection side;
	private IPartHost host;
	protected TileEntity tile;
	private ECBaseGridBlock gridBlock;
	private double powerUsage;
	private TileEntity hostTile;
	private IFluidHandler facingTank;
	private Object facingGasTank;
	private boolean redstonePowered;
	private boolean isActive;
	private boolean isPowerd = false;
	private EntityPlayer owner;
	private ItemStack is;

	public PartECBase() {
        this.is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1, PartEnum.getPartID(this));
    }

	@Override
	public void addToWorld() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
			return;
		this.gridBlock = new ECBaseGridBlock(this);
		this.node = AEApi.instance().createGridNode(this.gridBlock);
		if (this.node != null) {
			if (this.owner != null)
				this.node.setPlayerID(AEApi.instance().registries().players()
						.getID(this.owner));
			this.node.updateState();
		}
		setPower(null);
		onNeighborChanged();
	}

	@Override
	public abstract int cableConnectionRenderTo();

	@Override
	public boolean canBePlacedOn(BusSupport what) {
		return what != BusSupport.DENSE_CABLE;
	}

	@Override
	public boolean canConnectRedstone() {
		return false;
	}

	protected final IAEFluidStack extractFluid(IAEFluidStack toExtract,
			Actionable action) {
		if (this.gridBlock == null || this.facingTank == null)
			return null;
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null)
			return null;
		return monitor.extractItems(toExtract, action, new MachineSource(this));
	}

	protected final IAEFluidStack extractGasFluid(IAEFluidStack toExtract,
											   Actionable action) {
		if (this.gridBlock == null || this.facingGasTank == null)
			return null;
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null)
			return null;
		return monitor.extractItems(toExtract, action, new MachineSource(this));
	}

	protected final IAEFluidStack extractGas(IAEFluidStack toExtract,
											   Actionable action) {
		if (this.gridBlock == null || this.facingGasTank == null)
			return null;
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null)
			return null;
		return monitor.extractItems(toExtract, action, new MachineSource(this));
	}

	@Override
	public final IGridNode getActionableNode() {
		return this.node;
	}

	@Override
	public abstract void getBoxes(IPartCollisionHelper bch);

	@Override
	public IIcon getBreakingTexture() {
		return TextureManager.BUS_SIDE.getTexture();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.GLASS;
	}

	public Object getClientGuiElement(EntityPlayer player) {
		return null;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {}

	@Override
	public final IGridNode getExternalFacingNode() {
		return null;
	}

	public IFluidHandler getFacingTank() {
		return this.facingTank;
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public IGasHandler getFacingGasTank(){
		return (IGasHandler) facingGasTank;
	}

	public ECBaseGridBlock getGridBlock() {
		return this.gridBlock;
	}

	@Override
	public IGridNode getGridNode() {
		return this.node;
	}

	@Override
	public final IGridNode getGridNode(ForgeDirection dir) {
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
		return this.is;
	}

	@Override
	public int getLightLevel() {
		return 0;
	}

	public final DimensionalCoord getLocation() {
		return new DimensionalCoord(this.tile.getWorldObj(), this.tile.xCoord,
				this.tile.yCoord, this.tile.zCoord);
	}

	public double getPowerUsage() {
		return this.powerUsage;
	}

	public Object getServerGuiElement(EntityPlayer player) {
		return null;
	}

	public ForgeDirection getSide() {
		return this.side;
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

	protected final IAEFluidStack injectFluid(IAEFluidStack toInject,
			Actionable action) {
		if (this.gridBlock == null || this.facingTank == null) {
			return toInject;
		}
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null) {
			return toInject;
		}
		return monitor.injectItems(toInject, action, new MachineSource(this));
	}

	protected final IAEFluidStack injectGas(IAEFluidStack toInject, Actionable action) {
		if (this.gridBlock == null || this.facingGasTank == null) {
			return toInject;
		}
		IMEMonitor<IAEFluidStack> monitor = this.gridBlock.getFluidMonitor();
		if (monitor == null) {
			return toInject;
		}
		return monitor.injectItems(toInject, action, new MachineSource(this));
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
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (player != null && player instanceof EntityPlayerMP)
			GuiHandler.launchGui(GuiHandler.getGuiId(this), player,
					this.hostTile.getWorldObj(), this.hostTile.xCoord,
					this.hostTile.yCoord, this.hostTile.zCoord);
		return true;
	}

	@Override
	public void onEntityCollision(Entity entity) {}

	public boolean isValid() {
		if (this.hostTile != null && this.hostTile.hasWorldObj()) {
			DimensionalCoord loc = this.getLocation();
			TileEntity host = this.hostTile.getWorldObj().getTileEntity(loc.x, loc.y, loc.z);
			if (host instanceof IPartHost) {
				return ((IPartHost) host).getPart(this.side) == this;
			}
			else return false;
		}
		else return false;
	}

	@Override
	public void onNeighborChanged() {
		if (this.hostTile == null)
			return;
		World world = this.hostTile.getWorldObj();
		int x = this.hostTile.xCoord;
		int y = this.hostTile.yCoord;
		int z = this.hostTile.zCoord;
		TileEntity tileEntity = world.getTileEntity(x + this.side.offsetX, y
				+ this.side.offsetY, z + this.side.offsetZ);
		this.facingTank = null;
		if (tileEntity instanceof IFluidHandler)
			this.facingTank = (IFluidHandler) tileEntity;
		if (Integration.Mods.MEKANISMGAS.isEnabled())
			updateCheckGasTank(tileEntity);
		this.redstonePowered = world.isBlockIndirectlyGettingPowered(x, y, z)
				|| world.isBlockIndirectlyGettingPowered(x, y + 1, z);
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	private void updateCheckGasTank(TileEntity tile) {
		this.facingGasTank = null;
		if (tile != null && tile instanceof IGasHandler){
			this.facingGasTank = tile;
		}
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held,
			ForgeDirection side) {
		this.owner = player;
	}

	@Override
	public boolean onShiftActivate(EntityPlayer player, Vec3 pos) {
		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r) {}

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
		if (this.node != null)
			this.node.destroy();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z,
			IPartRenderHelper rh, RenderBlocks renderer) {}

	@Override
	@SideOnly(Side.CLIENT)
	public abstract void renderInventory(IPartRenderHelper rh,
			RenderBlocks renderer);

	@SideOnly(Side.CLIENT)
	public void renderInventoryBusLights(IPartRenderHelper rh,
			RenderBlocks renderer) {
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

	@Override
	@SideOnly(Side.CLIENT)
	public abstract void renderStatic(int x, int y, int z,
			IPartRenderHelper rh, RenderBlocks renderer);

	@SideOnly(Side.CLIENT)
	public void renderStaticBusLights(int x, int y, int z,
			IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon otherIcon = TextureManager.BUS_COLOR.getTextures()[0];
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon);
		rh.renderBlock(x, y, z, renderer);

		if (isActive()) {
			ts.setBrightness(13 << 20 | 13 << 4);
			ts.setColorOpaque_I(this.host.getColor().blackVariant);
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

	@Override
	public boolean requireDynamicRender() {
		return false;
	}

	protected final void saveData() {
		if (this.host != null)
			this.host.markForSave();
	}

	@Override
	public void securityBreak() {
		getHost().removePart(this.side, false); // TODO drop item
	}

	protected void setActive(boolean _active) {
		this.isActive = _active;
	}

	@Override
	public void setPartHostInfo(ForgeDirection _side, IPartHost _host,
			TileEntity _tile) {
		this.side = _side;
		this.host = _host;
		this.tile = _tile;
		this.hostTile = _tile;
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
				if (energy != null)
					this.isPowerd = energy.isNetworkPowered();
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

package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidEmitter;
import extracells.gui.GuiFluidEmitter;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.render.TextureManager;
import extracells.util.PermissionUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;
import java.util.Random;

public class PartFluidLevelEmitter extends PartECBase implements
		IStackWatcherHost, IFluidSlotPartOrBlock {

	private Fluid fluid;
	private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
	private IStackWatcher watcher;
	private long wantedAmount;
	private long currentAmount;
	private boolean clientRedstoneOutput = false;

	@Override
	public int cableConnectionRenderTo() {
		return 8;
	}

	public void changeWantedAmount(int modifier, EntityPlayer player) {
		setWantedAmount(this.wantedAmount + modifier, player);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(7, 7, 11, 9, 9, 16);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiFluidEmitter(this, player);
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerFluidEmitter(this, player);
	}

	private boolean isPowering() {
		switch (this.mode) {
		case LOW_SIGNAL:
			return this.wantedAmount >= this.currentAmount;
		case HIGH_SIGNAL:
			return this.wantedAmount < this.currentAmount;
		default:
			return false;
		}
	}

	@Override
	public int isProvidingStrongPower() {
		return isPowering() ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower() {
		return isProvidingStrongPower();
	}

	private void notifyTargetBlock(TileEntity _tile, ForgeDirection _side) {
		// note - params are always the same
		_tile.getWorldObj().notifyBlocksOfNeighborChange(_tile.xCoord,
				_tile.yCoord, _tile.zCoord, Blocks.air);
		_tile.getWorldObj().notifyBlocksOfNeighborChange(
				_tile.xCoord + _side.offsetX, _tile.yCoord + _side.offsetY,
				_tile.zCoord + _side.offsetZ, Blocks.air);
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
				(IPart) this)) {
			return super.onActivate(player, pos);
		}
		return false;
	}

	@Override
	public void onStackChange(IItemList o, IAEStack fullStack,
			IAEStack diffStack, BaseActionSource src, StorageChannel chan) {
		if (chan == StorageChannel.FLUIDS && diffStack != null
				&& ((IAEFluidStack) diffStack).getFluid() == this.fluid) {
			this.currentAmount = fullStack != null ? fullStack.getStackSize()
					: 0;

			IGridNode node = getGridNode();
			if (node != null) {
				setActive(node.isActive());
				getHost().markForUpdate();
				notifyTargetBlock(getHostTile(), getSide());
			}
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r) {
		if (this.clientRedstoneOutput) {
			ForgeDirection d = getSide();
			double d0 = d.offsetX * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d1 = d.offsetY * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d2 = d.offsetZ * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			world.spawnParticle("reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z
					+ d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.fluid = FluidRegistry.getFluid(data.getString("fluid"));
		this.mode = RedstoneMode.values()[data.getInteger("mode")];
		this.wantedAmount = data.getLong("wantedAmount");
		if (this.wantedAmount < 0)
			this.wantedAmount = 0;
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		this.clientRedstoneOutput = data.readBoolean();
		if (getHost() != null)
			getHost().markForUpdate();
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[0]);
		rh.setBounds(7, 7, 11, 9, 9, 14);
		rh.renderInventoryBox(renderer);

		rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[1]);
		rh.setBounds(7, 7, 14, 9, 9, 16);
		rh.renderInventoryBox(renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[0]);
		rh.setBounds(7, 7, 11, 9, 9, 14);
		rh.renderBlock(x, y, z, renderer);
		rh.setTexture(this.clientRedstoneOutput ? TextureManager.LEVEL_FRONT
				.getTextures()[2] : TextureManager.LEVEL_FRONT.getTextures()[1]);
		rh.setBounds(7, 7, 14, 9, 9, 16);
		rh.renderBlock(x, y, z, renderer);
	}

	private void updateCurrentAmount() {
		IGridNode n = getGridNode();
		if (n == null) return;
		IGrid g = n.getGrid();
		if (g == null) return;
		IStorageGrid s = g.getCache(IStorageGrid.class);
		if (s == null) return;
		IMEMonitor<IAEFluidStack> f = s.getFluidInventory();
		if (f == null) return;

		this.currentAmount = 0L;
		for (IAEFluidStack st: f.getStorageList()) {
			if (this.fluid != null && st.getFluid() == this.fluid)
				this.currentAmount = st.getStackSize();
		}

		IPartHost h = getHost();
		if (h != null) h.markForUpdate();
	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		this.fluid = _fluid;
		updateCurrentAmount();
		if (this.watcher == null)
			return;
		this.watcher.clear();
		updateWatcher(this.watcher);
		new PacketFluidSlot(Lists.newArrayList(this.fluid))
				.sendPacketToPlayer(_player);
		notifyTargetBlock(getHostTile(), getSide());
		saveData();
	}

	public void setWantedAmount(long _wantedAmount, EntityPlayer player) {
		this.wantedAmount = _wantedAmount;
		if (this.wantedAmount < 0)
			this.wantedAmount = 0;

		IPartHost h = getHost();
		if (h != null) h.markForUpdate();

		new PacketFluidEmitter(this.wantedAmount, player)
				.sendPacketToPlayer(player);
		notifyTargetBlock(getHostTile(), getSide());
		saveData();
	}

	public void syncClientGui(EntityPlayer player) {
		new PacketFluidEmitter(this.mode, player).sendPacketToPlayer(player);
		new PacketFluidEmitter(this.wantedAmount, player)
				.sendPacketToPlayer(player);
		new PacketFluidSlot(Lists.newArrayList(this.fluid))
				.sendPacketToPlayer(player);
	}

	public void toggleMode(EntityPlayer player) {
		switch (this.mode) {
			case LOW_SIGNAL:
				this.mode = RedstoneMode.HIGH_SIGNAL;
				break;
			default:
				this.mode = RedstoneMode.LOW_SIGNAL;
				break;
		}

		IPartHost h = getHost();
		if (h != null) h.markForUpdate();

		new PacketFluidEmitter(this.mode, player).sendPacketToPlayer(player);
		notifyTargetBlock(getHostTile(), getSide());
		saveData();
	}

	@Override
	public void updateWatcher(IStackWatcher newWatcher) {
		this.watcher = newWatcher;
		if (this.fluid != null)
			this.watcher.add(AEApi.instance().storage()
					.createFluidStack(new FluidStack(this.fluid, 1)));
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		if (this.fluid != null)
			data.setString("fluid", this.fluid.getName());
		else
			data.removeTag("fluid");
		data.setInteger("mode", this.mode.ordinal());
		data.setLong("wantedAmount", this.wantedAmount);
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		data.writeBoolean(isPowering());
	}
}

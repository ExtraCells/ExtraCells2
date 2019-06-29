package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerPlaneFormation;
import extracells.gridblock.ECBaseGridBlock;
import extracells.gui.GuiFluidPlaneFormation;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.render.TextureManager;
import extracells.util.ColorUtil;
import extracells.util.FluidUtil;
import extracells.util.PermissionUtil;
import extracells.util.inventory.ECPrivateInventory;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.List;

public class PartFluidPlaneFormation extends PartECBase implements
		IFluidSlotPartOrBlock, IGridTickable {

	private Fluid fluid;
	// TODO redstone control
	private RedstoneMode redstoneMode;
	private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 1,
			1) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return AEApi.instance().definitions().materials().cardRedstone().isSameAs(itemStack);
		}
	};

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : upgradeInventory.slots) {
			if (stack == null)
				continue;
			drops.add(stack);
		}
	}

	@Override
	public int cableConnectionRenderTo() {
		return 2;
	}

	public void doWork() {
		TileEntity hostTile = getHostTile();
		ECBaseGridBlock gridBlock = getGridBlock();
		ForgeDirection side = getSide();

		if (this.fluid == null || hostTile == null || gridBlock == null || this.fluid.getBlock() == null)
			return;
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null)
			return;
		World world = hostTile.getWorldObj();
		int x = hostTile.xCoord + side.offsetX;
		int y = hostTile.yCoord + side.offsetY;
		int z = hostTile.zCoord + side.offsetZ;
		Block worldBlock = world.getBlock(x, y, z);
		if (worldBlock != null && worldBlock != Blocks.air)
			return;
		IAEFluidStack canDrain = monitor.extractItems(FluidUtil
				.createAEFluidStack(this.fluid,
						FluidContainerRegistry.BUCKET_VOLUME),
				Actionable.SIMULATE, new MachineSource(this));
		if (canDrain == null
				|| canDrain.getStackSize() < FluidContainerRegistry.BUCKET_VOLUME)
			return;
		monitor.extractItems(FluidUtil.createAEFluidStack(this.fluid,
				FluidContainerRegistry.BUCKET_VOLUME), Actionable.MODULATE,
				new MachineSource(this));
		Block fluidWorldBlock = this.fluid.getBlock();
		world.setBlock(x, y, z, fluidWorldBlock);
		world.markBlockForUpdate(x, y, z);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiFluidPlaneFormation(this, player);
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerPlaneFormation(this, player);
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	public ECPrivateInventory getUpgradeInventory() {
		return this.upgradeInventory;
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
	public void readFromNBT(NBTTagCompound data) {
		this.fluid = FluidRegistry.getFluid(data.getString("fluid"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		IIcon side = TextureManager.PANE_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		rh.setInvColor(AEColor.Cyan.blackVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[0],
				ForgeDirection.SOUTH, renderer);
		Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
		rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.mediumVariant));
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[1],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.whiteVariant));
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[2],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;
		IIcon side = TextureManager.PANE_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		IPartHost host = getHost();
		if (host != null) {
			ts.setColorOpaque_I(host.getColor().blackVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[0],
					ForgeDirection.SOUTH, renderer);
			if (isActive())
				ts.setBrightness(13 << 20 | 13 << 4);
			ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.getColor().mediumVariant));
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[1],
					ForgeDirection.SOUTH, renderer);
			ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.getColor().whiteVariant));
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[2],
					ForgeDirection.SOUTH, renderer);
		}

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	public void sendInformation(EntityPlayer _player) {
		new PacketFluidSlot(Lists.newArrayList(this.fluid))
				.sendPacketToPlayer(_player);
	}

	@Override
	public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
		this.fluid = _fluid;
		new PacketFluidSlot(Lists.newArrayList(this.fluid))
				.sendPacketToPlayer(_player);
		saveData();
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node,
			int TicksSinceLastCall) {
		doWork();
		return TickRateModulation.SAME;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setString("fluid", this.fluid == null ? "" : this.fluid.getName());
	}
}

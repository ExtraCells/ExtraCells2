package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.commons.lang3.tuple.MutablePair;

public class PartFluidConversionMonitor extends PartFluidStorageMonitor {

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if (super.onActivate(player, pos))
			return true;
		if (player == null || player.worldObj == null)
			return true;
		if (player.worldObj.isRemote)
			return true;
		ItemStack s = player.getCurrentEquippedItem();
		IMEMonitor<IAEFluidStack> mon = getFluidStorage();
		if (this.locked && s != null && mon != null) {
			ItemStack s2 = s.copy();
			s2.stackSize = 1;
			if (FluidUtil.isFilled(s2)) {
				FluidStack f = FluidUtil.getFluidFromContainer(s2);
				if (f == null)
					return true;
				IAEFluidStack fl = FluidUtil.createAEFluidStack(f);
				IAEFluidStack not = mon.injectItems(fl.copy(),
						Actionable.SIMULATE, new MachineSource(this));
				if (mon.canAccept(fl)
						&& (not == null || not.getStackSize() == 0L)) {
					MutablePair<Integer, ItemStack> empty1 = FluidUtil.drainStack(s2, f);
					int amount = empty1.getLeft();
					if (amount > 0) {
						f.amount = amount;
						fl.setStackSize(amount);
						not = mon.injectItems(fl.copy(), Actionable.SIMULATE, new MachineSource(this));
						if (mon.canAccept(fl) && (not == null || not.getStackSize() == 0L)) {
							mon.injectItems(fl, Actionable.MODULATE, new MachineSource(this));
							ItemStack empty = empty1.right;
							if (empty != null) {
								TileEntity tile = this.getHost().getTile();
								ForgeDirection side = this.getSide();
								this.dropItems(tile.getWorldObj(), tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ, empty);
							}
							ItemStack s3 = s.copy();
							s3.stackSize--;
							if (s3.stackSize <= 0)
								player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
							else
								player.inventory.setInventorySlotContents(player.inventory.currentItem, s3);
						}
					}
				}
				return true;
			} else if (FluidUtil.isEmpty(s2)) {
				if (this.fluid == null)
					return true;
				IAEFluidStack extract;
				if (s2.getItem() instanceof IFluidContainerItem) {
					extract = mon.extractItems(FluidUtil.createAEFluidStack(
							this.fluid, ((IFluidContainerItem) s2.getItem())
									.getCapacity(s2)), Actionable.SIMULATE,
							new MachineSource(this));
				} else
					extract = mon.extractItems(
							FluidUtil.createAEFluidStack(this.fluid),
							Actionable.SIMULATE, new MachineSource(this));
				if (extract != null) {
					if (extract.getStackSize() <= 0)
						return true;
					extract = mon.extractItems(extract, Actionable.MODULATE, new MachineSource(this));
					if (extract == null || extract.getStackSize() <= 0)
						return true;

					MutablePair<Integer, ItemStack> empty1 = FluidUtil
							.fillStack(s2, extract.getFluidStack());
					if (empty1.left == 0) {
						mon.injectItems(extract, Actionable.MODULATE, new MachineSource(this));
						return true;
					}
					ItemStack empty = empty1.right;
					if (empty != null) {
						dropItems(getHost().getTile().getWorldObj(), getHost()
								.getTile().xCoord + getSide().offsetX,
								getHost().getTile().yCoord + getSide().offsetY,
								getHost().getTile().zCoord + getSide().offsetZ,
								empty);
					}
					ItemStack s3 = s.copy();
					s3.stackSize = s3.stackSize - 1;
					if (s3.stackSize == 0) {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, null);
					} else {
						player.inventory.setInventorySlotContents(
								player.inventory.currentItem, s3);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderInventoryBox(renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		ts.setBrightness(13 << 20 | 13 << 4);

		rh.setInvColor(0xFFFFFF);
		rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(),
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(3, 3, 15, 13, 13, 16);
		rh.setInvColor(AEColor.Transparent.blackVariant);
		rh.renderInventoryFace(
				TextureManager.CONVERSION_MONITOR.getTextures()[0],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.mediumVariant);
		rh.renderInventoryFace(
				TextureManager.CONVERSION_MONITOR.getTextures()[1],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.whiteVariant);
		rh.renderInventoryFace(
				TextureManager.CONVERSION_MONITOR.getTextures()[2],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		renderInventoryBusLights(rh, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		if (isActive())
			Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

		ts.setColorOpaque_I(0xFFFFFF);
		rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(),
				ForgeDirection.SOUTH, renderer);

		IPartHost host = getHost();
		rh.setBounds(3, 3, 15, 13, 13, 16);
		ts.setColorOpaque_I(host.getColor().mediumVariant);
		rh.renderFace(x, y, z,
				TextureManager.CONVERSION_MONITOR.getTextures()[0],
				ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().whiteVariant);
		rh.renderFace(x, y, z,
				TextureManager.CONVERSION_MONITOR.getTextures()[1],
				ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().blackVariant);
		rh.renderFace(x, y, z,
				TextureManager.CONVERSION_MONITOR.getTextures()[2],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

}

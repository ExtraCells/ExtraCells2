package extracells.part;

import java.io.IOException;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import extracells.inventory.IInventoryListener;
import extracells.inventory.InventoryPlain;
import extracells.models.PartModels;
import io.netty.buffer.ByteBuf;

public class PartBattery extends PartECBase implements IAEPowerStorage, IInventoryListener {

	//private IIcon batteryIcon = TextureManager.BATTERY_FRONT.getTexture();
	private ItemStack battery;
	IAEItemPowerStorage handler;
	private InventoryPlain inventory = new InventoryPlain(
		"extracells.part.battery", 1, 1) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			return itemStack != null
				&& itemStack.getItem() instanceof IAEItemPowerStorage;
		}
	};

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : inventory.slots) {
			if (stack == null) {
				continue;
			}
			drops.add(stack);
		}
	}

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 2.0F;
	}

	@Override
	public double extractAEPower(double amt, Actionable mode,
		PowerMultiplier usePowerMultiplier) {
		if (this.handler == null || this.battery == null) {
			return 0;
		}
		return this.handler.extractAEPower(this.battery, usePowerMultiplier.multiply(amt), mode);
	}

	@Override
	public double getAECurrentPower() {
		if (this.handler == null || this.battery == null) {
			return 0;
		}
		return this.handler.getAECurrentPower(this.battery);
	}

	@Override
	public double getAEMaxPower() {
		if (this.handler == null || this.battery == null) {
			return 0;
		}
		return this.handler.getAEMaxPower(this.battery);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
	}

	@Override
	public AccessRestriction getPowerFlow() {
		if (this.handler == null || this.battery == null) {
			return AccessRestriction.NO_ACCESS;
		}
		return this.handler.getPowerFlow(this.battery);
	}

	@Override
	public double injectAEPower(double amt, Actionable mode) {
		if (this.handler == null || this.battery == null) {
			return 0;
		}
		return this.handler.injectAEPower(this.battery, amt, mode);
	}

	@Override
	public boolean isAEPublicPowerStorage() {
		return true;
	}

	@Override
	public void onInventoryChanged() {
		this.battery = this.inventory.getStackInSlot(0);
		if (this.battery != null
			&& this.battery.getItem() instanceof IAEItemPowerStorage) {
			//this.batteryIcon = this.battery.getgetIconIndex();
			this.handler = (IAEItemPowerStorage) this.battery.getItem();
		} else {
			//this.batteryIcon = null;
			this.handler = null;
		}
		IGridNode node = getGridNode();
		if (node != null) {
			IGrid grid = node.getGrid();
			if (grid != null) {
				grid.postEvent(new MENetworkPowerStorage(this,
					MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
			}
			getHost().markForUpdate();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.inventory.readFromNBT(data.getTagList("inventory", 10));
		onInventoryChanged();
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		/*String iconName = AbstractPacket.readString(data);
		if (!iconName.equals("none")) {
			this.batteryIcon = ((TextureMap) Minecraft.getMinecraft()
					.getTextureManager()
					.getTexture(TextureMap.locationBlocksTexture))
					.getAtlasSprite(iconName);
		} else {
			this.batteryIcon = TextureManager.BATTERY_FRONT.getTexture();
		}*/
		return true;
	}

	/*@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side,
				TextureManager.BATTERY_FRONT.getTextures()[0], side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, this.batteryIcon, side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}*/

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setTag("inventory", this.inventory.writeToNBT());
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
	/*	AbstractPacket.writeString(this.battery != null ? this.battery
				.getItem().getIconIndex(this.battery).getIconName() : "none",
				data);*/
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.BATTERY_PLANE_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.BATTERY_PLANE_ON;
		} else {
			return PartModels.BATTERY_PLANE_OFF;
		}
	}
}

package extracells.part.fluid;

import java.io.IOException;
import java.util.List;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import extracells.util.StorageChannels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import extracells.models.PartModels;
import extracells.part.PartECBase;
import extracells.util.AEUtils;
import extracells.util.FluidHelper;
import extracells.util.WrenchUtil;
import io.netty.buffer.ByteBuf;

public class PartFluidStorageMonitor extends PartECBase implements IStackWatcherHost {

	protected Fluid fluid = null;
	protected long amount = 0L;
	private Object dspList;

	protected boolean locked = false;

	protected IStackWatcher watcher = null;

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 1.0F;
	}

	protected void dropItems(World world, BlockPos pos, ItemStack stack) {
		if (world == null) {
			return;
		}
		if (!world.isRemote) {
			float f = 0.7F;
			double d0 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			EntityItem entityitem = new EntityItem(world, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, stack);
			entityitem.setPickupDelay(10);
			world.spawnEntity(entityitem);
		}
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(4, 4, 13, 12, 12, 14);
		bch.addBox(5, 5, 12, 11, 11, 13);
	}

	protected IMEMonitor<IAEFluidStack> getFluidStorage() {
		IGridNode n = getGridNode();
		if (n == null) {
			return null;
		}
		IGrid g = n.getGrid();
		if (g == null) {
			return null;
		}
		IStorageGrid storage = g.getCache(IStorageGrid.class);
		if (storage == null) {
			return null;
		}
		return storage.getInventory(StorageChannels.FLUID());
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public List<String> getWailaBodey(NBTTagCompound data, List<String> list) {
		super.getWailaBodey(data, list);
		long amount = 0L;
		Fluid fluid = null;
		if (data.hasKey("locked") && data.getBoolean("locked")) {
			list.add(I18n
				.translateToLocal("waila.appliedenergistics2.Locked"));
		} else {
			list.add(I18n
				.translateToLocal("waila.appliedenergistics2.Unlocked"));
		}
		if (data.hasKey("amount")) {
			amount = data.getLong("amount");
		}
		if (data.hasKey("fluid")) {
			String fluidName = data.getString("fluid");
			if (!fluidName.isEmpty()) {
				fluid = FluidRegistry.getFluid(fluidName);
			}
		}
		if (fluid != null) {
			list.add(I18n.translateToLocal("extracells.tooltip.fluid")
				+ ": "
				+ fluid.getLocalizedName(new FluidStack(fluid,
				Fluid.BUCKET_VOLUME)));
			if (isActive()) {
				list.add(I18n
					.translateToLocal("extracells.tooltip.amount")
					+ ": "
					+ amount + "mB");
			} else {
				list.add(I18n
					.translateToLocal("extracells.tooltip.amount")
					+ ": 0mB");
			}
		} else {
			list.add(I18n.translateToLocal("extracells.tooltip.fluid")
				+ ": "
				+ I18n
				.translateToLocal("extracells.tooltip.empty1"));
			list.add(I18n
				.translateToLocal("extracells.tooltip.amount") + ": 0mB");
		}
		return list;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		super.getWailaTag(tag);
		tag.setBoolean("locked", this.locked);
		tag.setLong("amount", this.amount);
		if (this.fluid == null) {
			tag.setString("fluid", "");
		} else {
			tag.setString("fluid", this.fluid.getName());
		}
		return tag;
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (player == null || player.world == null) {
			return true;
		}
		if (player.world.isRemote) {
			return true;
		}
		ItemStack s = player.getHeldItem(hand);
		if (s == null) {
			if (this.locked) {
				return false;
			}
			if (this.fluid == null) {
				return true;
			}
			if (this.watcher != null) {
				this.watcher.remove(AEUtils.createFluidStack(this.fluid));
			}
			this.fluid = null;
			this.amount = 0L;
			IPartHost host = getHost();
			if (host != null) {
				host.markForUpdate();
			}
			return true;
		}
		if (WrenchUtil.canWrench(s, player, getHostTile().getPos())) {
			this.locked = !this.locked;
			WrenchUtil.wrenchUsed(s, player, getHostTile().getPos());
			IPartHost host = getHost();
			if (host != null) {
				host.markForUpdate();
			}
			if (this.locked) {
				player.sendMessage(new TextComponentTranslation(
					"chat.appliedenergistics2.isNowLocked"));
			} else {
				player.sendMessage(new TextComponentTranslation(
					"chat.appliedenergistics2.isNowUnlocked"));
			}
			return true;
		}
		if (this.locked) {
			return false;
		}
		if (!FluidHelper.isEmpty(s)) {
			if (this.fluid != null && this.watcher != null) {
				this.watcher.remove(AEUtils.createFluidStack(this.fluid));
			}
			this.fluid = FluidHelper.getFluidFromContainer(s).getFluid();
			if (this.watcher != null) {
				this.watcher.add(AEUtils.createFluidStack(this.fluid));
			}
			IPartHost host = getHost();
			if (host != null) {
				host.markForUpdate();
			}
			return true;
		}
		return false;
	}


	@Override
	public void onStackChange(IItemList<?> arg0, IAEStack<?> arg1, IAEStack<?> arg2,
							  IActionSource arg3, IStorageChannel<?> arg4) {
		if (this.fluid != null) {
			IGridNode n = getGridNode();
			if (n == null) {
				return;
			}
			IGrid g = n.getGrid();
			if (g == null) {
				return;
			}
			IStorageGrid storage = g.getCache(IStorageGrid.class);
			if (storage == null) {
				return;
			}
			IMEMonitor<IAEFluidStack> fluids = getFluidStorage();
			if (fluids == null) {
				return;
			}
			for (IAEFluidStack s : fluids.getStorageList()) {
				if (s.getFluid() == this.fluid) {
					this.amount = s.getStackSize();
					IPartHost host = getHost();
					if (host != null) {
						host.markForUpdate();
					}
					return;
				}
			}
			this.amount = 0L;
			IPartHost host = getHost();
			if (host != null) {
				host.markForUpdate();
			}
		}

	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		if (data.hasKey("amount")) {
			this.amount = data.getLong("amount");
		}
		if (data.hasKey("fluid")) {
			String name = data.getString("fluid");
			if (name.isEmpty()) {
				this.fluid = null;
			} else {
				this.fluid = FluidRegistry.getFluid(name);
			}
		}
		if (data.hasKey("locked")) {
			this.locked = data.getBoolean("locked");
		}
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		this.amount = data.readLong();
		String name = ByteBufUtils.readUTF8String(data);
		if (name.isEmpty()) {
			this.fluid = null;
		} else {
			this.fluid = FluidRegistry.getFluid(name);
		}
		this.locked = data.readBoolean();
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z, float partialTicks, int destroyStage) {
		if (this.fluid == null) {
			return;
		}

		if (this.dspList == null) {
			this.dspList = GLAllocation.generateDisplayLists(1);
		}

		if (!isActive()) {
			return;
		}

		IAEFluidStack aeFluidStack = AEUtils.createFluidStack(this.fluid);
		aeFluidStack.setStackSize(this.amount);
		if (aeFluidStack != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

			GlStateManager.glNewList((Integer) this.dspList, GL11.GL_COMPILE_AND_EXECUTE);
			Tessellator tess = Tessellator.getInstance();
			this.renderFluid(tess, aeFluidStack);
			GlStateManager.glEndList();

			GlStateManager.popMatrix();
		}
	}

	@SideOnly(Side.CLIENT)
	private void renderFluid(Tessellator tess, IAEFluidStack fluidStack) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		EnumFacing facing = this.getSide().getFacing();

		moveToFace(facing);
		rotateToFace(facing, (byte) 1);

		GlStateManager.pushMatrix();
		try {

			int br = 16 << 20 | 16 << 4;
			int var11 = br % 65536;
			int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
				var11 * 0.8F, var12 * 0.8F);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.disableLighting();
			GlStateManager.disableRescaleNormal();
			// RenderHelper.enableGUIStandardItemLighting();

			Minecraft mc = Minecraft.getMinecraft();

			ResourceLocation fluidStill = fluid.getStill();

			if (fluidStill != null) {
				TextureMap textureMap = mc.getTextureMapBlocks();
				TextureAtlasSprite fluidIcon = textureMap.getAtlasSprite(fluidStill.toString());
				if (fluidIcon != null) {
					GL11.glTranslatef(0.0f, 0.14f, -0.24f);
					GL11.glScalef(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);
					GL11.glTranslated(-8.6F, -16.3, -1.2F);
					mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					BufferBuilder buffer = tess.getBuffer();
					buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
					try {
						buffer.pos(0, 16, 0).tex(fluidIcon.getMinU(), fluidIcon.getMaxV()).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
						buffer.pos(16, 16, 0).tex(fluidIcon.getMaxU(), fluidIcon.getMaxV()).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
						buffer.pos(16, 0, 0).tex(fluidIcon.getMaxU(), fluidIcon.getMinV()).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
						buffer.pos(0, 0, 0).tex(fluidIcon.getMinU(), fluidIcon.getMinV()).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
					} finally {
						tess.draw();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		GlStateManager.popMatrix();

		GlStateManager.translate(0.0f, 0.14f, -0.24f);
		GlStateManager.scale(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);

		long qty = fluidStack.getStackSize();
		if (qty > 999999999999L) {
			qty = 999999999999L;
		}

		String msg = Long.toString(qty) + "mB";
		if (qty > 1000000000) {
			msg = Long.toString(qty / 1000000000) + "MB";
		} else if (qty > 1000000) {
			msg = Long.toString(qty / 1000000) + "KB";
		} else if (qty > 9999) {
			msg = Long.toString(qty / 1000) + 'B';
		}

		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int width = fr.getStringWidth(msg);
		GlStateManager.translate(-0.5f * width, 0.0f, -1.0f);
		fr.drawString(msg, 0, 0, 0);

		GlStateManager.popAttrib();
	}

	private static void moveToFace(EnumFacing face) {
		GlStateManager.translate(face.getFrontOffsetX() * 0.77, face.getFrontOffsetY() * 0.77, face.getFrontOffsetZ() * 0.77);
	}

	private static void rotateToFace(EnumFacing face, byte spin) {
		switch (face) {
			case UP:
				GlStateManager.scale(1.0f, -1.0f, 1.0f);
				GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate(spin * 90.0F, 0, 0, 1);
				break;
			case DOWN:
				GlStateManager.scale(1.0f, -1.0f, 1.0f);
				GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate(spin * -90.0F, 0, 0, 1);
				break;
			case EAST:
				GlStateManager.scale(-1.0f, -1.0f, -1.0f);
				GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
				break;
			case WEST:
				GlStateManager.scale(-1.0f, -1.0f, -1.0f);
				GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
				break;
			case NORTH:
				GlStateManager.scale(-1.0f, -1.0f, -1.0f);
				break;
			case SOUTH:
				GlStateManager.scale(-1.0f, -1.0f, -1.0f);
				GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
				break;
			default:
				break;
		}
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.STORAGE_MONITOR_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.STORAGE_MONITOR_ON;
		} else {
			return PartModels.STORAGE_MONITOR_OFF;
		}
	}

	@Override
	public boolean requireDynamicRender() {
		return true;
	}

	@Override
	public void updateWatcher(IStackWatcher w) {
		this.watcher = w;
		if (this.fluid != null) {
			w.add(AEUtils.createFluidStack(this.fluid));
		}
		onStackChange(null, null, null, null, null);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setLong("amount", this.amount);
		if (this.fluid == null) {
			data.setInteger("fluid", -1);
		} else {
			data.setString("fluid", this.fluid.getName());
		}
		data.setBoolean("locked", this.locked);
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		data.writeLong(this.amount);
		if (this.fluid == null) {
			ByteBufUtils.writeUTF8String(data, "");
		} else {
			ByteBufUtils.writeUTF8String(data, fluid.getName());
		}
		data.writeBoolean(this.locked);

	}
}

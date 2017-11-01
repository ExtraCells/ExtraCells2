package extracells.gui.widget.fluid;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import extracells.gui.widget.AbstractWidget;
import extracells.gui.widget.WidgetManager;
import extracells.integration.Integration;
import extracells.network.packet.other.PacketFluidSlotSelect;
import extracells.part.gas.PartGasExport;
import extracells.part.gas.PartGasImport;
import extracells.part.gas.PartGasStorage;
import extracells.util.FluidHelper;
import extracells.util.GasUtil;
import extracells.util.NetworkUtil;
import mekanism.api.gas.GasStack;

@SideOnly(Side.CLIENT)
public class WidgetFluidSlot extends AbstractWidget {

	public interface IConfigurable {

		byte getConfigState();
	}

	private int id;
	private Fluid fluid;
	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
	private IFluidSlotListener listener;
	private IConfigurable configurable;

	private byte configOption;

	public WidgetFluidSlot(WidgetManager widgetManager, IFluidSlotListener listener, int posX, int posY) {
		this(widgetManager, listener, 0, posX, posY, null, (byte) 0);
	}

	public WidgetFluidSlot(WidgetManager widgetManager, IFluidSlotListener listener, int id, int posX, int posY) {
		this(widgetManager, listener, id, posX, posY, null, (byte) 0);
	}

	public WidgetFluidSlot(WidgetManager widgetManager, IFluidSlotListener listener, int id, int posX, int posY, IConfigurable configurable, byte configOption) {
		super(widgetManager, posX, posY);
		this.width = 18;
		this.height = 18;
		this.listener = listener;
		this.id = id;
		this.configurable = configurable;
		this.configOption = configOption;
	}

	public boolean isVisable() {
		return this.configurable == null || this.configurable.getConfigState() >= this.configOption;
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		if (!isVisable()) {
			return;
		}
		TextureManager textureManager = manager.mc.getTextureManager();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		textureManager.bindTexture(guiTexture);
		manager.gui.drawTexturedModalRect(xPos, yPos, 79, 39, 18, 18);

		if (this.fluid != null) {
			drawFluid(textureManager);
		}

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

	@Override
	public void drawOverlay(int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		TextureManager textureManager = manager.mc.getTextureManager();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		textureManager.bindTexture(guiTexture);
		manager.gui.drawGradientRect(xPos + 1, yPos + 1, xPos + 17, yPos + 17, -0x7F000001, -0x7F000001);
		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
	}

	private void drawFluid(TextureManager textureManager) {
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = manager.mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		manager.gui.drawTexturedModalRect(this.xPos + 1, this.yPos + 1, sprite, 16, 16);
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		EntityPlayer player = manager.mc.player;
		ItemStack stack = player.inventory.getItemStack();
		handleContainer(stack);
	}

	public void handleContainer(ItemStack stack) {
		if (!isVisable()) {
			return;
		}
		if ((listener instanceof PartGasImport || listener instanceof PartGasExport || listener instanceof PartGasStorage) && Integration.Mods.MEKANISMGAS.isEnabled()) {
			handleGasContainer(stack);
		} else {
			handleFluidContainer(stack);
		}
	}

	public void handleFluidContainer(ItemStack stack) {
		FluidStack fluidStack = FluidHelper.getFluidFromContainer(stack);
		this.fluid = fluidStack == null ? null : fluidStack.getFluid();
		NetworkUtil.sendToServer(new PacketFluidSlotSelect(listener, id, fluid));
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public void handleGasContainer(ItemStack stack) {
		GasStack gasStack = GasUtil.getGasFromContainer(stack);
		FluidStack fluidStack = GasUtil.getFluidStack(gasStack);
		this.fluid = fluidStack == null ? null : fluidStack.getFluid();
		NetworkUtil.sendToServer(new PacketFluidSlotSelect(listener, id, fluid));
	}

	@Override
	public List<String> getToolTip(int mouseX, int mouseY) {
		if (fluid == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(I18n.translateToLocal(fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME))));
	}

	public void setFluid(Fluid fluid) {
		this.fluid = fluid;
	}
}

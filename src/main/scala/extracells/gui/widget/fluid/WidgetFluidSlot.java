package extracells.gui.widget.fluid;

import cpw.mods.fml.common.Optional;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.util.FluidUtil;
import extracells.util.GasUtil;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;
import java.util.ArrayList;
public class WidgetFluidSlot extends Gui {

	public interface IConfigurable {

		public byte getConfigState();
	}

	private int id;
	private int posX, posY;
	private Fluid fluid;
	private static final ResourceLocation guiTexture = new ResourceLocation(
			"extracells", "textures/gui/busiofluid.png");
	private IFluidSlotPartOrBlock part;
	private EntityPlayer player;
	private IConfigurable configurable;

	private byte configOption;

	public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPartOrBlock _part,
			int _posX, int _posY) {
		this(_player, _part, 0, _posX, _posY, null, (byte) 0);
	}

	public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPartOrBlock _part,
			int _id, int _posX, int _posY) {
		this(_player, _part, _id, _posX, _posY, null, (byte) 0);
	}

	public WidgetFluidSlot(EntityPlayer _player, IFluidSlotPartOrBlock _part,
			int _id, int _posX, int _posY, IConfigurable _configurable,
			byte _configOption) {
		this.player = _player;
		this.part = _part;
		this.id = _id;
		this.posX = _posX;
		this.posY = _posY;
		this.configurable = _configurable;
		this.configOption = _configOption;
	}

	public boolean canRender() {
		return this.configurable == null
				|| this.configurable.getConfigState() >= this.configOption;
	}

	@SuppressWarnings("rawtypes")
	protected void drawHoveringText(List list, int x, int y,
			FontRenderer fontrenderer) {
		boolean lighting_enabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
		if (!list.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;

			for (Object string : list) {
				String s = (String) string;
				int l = fontrenderer.getStringWidth(s);

				if (l > k) {
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if (list.size() > 1) {
				k1 += 2 + (list.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4,
					l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1,
					l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3,
					l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3
					- 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1
					+ 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2,
					i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3,
					j2, j2);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				String s1 = (String) list.get(k2);
				fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			if (lighting_enabled)
				GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public void drawTooltip(int x, int y) {
		if (canRender()) {
		if (this.fluid != null) {
			List<String> description = new ArrayList<String>();
			description.add(this.fluid.getLocalizedName(new FluidStack(this.fluid,0)));
			drawHoveringText(description, x, y, Minecraft.getMinecraft().fontRenderer);
		}
		}
	}

	public void drawWidget() {
		if (!canRender())
			return;
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor3f(1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(this.posX, this.posY, 79, 39, 18, 18);
		GL11.glEnable(GL11.GL_LIGHTING);

		if (this.fluid == null || this.fluid.getIcon() == null)
			return;

		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		GL11.glColor3f((this.fluid.getColor() >> 16 & 0xFF) / 255.0F, (this.fluid.getColor() >> 8 & 0xFF) / 255.0F, (this.fluid.getColor() & 0xFF) / 255.0F);
		drawTexturedModelRectFromIcon(this.posX + 1, this.posY + 1,
				this.fluid.getIcon(), 16, 16);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	public int getPosX() {
		return this.posX;
	}

	public int getPosY() {
		return this.posY;
	}

	public void mouseClicked(ItemStack stack) {
		FluidStack fluidStack = FluidUtil.getFluidFromContainer(stack);
		this.fluid = fluidStack == null ? null : fluidStack.getFluid();
		new PacketFluidSlot(this.part, this.id, this.fluid, this.player).sendPacketToServer();
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public void mouseClickedGas(ItemStack stack) {
		GasStack gasStack = GasUtil.getGasFromContainer(stack);
		FluidStack fluidStack = GasUtil.getFluidStack(gasStack);
		this.fluid = fluidStack == null ? null : fluidStack.getFluid();
		new PacketFluidSlot(this.part, this.id, this.fluid, this.player).sendPacketToServer();
	}

	public void setFluid(Fluid _fluid) {
		this.fluid = _fluid;
	}
}

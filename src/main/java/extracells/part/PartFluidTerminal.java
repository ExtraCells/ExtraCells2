package extracells.part;

import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.PacketFluidTerminal;
import extracells.render.TextureManager;
import extracells.util.ECPrivateInventory;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.ArrayList;
import java.util.List;

public class PartFluidTerminal extends PartECBase
{
	private Fluid currentFluid;
	private List<ContainerFluidTerminal> containers = new ArrayList<ContainerFluidTerminal>();
	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.fluid.terminal", 2, 64)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return FluidContainerRegistry.isContainer(itemstack) || (itemstack != null && itemstack.getItem() instanceof IFluidContainerItem);
		}
	};

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F);
		rh.renderInventoryBox(renderer);

		ts.setBrightness(13 << 20 | 13 << 4);

		rh.setInvColor(0xFFFFFF);
		rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		rh.setBounds(3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F);
		rh.setInvColor(AEColor.Transparent.blackVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.mediumVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.whiteVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F);
		renderInventoryBusLights(rh, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F);
		rh.renderBlock(x, y, z, renderer);

		if (isActive)
			Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

		ts.setColorOpaque_I(0xFFFFFF);
		rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		rh.setBounds(3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16);
		ts.setColorOpaque_I(host.getColor().blackVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().mediumVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().whiteVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F);
		renderStaticBusLights(x, y, z, rh, renderer);
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT(data);
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	public void setCurrentFluid(Fluid _currentFluid)
	{
		currentFluid = _currentFluid;
		for (ContainerFluidTerminal containerTerminalFluid : containers)
		{
			new PacketFluidTerminal(containerTerminalFluid.getPlayer(), currentFluid).sendPacketToPlayer(containerTerminalFluid.getPlayer());
		}
	}

	public void addContainer(ContainerFluidTerminal containerTerminalFluid)
	{
		containers.add(containerTerminalFluid);
		new PacketFluidTerminal(containerTerminalFluid.getPlayer(), currentFluid).sendPacketToPlayer(containerTerminalFluid.getPlayer());
	}

	public void removeContainer(ContainerFluidTerminal containerTerminalFluid)
	{
		containers.remove(containerTerminalFluid);
	}

	public Object getServerGuiElement(EntityPlayer player)
	{
		return new ContainerFluidTerminal(this, player);
	}

	public Object getClientGuiElement(EntityPlayer player)
	{
		return new GuiFluidTerminal(this, player);
	}

	public IInventory getInventory()
	{
		return inventory;
	}

}

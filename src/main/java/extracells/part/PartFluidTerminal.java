package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.PacketFluidTerminal;
import extracells.render.TextureManager;
import extracells.util.ECPrivateInventory;
import extracells.util.FluidUtil;
import javafx.util.Pair;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class PartFluidTerminal extends PartECBase implements ECPrivateInventory.IInventoryUpdateReceiver
{
	private Fluid currentFluid;
	private List<ContainerFluidTerminal> containers = new ArrayList<ContainerFluidTerminal>();
	private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.fluid.terminal", 2, 64)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemStack)
		{
			return FluidUtil.isFluidContainer(itemStack);
		}
	};

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderInventoryBox(renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);

		ts.setBrightness(13 << 20 | 13 << 4);

		rh.setInvColor(0xFFFFFF);
		rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		rh.setBounds(3, 3, 15, 13, 13, 16);
		rh.setInvColor(AEColor.Transparent.blackVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.mediumVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Transparent.whiteVariant);
		rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
		renderInventoryBusLights(rh, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side);
		rh.setBounds(4, 4, 13, 12, 12, 14);
		rh.renderBlock(x, y, z, renderer);
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);

		if (isActive())
			Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

		ts.setColorOpaque_I(0xFFFFFF);
		rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

		rh.setBounds(3, 3, 15, 13, 13, 16);
		ts.setColorOpaque_I(host.getColor().blackVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().mediumVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		ts.setColorOpaque_I(host.getColor().whiteVariant);
		rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 12, 11, 11, 13);
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
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(4, 4, 13, 12, 12, 14);
		bch.addBox(5, 5, 12, 11, 11, 13);
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

	@Override
	public void onInventoryChanged()
	{
		ItemStack container = inventory.getStackInSlot(0);
		if (!FluidUtil.isFluidContainer(container))
			return;

		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null)
			return;

		FluidStack containerFluid = FluidUtil.getFluidFromContainer(container);
		if (containerFluid != null)
		{
			IAEFluidStack notAdded = monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.SIMULATE, new MachineSource(this));
			FluidStack toDrain;
			if (notAdded != null)
			{
				if (notAdded.getStackSize() == containerFluid.amount)
					return;
				toDrain = new FluidStack(containerFluid.fluidID, (int) (containerFluid.amount - notAdded.getStackSize()));
			} else
			{
				toDrain = containerFluid.copy();
			}
			Pair<Integer, ItemStack> result = FluidUtil.drainStack(container, toDrain, false);
			if (result != null)
			{
				int drained = result.getKey();
				if (drained <= 0)
					return;
				toDrain.amount = drained;
				monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.MODULATE, new MachineSource(this));
				fillSecondSlot(FluidUtil.drainStack(container, toDrain, false).getValue());
				inventory.decrStackSize(0, 1);
			}
		} else
		{
			// TODO fill container
		}
	}

	public ItemStack fillSecondSlot(ItemStack itemStack)
	{
		ItemStack secondSlot = inventory.getStackInSlot(1);
		if (secondSlot == null)
		{
			inventory.setInventorySlotContents(1, itemStack);
			return itemStack;
		} else
		{
			if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot))
				return null;
			return inventory.incrStackSize(1, itemStack.stackSize);
		}
	}
}

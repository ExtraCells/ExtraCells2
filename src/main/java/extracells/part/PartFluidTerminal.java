package extracells.part;

import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.PacketFluidTerminal;
import extracells.util.ECPrivateInventory;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
		rh.renderInventoryBox(renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture(Block.stone.getIcon(0, 0));
		rh.setBounds(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
		rh.renderBlock(x, y, z, renderer);
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{

	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{

	}

	@Override
	public void writeToStream(DataOutputStream data) throws IOException
	{

	}

	@Override
	public boolean readFromStream(DataInputStream data) throws IOException
	{
		return false;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F);
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 0;
	}

	public void setCurrentFluid(Fluid _currentFluid)
	{
		currentFluid = _currentFluid;
		for (ContainerFluidTerminal containerTerminalFluid : containers)
		{
			PacketDispatcher.sendPacketToPlayer(new PacketFluidTerminal(currentFluid).makePacket(), containerTerminalFluid.getPlayer());
		}
	}

	public void addContainer(ContainerFluidTerminal containerTerminalFluid)
	{
		containers.add(containerTerminalFluid);
		PacketDispatcher.sendPacketToPlayer(new PacketFluidTerminal(currentFluid).makePacket(), containerTerminalFluid.getPlayer());
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

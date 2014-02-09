package extracells.network.packet;

import appeng.api.AEApi;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.relauncher.Side;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.GuiFluidTerminal;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PacketFluidTerminal extends AbstractPacket
{
	IItemList<IAEFluidStack> fluidStackList;
	byte mode;
	Fluid currentFluid;
	PartFluidTerminal terminalFluid;

	public PacketFluidTerminal(IItemList<IAEFluidStack> _list)
	{
		mode = 0;
		fluidStackList = _list;
	}

	public PacketFluidTerminal()
	{
	}

	public PacketFluidTerminal(Fluid _currentFluid, PartFluidTerminal _terminalFluid)
	{
		mode = 1;
		currentFluid = _currentFluid;
		terminalFluid = _terminalFluid;
	}

	public PacketFluidTerminal(Fluid _currentFluid)
	{
		mode = 2;
		currentFluid = _currentFluid;
	}

	public PacketFluidTerminal(PartFluidTerminal _terminalFluid)
	{
		mode = 3;
		terminalFluid = _terminalFluid;
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeByte(mode);
		switch (mode)
		{
		case 0:
			out.writeInt(fluidStackList.size());
			for (IAEFluidStack stack : fluidStackList)
			{
				FluidStack fluidStack = stack.getFluidStack();
				System.out.println(fluidStack.getFluid().getUnlocalizedName());
				out.writeUTF(fluidStack.getFluid().getUnlocalizedName());
				out.writeLong(fluidStack.amount);
			}
			break;
		case 1:
			out.writeInt(terminalFluid.getHost().getTile().worldObj.provider.dimensionId);
			out.writeInt(terminalFluid.getHost().getTile().xCoord);
			out.writeInt(terminalFluid.getHost().getTile().yCoord);
			out.writeInt(terminalFluid.getHost().getTile().zCoord);
			out.writeByte(terminalFluid.getSide().ordinal());
			out.writeInt(currentFluid.getID());
			break;
		case 2:
			out.writeInt(currentFluid != null ? currentFluid.getID() : -1);
			break;
		case 3:
			out.writeInt(terminalFluid.getHost().getTile().worldObj.provider.dimensionId);
			out.writeInt(terminalFluid.getHost().getTile().xCoord);
			out.writeInt(terminalFluid.getHost().getTile().yCoord);
			out.writeInt(terminalFluid.getHost().getTile().zCoord);
			out.writeByte(terminalFluid.getSide().ordinal());
			break;
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException
	{
		mode = in.readByte();
		switch (mode)
		{
		case 0:
			fluidStackList = AEApi.instance().storage().createItemList();
			int length = in.readInt();
			for (int i = 0; i < length; i++)
			{
				Fluid fluid = FluidRegistry.getFluid(in.readUTF());
				long fluidAmount = in.readLong();
				if (fluid != null)
				{
					IAEFluidStack stack = AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1));
					stack.setStackSize(fluidAmount);
					fluidStackList.add(stack);
				}
			}
			break;
		case 1:
			terminalFluid = (PartFluidTerminal) ((IPartHost) DimensionManager.getWorld(in.readInt()).getBlockTileEntity(in.readInt(), in.readInt(), in.readInt())).getPart(ForgeDirection.getOrientation(in.readByte()));
			currentFluid = FluidRegistry.getFluid(in.readInt());
			break;
		case 2:
			int fluidID = in.readInt();
			currentFluid = fluidID > 0 ? FluidRegistry.getFluid(fluidID) : null;
			break;
		case 3:
			terminalFluid = (PartFluidTerminal) ((IPartHost) DimensionManager.getWorld(in.readInt()).getBlockTileEntity(in.readInt(), in.readInt(), in.readInt())).getPart(ForgeDirection.getOrientation(in.readByte()));
			break;
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException
	{
		switch (mode)
		{
		case 0:
			if (player != null && player.worldObj.isRemote)
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidTerminal)
				{
					ContainerFluidTerminal container = (ContainerFluidTerminal) ((GuiFluidTerminal) gui).inventorySlots;
					container.updateFluidList(fluidStackList);
				}
			}
			break;
		case 1:
			terminalFluid.setCurrentFluid(currentFluid);
			break;
		case 2:
			if (player != null && player.worldObj.isRemote)
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiFluidTerminal)
				{
					ContainerFluidTerminal container = (ContainerFluidTerminal) ((GuiFluidTerminal) gui).inventorySlots;
					container.setSelectedFluid(currentFluid);
				}
			}
			break;
		case 3:
			if (player.openContainer instanceof ContainerFluidTerminal)
				((ContainerFluidTerminal) player.openContainer).forceFluidUpdate();
			break;
		}
	}
}

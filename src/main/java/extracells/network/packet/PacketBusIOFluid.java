package extracells.network.packet;

import appeng.api.parts.IPartHost;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import extracells.gui.GuiBusIOFluid;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.List;

public class PacketBusIOFluid extends AbstractPacket
{
	private List<Fluid> filterFluids;
	private byte mode;
	private byte index;
	private Fluid fluid;
	private PartFluidIO part;
	private byte action;
	private byte ordinal;
	private byte filterSize;

	public PacketBusIOFluid()
	{
	}

	public PacketBusIOFluid(List<Fluid> _filterFluids)
	{
		mode = 0;
		filterFluids = _filterFluids;
	}

	public PacketBusIOFluid(byte _index, Fluid _toSet, PartFluidIO _part)
	{
		mode = 1;
		index = _index;
		fluid = _toSet;
		part = _part;
	}

	public PacketBusIOFluid(byte _action, PartFluidIO _part)
	{
		mode = 2;
		action = _action;
		part = _part;
	}

	public PacketBusIOFluid(byte _action, byte _ordinal)
	{
		mode = 3;
		action = _action;
		ordinal = _ordinal;
	}

	public PacketBusIOFluid(PartFluidIO _part)
	{
		mode = 4;
		part = _part;
	}

	public PacketBusIOFluid(byte _filterSize)
	{
		mode = 5;
		filterSize = _filterSize;
	}

	@Override
	public void write(ByteArrayDataOutput out)
	{
		out.writeByte(mode);
		switch (mode)
		{
		case 0:
			out.writeInt(filterFluids.size());
			for (Fluid fluid : filterFluids)
			{
				out.writeUTF(fluid == null ? "" : fluid.getName());
			}
			break;
		case 1:
			out.writeInt(part.getHost().getTile().worldObj.provider.dimensionId);
			out.writeInt(part.getHost().getTile().xCoord);
			out.writeInt(part.getHost().getTile().yCoord);
			out.writeInt(part.getHost().getTile().zCoord);
			out.writeByte(part.getSide().ordinal());
			out.writeByte(index);
			out.writeUTF(fluid == null ? "" : fluid.getName());
			break;
		case 2:
			out.writeInt(part.getHost().getTile().worldObj.provider.dimensionId);
			out.writeInt(part.getHost().getTile().xCoord);
			out.writeInt(part.getHost().getTile().yCoord);
			out.writeInt(part.getHost().getTile().zCoord);
			out.writeByte(part.getSide().ordinal());
			out.writeByte(action);
			break;
		case 3:
			out.writeByte(action);
			out.writeByte(ordinal);
			break;
		case 4:
			out.writeInt(part.getHost().getTile().worldObj.provider.dimensionId);
			out.writeInt(part.getHost().getTile().xCoord);
			out.writeInt(part.getHost().getTile().yCoord);
			out.writeInt(part.getHost().getTile().zCoord);
			out.writeByte(part.getSide().ordinal());
			break;
		case 5:
			out.writeByte(filterSize);
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
			filterFluids = new ArrayList<Fluid>();
			int length = in.readInt();
			for (int i = 0; i < length; i++)
				filterFluids.add(FluidRegistry.getFluid(in.readUTF()));
			break;
		case 1:
			part = (PartFluidIO) ((IPartHost) DimensionManager.getWorld(in.readInt()).getBlockTileEntity(in.readInt(), in.readInt(), in.readInt())).getPart(ForgeDirection.getOrientation(in.readByte()));
			index = in.readByte();
			fluid = FluidRegistry.getFluid(in.readUTF());
			break;
		case 2:
			part = (PartFluidIO) ((IPartHost) DimensionManager.getWorld(in.readInt()).getBlockTileEntity(in.readInt(), in.readInt(), in.readInt())).getPart(ForgeDirection.getOrientation(in.readByte()));
			action = in.readByte();
			break;
		case 3:
			action = in.readByte();
			ordinal = in.readByte();
			break;
		case 4:
			part = (PartFluidIO) ((IPartHost) DimensionManager.getWorld(in.readInt()).getBlockTileEntity(in.readInt(), in.readInt(), in.readInt())).getPart(ForgeDirection.getOrientation(in.readByte()));
			break;
		case 5:
			filterSize = in.readByte();
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
				if (gui instanceof GuiBusIOFluid)
				{
					GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
					partGui.updateFluids(filterFluids);
				}
			}
			break;
		case 1:
			part.setFilterFluid(index, fluid, player);
			break;
		case 2:
			switch (action)
			{
			case 0:
				part.loopRedstoneMode((Player) player);
				break;
			case 1:
				part.loopFluidMode((Player) player);
				break;
			}
			break;
		case 3:
			if (player != null && player.worldObj.isRemote)
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiBusIOFluid)
				{
					GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
					partGui.updateButtons(action, ordinal);
				}
			}
			break;
		case 4:
			part.sendInformation((Player) player);
			break;
		case 5:
			if (player != null && player.worldObj.isRemote)
			{
				Gui gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiBusIOFluid)
				{
					GuiBusIOFluid partGui = (GuiBusIOFluid) gui;
					partGui.changeConfig(filterSize);
				}
			}
			break;
		}
	}
}

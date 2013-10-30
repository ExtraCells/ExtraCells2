package extracells.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.primitives.Ints;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.me.tiles.IColoredMETile;
import appeng.api.me.tiles.IConnectionSensitive;
import cpw.mods.fml.common.network.PacketDispatcher;

public abstract class ColorableECTile extends TileEntity implements IConnectionSensitive, IColoredMETile
{
	private Set<ForgeDirection> connections;
	private Set<ForgeDirection> visualConnections;
	private int color = -1;

	public void writeToNBT(NBTTagCompound nbtTag)
	{
		super.writeToNBT(nbtTag);
		nbtTag.setInteger("Color", color);

		if (connections != null)
		{
			int[] connectionInts = new int[connections.size()];
			int counter = 0;
			for (ForgeDirection direction : connections)
			{
				connectionInts[counter] = direction.ordinal();
				counter++;
			}

			nbtTag.setIntArray("ValidDirections", connectionInts);
		}
		if (visualConnections != null)
		{
			int[] visualConnectionInts = new int[connections.size()];
			int counter = 0;
			for (ForgeDirection direction : visualConnections)
			{
				visualConnectionInts[counter] = direction.ordinal();
				counter++;
			}

			nbtTag.setIntArray("ValidVisualDirections", visualConnectionInts);
		}
	}

	public void readFromNBT(NBTTagCompound nbtTag)
	{
		super.readFromNBT(nbtTag);
		color = nbtTag.getInteger("Color");

		connections = new HashSet<ForgeDirection>();
		for (int directionInt : nbtTag.getIntArray("ValidDirections"))
		{
			connections.add(ForgeDirection.getOrientation(directionInt));
		}
		visualConnections = new HashSet<ForgeDirection>();
		for (int directionInt : nbtTag.getIntArray("ValidVisualDirections"))
		{
			visualConnections.add(ForgeDirection.getOrientation(directionInt));
		}
	}

	public void onMEConnectionsChanged(Set<ForgeDirection> connections, Set<ForgeDirection> visualConnections)
	{
		this.connections = connections;
		this.visualConnections = visualConnections;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public Set<ForgeDirection> getVisualConnections()
	{
		return this.visualConnections;
	}

	// AE 13
	public boolean isColored()
	{
		return color != -1;
	}

	// AE 14
	public boolean isColored(ForgeDirection input)
	{
		return isColored();
	}

	public void setColor(int offset)
	{
		color = offset;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public int getColor()
	{
		return color;
	}

	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);
	}
}

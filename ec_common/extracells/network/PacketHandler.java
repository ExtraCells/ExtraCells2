package extracells.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import extracells.tile.TileEntitySolderingStation;
import extracells.tile.TileEntityTerminalFluid;

public class PacketHandler implements IPacketHandler
{
	public static final String channel = "ExtraCells";

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
		EntityPlayer entityPlayer = (EntityPlayer) player;

		int x, y, z;

		byte packetId = in.readByte();

		switch (packetId)
		{
		case 0:
			String playerName = in.readUTF();
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			int size = in.readInt();
			int types = in.readInt();
			Boolean remove = in.readBoolean();
			char upgrade = in.readChar();
			char downgrade = in.readChar();

			if (!entityPlayer.worldObj.isRemote)
			{
				TileEntitySolderingStation tile = (TileEntitySolderingStation) entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				if (remove)
				{
					tile.remUser(playerName);
				} else
				{
					tile.updateData(playerName, size, types, upgrade, downgrade);
				}
			}
			break;

		case 1:
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			int action = in.readInt();

			if (!entityPlayer.worldObj.isRemote)
			{
				TileEntityTerminalFluid tile = (TileEntityTerminalFluid) entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				int currentFluid = tile.getCurrentFluid();
				switch (action)
				{
				case 0:
					tile.setCurrentFluid(-1);
					break;
				case 1:
					tile.setCurrentFluid(1);
					break;
				}
			}
			break;
		}

	}

	public static void sendSolderingStationPacket(String playerName, int x, int y, int z, int size, int types, boolean remove, char upgrade, char downgrade)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		try
		{
			dataStream.writeByte((byte) 0);
			dataStream.writeUTF(playerName);
			dataStream.writeInt(x);
			dataStream.writeInt(y);
			dataStream.writeInt(z);
			dataStream.writeInt(size);
			dataStream.writeInt(types);
			dataStream.writeBoolean(remove);
			dataStream.writeChar(upgrade);
			dataStream.writeChar(downgrade);

			PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(channel, byteStream.toByteArray()));
		} catch (IOException ex)
		{
			System.err.append("Failed to send packet");
		}
	}

	public static void sendMonitorFluidPacket(int x, int y, int z, int action)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		try
		{
			dataStream.writeByte((byte) 1);
			dataStream.writeInt(x);
			dataStream.writeInt(y);
			dataStream.writeInt(z);
			dataStream.writeInt(action);

			PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(channel, byteStream.toByteArray()));
		} catch (IOException ex)
		{
			System.err.append("Failed to send packet");
		}
	}

}

package extracells.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import appeng.api.config.RedstoneModeInput;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import extracells.tile.TileEntityBusFluidExport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityMEBattery;
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

		int x, y, z, action;
		String playerName;
		byte packetId = in.readByte();

		switch (packetId)
		{
		case 0:
			playerName = in.readUTF();
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
			action = in.readInt();

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

		case 2:
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			playerName = in.readUTF();

			if (!entityPlayer.worldObj.isRemote)
			{
				TileEntityMEBattery tile = (TileEntityMEBattery) entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				tile.updateGuiTile(playerName);
			}
			break;

		case 3:
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			action = in.readInt();
			playerName = in.readUTF();

			if (!entityPlayer.worldObj.isRemote)
			{
				TileEntityBusFluidImport tile = (TileEntityBusFluidImport) entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				switch (action)
				{
				case 0:
					tile.updateGuiTile(playerName);
					break;
				case 1:
					tile.toggleRedstoneAction(playerName);
					break;
				}
			}
			break;

		case 4:
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
			action = in.readInt();
			playerName = in.readUTF();

			if (!entityPlayer.worldObj.isRemote)
			{
				TileEntityBusFluidExport tile = (TileEntityBusFluidExport) entityPlayer.worldObj.getBlockTileEntity(x, y, z);
				switch (action)
				{
				case 0:
					PacketDispatcher.sendPacketToPlayer(tile.getDescriptionPacket(), player);
					break;
				case 1:
					if (tile.getRedstoneAction().ordinal() >= 3)
					{

						tile.setRedstoneAction(RedstoneModeInput.values()[0]);
					} else
					{
						tile.setRedstoneAction(RedstoneModeInput.values()[tile.getRedstoneAction().ordinal() + 1]);
					}
					PacketDispatcher.sendPacketToAllPlayers(tile.getDescriptionPacket());
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

	public static void sendBatteryPacket(int x, int y, int z, String playername)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		try
		{
			dataStream.writeByte((byte) 2);
			dataStream.writeInt(x);
			dataStream.writeInt(y);
			dataStream.writeInt(z);
			dataStream.writeUTF(playername);

			PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(channel, byteStream.toByteArray()));
		} catch (IOException ex)
		{
			System.err.append("Failed to send packet");
		}
	}

	public static void sendFluidImportBusPacket(int x, int y, int z, int action, String playername)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		try
		{
			dataStream.writeByte((byte) 3);
			dataStream.writeInt(x);
			dataStream.writeInt(y);
			dataStream.writeInt(z);
			dataStream.writeInt(action);
			dataStream.writeUTF(playername);

			PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(channel, byteStream.toByteArray()));
		} catch (IOException ex)
		{
			System.err.append("Failed to send packet");
		}
	}

	public static void sendFluidExportBusPacket(int x, int y, int z, int action, String playername)
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		try
		{
			dataStream.writeByte((byte) 4);
			dataStream.writeInt(x);
			dataStream.writeInt(y);
			dataStream.writeInt(z);
			dataStream.writeInt(action);
			dataStream.writeUTF(playername);

			PacketDispatcher.sendPacketToServer(PacketDispatcher.getPacket(channel, byteStream.toByteArray()));
		} catch (IOException ex)
		{
			System.err.append("Failed to send packet");
		}
	}
}

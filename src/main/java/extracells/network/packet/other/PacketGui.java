package extracells.network.packet.other;

import cpw.mods.fml.common.FMLCommonHandler;
import extracells.network.AbstractPacket;
import extracells.network.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.io.IOException;

public class PacketGui extends AbstractPacket
{
	private int ID;
	private World world;
	private int x, y, z;

	public PacketGui()
	{
	}

	public PacketGui(int _ID, EntityPlayer _player, World _world, int _x, int _y, int _z)
	{
		this(_ID, _player);
		world = _world;
		x = _x;
		y = _y;
		z = _z;
		mode = 0;
	}

	public PacketGui(int _ID, EntityPlayer _player)
	{
		ID = _ID;
		player = _player;
		mode = 1;
	}

	@Override
	public void writeData(ByteBuf out) throws IOException
	{
		switch (mode)
		{
		case 0:// Fallthrough intended
			writeWorld(world, out);
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(z);
		case 1:
			out.writeInt(ID);
			writePlayer(player, out);
		}
	}

	@Override
	public void readData(ByteBuf in) throws IOException
	{
		switch (mode)
		{
		case 0: // Fallthrough intended
			world = readWorld(in);
			x = in.readInt();
			y = in.readInt();
			z = in.readInt();
		case 1:
			ID = in.readInt();
			player = readPlayer(in);
		}
	}

	@Override
	public void execute()
	{
		switch (mode)
		{
		case 0:
			FMLCommonHandler.instance().showGuiScreen(GuiHandler.getClientGuiElement(ID, player, world, x, y, z));
			break;
		case 1:
			FMLCommonHandler.instance().showGuiScreen(GuiHandler.getClientGuiElement(ID, player));
			break;
		}
	}
}

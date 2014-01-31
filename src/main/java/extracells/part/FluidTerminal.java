package extracells.part;

import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FluidTerminal extends ECBasePart
{

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{

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
	public void removeFromWorld()
	{

	}

	@Override
	public void addToWorld()
	{

	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{

	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 0;
	}
}

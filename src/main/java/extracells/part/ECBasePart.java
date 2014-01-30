package extracells.part;

import appeng.api.AEApi;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import extracells.ItemEnum;
import extracells.item.ItemECBasePart;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public abstract class ECBasePart implements IPart, IGridHost
{
	protected int partId;
	protected IGridNode node;
	protected ForgeDirection side;
	protected IPartHost host;
	protected TileEntity tile;
	protected IGridNode externalNode;
	protected IGridBlock gridBlock;

	protected ECBasePart()
	{
		partId = ItemECBasePart.registerPart(this);
		gridBlock = createGridBlock();
		externalNode = AEApi.instance().createGridNode(gridBlock);
	}

	@Override
	public ItemStack getItemStack(PartItemStack type)
	{
		ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItemInstance(), 1);
		NBTTagCompound itemNbt = new NBTTagCompound();
		NBTTagCompound partNbt = new NBTTagCompound();
		writeToNBT(partNbt);
		itemNbt.setCompoundTag("partNbt", partNbt);
		itemNbt.setInteger("partId", partId);
		is.setTagCompound(partNbt);
		return is;
	}

	@Override
	public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public abstract void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public abstract void writeToNBT(NBTTagCompound data);

	@Override
	public abstract void readFromNBT(NBTTagCompound data);

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{
	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public abstract void writeToStream(DataOutputStream data) throws IOException;

	@Override
	public abstract boolean readFromStream(DataInputStream data) throws IOException;

	@Override
	public IGridNode getGridNode()
	{
		return node;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
	}

	@Override
	public abstract void removeFromWorld();

	@Override
	public abstract void addToWorld();

	@Override
	public final IGridNode getExternalFacingNode()
	{
		return externalNode;
	}

	public abstract IGridBlock createGridBlock();

	@Override
	public final void setPartHostInfo(ForgeDirection _side, IPartHost _host, TileEntity _tile)
	{
		side = _side;
		host = _host;
		tile = _tile;
	}

	@Override
	public abstract void getBoxes(IPartCollsionHelper bch);

	@Override
	public abstract boolean onActivate(EntityPlayer player, Vec3 pos);

	@Override
	public boolean onShiftActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public final void getDrops(List<ItemStack> drops, boolean wrenched)
	{
	}

	@Override
	public abstract int cableConnectionRenderTo();

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
	}

	@Override
	public final IGridNode getGridNode(ForgeDirection dir)
	{
		return node;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
	}

	public static void registerParts()
	{
		new FluidExport();
		new FluidStorage();
	}
}

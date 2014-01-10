package extracells.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.Blocks;
import appeng.api.Util;
import appeng.api.me.items.IMemoryCard;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.Extracells;
import extracells.tileentity.TileEntityBusFluidExport;

public class BlockBusFluidExport extends RotatableColorBlock
{

	public BlockBusFluidExport(int id)
	{
		super(id, Material.rock);
		setCreativeTab(extracells.Extracells.ModTab);
		setUnlocalizedName("block.fluid.bus.export");
		setHardness(2.0F);
		setResistance(10.0F);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityBusFluidExport();
	}

	public Icon getIcon(int side, int metadata)
	{
		return Blocks.blkInterface.getIconIndex();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID)
	{
		super.onNeighborBlockChange(world, x, y, z, neighborID);
		if (!world.isRemote)
		{
			if (world.getBlockTileEntity(x, y, z) instanceof TileEntityBusFluidExport)
				PacketDispatcher.sendPacketToAllPlayers(world.getBlockTileEntity(x, y, z).getDescriptionPacket());
			((TileEntityBusFluidExport) world.getBlockTileEntity(x, y, z)).setRedstoneStatus(world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z));
		}
		ForgeDirection blockOrientation = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
		TileEntity blockTE = world.getBlockTileEntity(x, y, z);
		if (blockTE instanceof TileEntityBusFluidExport)
		{
			TileEntity fluidHandler = world.getBlockTileEntity(x + blockOrientation.offsetX, y + blockOrientation.offsetY, z + blockOrientation.offsetZ);
			((TileEntityBusFluidExport) blockTE).setFluidHandler(fluidHandler instanceof IFluidHandler ? (IFluidHandler) fluidHandler : null);
		}
	}

	@Override
	protected void initializeBlock()
	{
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (!world.isRemote)
		{
			ItemStack currentItem = player.inventory.getCurrentItem();
			if (currentItem != null)
			{
				IMemoryCard card = Util.getAppEngApi().getMemoryCardHandler();
				TileEntity blockTE = world.getBlockTileEntity(x, y, z);

				if (card.isMemoryCard(currentItem))
				{
					if (player.isSneaking())
					{
						NBTTagCompound nbt = new NBTTagCompound();
						blockTE.writeToNBT(nbt);
						nbt.removeTag("x");
						nbt.removeTag("y");
						nbt.removeTag("z");
						blockTE.readFromNBT(nbt);
						card.setMemoryCardContents(currentItem, getUnlocalizedName() + ".name", nbt);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.SettingsSaved")));
						return true;
					} else if (card.getSettingsName(currentItem).equals(getUnlocalizedName() + ".name") || card.getSettingsName(currentItem).equals("AppEng.GuiITooltip.Blank"))
					{
						blockTE.readFromNBT(card.getData(currentItem));
						Packet description = blockTE.getDescriptionPacket();
						if (description != null)
							PacketDispatcher.sendPacketToAllPlayers(description);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.SettingsLoaded")));
						return true;
					} else
					{
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.IncorrectDevice")));
						return true;
					}
				}
			}
			if (world.getBlockTileEntity(x, y, z) == null || player.isSneaking())
			{
				return false;
			}
			player.openGui(Extracells.instance, 4, world, x, y, z);
		}
		return true;
	}
}

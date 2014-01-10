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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.Blocks;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridStorageUpdateEvent;
import appeng.api.me.items.IMemoryCard;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tileentity.TileEntityBusFluidStorage;

public class BlockBusFluidStorage extends RotatableColorBlock
{

	@SideOnly(Side.CLIENT)
	Icon frontIcon;
	@SideOnly(Side.CLIENT)
	Icon sideIcon;
	@SideOnly(Side.CLIENT)
	Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	Icon topIcon;

	public BlockBusFluidStorage(int id)
	{
		super(id, Material.rock);
		setCreativeTab(extracells.Extracells.ModTab);
		setUnlocalizedName("block.fluid.bus.storage");
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
		return new TileEntityBusFluidStorage();
	}

	public Icon getIcon(int side, int metadata)
	{
		return Blocks.blkInterface.getIconIndex();
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
			PacketDispatcher.sendPacketToPlayer(world.getBlockTileEntity(x, y, z).getDescriptionPacket(), (Player) player);
			player.openGui(Extracells.instance, 2, world, x, y, z);
		}
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighbourID)
	{
		TileEntity blockTE = world.getBlockTileEntity(x, y, z);
		if (blockTE instanceof TileEntityBusFluidStorage)
		{
			TileEntityBusFluidStorage storageBus = (TileEntityBusFluidStorage) blockTE;
			if (!world.isRemote)
			{
				storageBus.updateGrid();
				PacketDispatcher.sendPacketToAllPlayers(world.getBlockTileEntity(x, y, z).getDescriptionPacket());
				MinecraftForge.EVENT_BUS.post(new GridStorageUpdateEvent(world, new WorldCoord(x, y, z), storageBus.getGrid()));
			}
			ForgeDirection blockOrientation = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));

			TileEntity fluidHandler = world.getBlockTileEntity(x + blockOrientation.offsetX, y + blockOrientation.offsetY, z + blockOrientation.offsetZ);
			storageBus.setFluidHandler(fluidHandler instanceof IFluidHandler ? (IFluidHandler) fluidHandler : null);
		}
	}
}

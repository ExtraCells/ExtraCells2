package extracells.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Materials;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tile.TileEntityMonitorStorageFluid;

public class BlockMonitorStorageFluid extends RotatableColorBlock
{

	@SideOnly(Side.CLIENT)
	public Icon topIcon;
	@SideOnly(Side.CLIENT)
	public Icon sideIcon;
	@SideOnly(Side.CLIENT)
	public Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	public Icon baseLayer;
	@SideOnly(Side.CLIENT)
	public Icon[] colorLayers;

	public BlockMonitorStorageFluid(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.Extracells.ModTab);
		this.setUnlocalizedName("block.fluid.monitor.storage");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (!world.isRemote)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityMonitorStorageFluid)
			{
				TileEntityMonitorStorageFluid monitorTE = (TileEntityMonitorStorageFluid) te;
				ItemStack currItem = player.getCurrentEquippedItem();
				if (currItem != null && !monitorTE.isLocked())
				{
					if (currItem.isItemEqual(Materials.matConversionMatrix))
					{
						monitorTE.setMatrixed();
					} else if (currItem.getItem() instanceof IFluidContainerItem)
					{
						FluidStack fluid = ((IFluidContainerItem) currItem.getItem()).getFluid(currItem);
						monitorTE.setFluid(fluid != null ? fluid.getFluid() : null);
					} else if (FluidContainerRegistry.isFilledContainer(currItem))
					{
						FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(currItem);
						monitorTE.setFluid(fluid != null ? fluid.getFluid() : null);
					} else if (FluidContainerRegistry.isEmptyContainer(currItem))
					{
						monitorTE.setFluid(null);
					}
				} else if (player.isSneaking())
				{
					if (!monitorTE.isLocked())
					{
						monitorTE.setLocked(true);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.Locked")));
					} else
					{
						monitorTE.setLocked(false);
						player.sendChatToPlayer(new ChatMessageComponent().addText(StatCollector.translateToLocal("ChatMsg.Unlocked")));
					}
				} else if (!player.isSneaking() && !monitorTE.isLocked())
				{
					monitorTE.setFluid(null);
				}
			}
		}
		return true;
	}

	@Override
	public int getRenderType()
	{
		return Extracells.renderID;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMonitorStorageFluid();
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		this.sideIcon = iconregister.registerIcon("extracells:machine.side");
		this.bottomIcon = iconregister.registerIcon("extracells:machine.bottom");
		this.topIcon = iconregister.registerIcon("extracells:machine.top");
		this.baseLayer = iconregister.registerIcon("extracells:fluid.monitor.layerbase");
		colorLayers = new Icon[]
		{ iconregister.registerIcon("extracells:fluid.monitor.layer3"), iconregister.registerIcon("extracells:fluid.monitor.layer2"), iconregister.registerIcon("extracells:fluid.monitor.layer1") };
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return side == 3 ? baseLayer : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity tileentity = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);

		if (tileentity != null)
		{
			return side == metadata ? baseLayer : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
		}
		return null;
	}

}

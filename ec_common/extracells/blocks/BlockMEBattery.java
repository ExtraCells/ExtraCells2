package extracells.blocks;

import java.text.DecimalFormat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.tile.TileEntityMEBattery;

public class BlockMEBattery extends BlockContainer
{

	@SideOnly(Side.CLIENT)
	public Icon iconLow;
	@SideOnly(Side.CLIENT)
	public Icon iconMed;
	@SideOnly(Side.CLIENT)
	public Icon iconHi;

	public BlockMEBattery(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.extracells.ModTab);
		this.setUnlocalizedName("meBattery");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return iconHi;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		iconLow = iconregister.registerIcon("extracells:me_battery_low");
		iconMed = iconregister.registerIcon("extracells:me_battery_medium");
		iconHi = iconregister.registerIcon("extracells:me_battery_high");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMEBattery();
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (!world.isRemote)
		{
			Double energy = ((TileEntityMEBattery) world.getBlockTileEntity(x, y, z)).energy;
			Double maxEnergy = ((TileEntityMEBattery) world.getBlockTileEntity(x, y, z)).maxEnergy;
			if (energy > maxEnergy)
			{
				p.addChatMessage("Current Energy: " + new DecimalFormat("#").format(maxEnergy));
			} else
			{
				p.addChatMessage("Current Energy: " + new DecimalFormat("#").format(energy));
			}
			p.addChatMessage("Max Energy: " + new DecimalFormat("#").format(maxEnergy));
		}
		return true;
	}
}

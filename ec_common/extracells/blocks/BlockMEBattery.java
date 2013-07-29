package extracells.blocks;

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
		return blockIcon;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		this.blockIcon = iconregister.registerIcon("extracells:me_battery");
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
			p.addChatMessage("Current Energy: " + Float.toString(((TileEntityMEBattery) world.getBlockTileEntity(x, y, z)).energy));
			p.addChatMessage("Max Energy: " + Float.toString(((TileEntityMEBattery) world.getBlockTileEntity(x, y, z)).maxEnergy));
		}
		return true;
	}
}

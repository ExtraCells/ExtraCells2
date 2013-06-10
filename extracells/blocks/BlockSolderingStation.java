package extracells.blocks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import extracells.tile.TileEntitySolderingStation;
import extracells.extracells;

public class BlockSolderingStation extends BlockContainer {
    
    private TileEntitySolderingStation tile;

    public BlockSolderingStation(int id, Material par2Material) {
        super(id, Material.rock);
        this.setCreativeTab(extracells.ModTab);
        this.setUnlocalizedName("SolderingStation");
        this.setHardness(2.0F);
        this.setResistance(6000F);
        this.setLightValue(0.02f);
        this.setBlockBounds(0F, 0F, 0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * How many does it drop when broken...
     */
    @Override
    public int quantityDropped(Random par1Random) {
        return 1;
    }
    
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLiving par5EntityLiving, ItemStack par6ItemStack)
    {
        int l = MathHelper.floor_double((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int i1 = par1World.getBlockMetadata(par2, par3, par4) >> 2;
        ++l;
        l %= 4;
        par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
    }
    
    /**
     * Obviously it's not an opaque cube.
     */
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /**
     * Block does not render as Normal blocks it has a Model...
     */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /**
     * So that the texture does show up correct.
     */
    @Override
    public int getRenderType() {
        return -2;
    }

    /**
     * Creates a new TileEntity instance.
     */
    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntitySolderingStation();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float a, float b, float c){
		if(!world.isRemote){
			player.openGui(extracells.instance, 0, world, x, y, z);
			return true;
		}else
		{
			return false;
		}
    }
}

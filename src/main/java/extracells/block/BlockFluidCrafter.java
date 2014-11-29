package extracells.block;

import java.util.Random;

import appeng.api.networking.IGridNode;
import extracells.Extracells;
import extracells.network.GuiHandler;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidCrafter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockFluidCrafter extends BlockContainer{

	IIcon icon;
	
	public BlockFluidCrafter() {
		super(Material.iron);
		setCreativeTab(Extracells.ModTab);
        setHardness(2.0F);
        setResistance(10.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFluidCrafter();
	}
	
	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int p_149714_5_) {
		if(world.isRemote)
			return;
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile != null){
			if(tile instanceof TileEntityFluidCrafter){
				((TileEntityFluidCrafter) tile).getGridNode().updateState();
			}
		}
	}
	
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if(world.isRemote)
			return;
		TileEntity tile  = world.getTileEntity(x, y, z);
		if(tile != null){
			if(tile instanceof TileEntityFluidCrafter){
				IGridNode  node =((TileEntityFluidCrafter) tile).getGridNode();
				if(node != null){
					node.destroy();
				}
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
		if(world.isRemote)
			return false;
		GuiHandler.launchGui(0, player, world, x, y, z);
		return true;
    }
	
	@Override
    public void breakBlock(World world, int x, int y, int z, Block par5, int par6) {
            dropItems(world, x, y, z);
            super.breakBlock(world, x, y, z, par5, par6);
    }

    private void dropItems(World world, int x, int y, int z){
            Random rand = new Random();

            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (!(tileEntity instanceof IInventory)) {
                    return;
            }
            IInventory inventory = (IInventory) tileEntity;

            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack item = inventory.getStackInSlot(i);

                    if (item != null && item.stackSize > 0) {
                            float rx = rand.nextFloat() * 0.8F + 0.1F;
                            float ry = rand.nextFloat() * 0.8F + 0.1F;
                            float rz = rand.nextFloat() * 0.8F + 0.1F;

                            EntityItem entityItem = new EntityItem(world,
                                            x + rx, y + ry, z + rz,
                                            item);

                            if (item.hasTagCompound()) {
                                    entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
                            }

                            float factor = 0.05F;
                            entityItem.motionX = rand.nextGaussian() * factor;
                            entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                            entityItem.motionZ = rand.nextGaussian() * factor;
                            world.spawnEntityInWorld(entityItem);
                            item.stackSize = 0;
                    }
            }
    }
    
    @Override
    public IIcon getIcon(int side, int b) {
    	return icon;
    }
    
    @Override
    public void registerBlockIcons(IIconRegister iconregister) {
    	icon = iconregister.registerIcon("extracells:fluid.crafter");
    }
    
    @Override
    public String getLocalizedName() {
        return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName().replace("tile.", "");
    }

}

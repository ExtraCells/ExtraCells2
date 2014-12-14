package extracells.block;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import appeng.api.AEApi;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridNode;
import extracells.Extracells;
import extracells.api.IECTileEntity;
import extracells.network.GuiHandler;
import extracells.tileentity.TileEntityFluidCrafter;
import extracells.tileentity.TileEntityFluidInterface;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ECBaseBlock extends BlockContainer {

	public ECBaseBlock() {
		super(Material.iron);
		setCreativeTab(Extracells.ModTab);
        setHardness(2.0F);
        setResistance(10.0F);
	}
	
	private IIcon[] icons = new IIcon[1];

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch(meta){
			case 0:
				return new TileEntityFluidInterface();
			default:
				return null;
		}
		
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if(world.isRemote)
			return;
		switch(world.getBlockMetadata(x, y, z)){
			case 0:
				TileEntity tile = world.getTileEntity(x, y, z);
				if(tile != null){
					if(tile instanceof IECTileEntity){
						IGridNode node = ((IECTileEntity) tile).getGridNode(ForgeDirection.UNKNOWN);
						if(entity != null && entity instanceof EntityPlayer){
							EntityPlayer player = (EntityPlayer) entity;
							node.setPlayerID(AEApi.instance().registries().players().getID(player));
						}
						node.updateState();
					}
				}
				return;
			default:
				return;
		}
	}
	
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if(world.isRemote)
			return;
		switch(meta){
			case 0:
				TileEntity tile  = world.getTileEntity(x, y, z);
				if(tile != null){
					if(tile instanceof IECTileEntity){
						IGridNode  node =((IECTileEntity) tile).getGridNode(ForgeDirection.UNKNOWN);
						if(node != null){
							node.destroy();
						}
					}
				}
				return;
			default:
				return;
		}
		
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
		if(world.isRemote)
			return false;
		ItemStack current = player.getCurrentEquippedItem();
		if (player.isSneaking() && current != null) {
        	try{
        		if(current.getItem() instanceof IToolWrench && ((IToolWrench)current.getItem()).canWrench(player, x, y, z)){
        			ItemStack block = new ItemStack(this);
        			TileEntity tile = world.getTileEntity(x, y, z);
        			if(tile != null && tile instanceof TileEntityFluidInterface){
        				block.setTagCompound(((TileEntityFluidInterface)tile).writeFilter(new NBTTagCompound()));
        			}
        			dropBlockAsItem(world, x, y, z, block);
                    world.setBlockToAir(x, y, z);
                    ((IToolWrench)current.getItem()).wrenchUsed(player, x, y, z);
                    return true;
        		}
        	}catch(Exception e){
        		//No IToolWrench
        	}
        	if(current.getItem() instanceof IAEWrench && ((IAEWrench)current.getItem()).canWrench(current, player, x, y, z)){
        		ItemStack block = new ItemStack(this);
    			TileEntity tile = world.getTileEntity(x, y, z);
    			if(tile != null && tile instanceof TileEntityFluidInterface){
    				block.setTagCompound(((TileEntityFluidInterface)tile).writeFilter(new NBTTagCompound()));
    			}
    			dropBlockAsItem(world, x, y, z, block);
                world.setBlockToAir(x, y, z);
                return true;
        	}
            
        }
		switch(world.getBlockMetadata(x, y, z)){
			case 0:
				GuiHandler.launchGui(0, player, world, x, y, z);
				return true;
			default:
				return false;
		}
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register)
    {
    	icons[0] = register.registerIcon("extracells:fluid.interface");
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta){
		if(meta >= 0 && meta + 1 <= icons.length){
			return icons[meta];
		}
        return null;
	}

}

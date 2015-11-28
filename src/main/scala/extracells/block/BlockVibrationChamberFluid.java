package extracells.block;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerVibrationChamberFluid;
import extracells.gui.GuiVibrationChamberFluid;
import extracells.network.GuiHandler;
import extracells.tileentity.TileEntityVibrationChamberFluid;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockVibrationChamberFluid extends BlockEC implements TGuiBlock {

    private IIcon[] icons = new IIcon[3];

    public BlockVibrationChamberFluid(){
        super(Material.iron, 2.0F, 10.0F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (world.isRemote)
            return false;
        GuiHandler.launchGui(0, player, world, x, y, z);
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVibrationChamberFluid();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister IIconRegister) {
        icons[0] = IIconRegister.registerIcon("extracells:VibrationChamberFluid");
        icons[1] = IIconRegister.registerIcon("extracells:VibrationChamberFluidFront");
        icons[2] = IIconRegister.registerIcon("extracells:VibrationChamberFluidFrontOn");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        if(side == world.getBlockMetadata(x, y, z)){
            TileEntity tile = world.getTileEntity(x,y, z);
            if(!(tile instanceof  TileEntityVibrationChamberFluid))
                return icons[0];
            TileEntityVibrationChamberFluid chamberFluid = (TileEntityVibrationChamberFluid) tile;
            if (chamberFluid.getBurnTime() > 0 && chamberFluid.getBurnTime() < (chamberFluid.getBurnTimeTotal()))
                return icons[2];
            else
                return icons[1];
        }else
            return icons[0];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta){
        switch (side)
        {
            case 4:
                return icons[1];
            default:
                return icons[0];
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z){
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity != null && tileEntity instanceof  TileEntityVibrationChamberFluid)
        return new GuiVibrationChamberFluid(player, (TileEntityVibrationChamberFluid)tileEntity);
        return null;
    }

    @Override
    public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z){
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(tileEntity != null && tileEntity instanceof  TileEntityVibrationChamberFluid)
            return new ContainerVibrationChamberFluid(player.inventory, (TileEntityVibrationChamberFluid)tileEntity);
        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        if(world == null)
            return;

        if(entity != null){
            int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            if (!entity.isSneaking())
            {
                if (l == 0)
                {
                    world.setBlockMetadataWithNotify(x, y, z, 2, 2);
                }

                if (l == 1)
                {
                    world.setBlockMetadataWithNotify(x, y, z, 5, 2);
                }

                if (l == 2)
                {
                    world.setBlockMetadataWithNotify(x, y, z, 3, 2);
                }

                if (l == 3)
                {
                    world.setBlockMetadataWithNotify(x, y, z, 4, 2);
                }
            } else
            {
                if (l == 0)
                {
                    world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(2).getOpposite().ordinal(), 2);
                }

                if (l == 1)
                {
                    world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(5).getOpposite().ordinal(), 2);
                }

                if (l == 2)
                {
                    world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(3).getOpposite().ordinal(), 2);
                }

                if (l == 3)
                {
                    world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(4).getOpposite().ordinal(), 2);
                }
            }
        }else
             world.setBlockMetadataWithNotify(x, y, z, 2, 2);

        if (world.isRemote)
            return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            if (tile instanceof TileEntityVibrationChamberFluid) {
                IGridNode node = ((TileEntityVibrationChamberFluid) tile).getGridNodeWithoutUpdate();
                if (entity != null && entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    node.setPlayerID(AEApi.instance().registries().players()
                            .getID(player));
                }
                node.updateState();
            }
        }
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
        if (world.isRemote)
            return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null) {
            if (tile instanceof TileEntityVibrationChamberFluid) {
                IGridNode node = ((TileEntityVibrationChamberFluid) tile).getGridNode(ForgeDirection.UNKNOWN);
                if (node != null) {
                    node.destroy();
                }
            }
        }
    }

}

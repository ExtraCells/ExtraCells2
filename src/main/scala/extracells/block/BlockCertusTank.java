package extracells.block;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IAEWrench;
import extracells.block.properties.PropertyFluid;
import extracells.models.IStateMapperRegister;
import extracells.network.ChannelHandler;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityCertusTank;

public class BlockCertusTank extends BlockEC implements IStateMapperRegister {

	public static final PropertyFluid FLUID = new PropertyFluid("fluid");
	public static final PropertyBool EMPTY = PropertyBool.create("empty");
	public static final PropertyBool TANK_ABOVE = PropertyBool.create("above");
	public static final PropertyBool TANK_BELOW = PropertyBool.create("below");
	public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);

	public BlockCertusTank() {
		super(Material.GLASS, 2.0F, 10.0F);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityCertusTank();
	}

	@Override
	public String getLocalizedName() {
		return I18n.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replace("tile.", "");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack current = player.inventory.getCurrentItem();

		if (player.isSneaking() && current != null) {
			//TODO: BuildCraft
			/*try {
				if (current.getItem() instanceof IToolWrench
					&& ((IToolWrench) current.getItem()).canWrench(
					entityplayer, x, y, z)) {
					dropBlockAsItem(worldObj, x, y, z,
						getDropWithNBT(worldObj, x, y, z));
					worldObj.setBlockToAir(x, y, z);
					((IToolWrench) current.getItem()).wrenchUsed(entityplayer,
						x, y, z);
					return true;
				}
			} catch (Throwable e) {
				// No IToolWrench
			}*/
			if (current.getItem() instanceof IAEWrench
				&& ((IAEWrench) current.getItem()).canWrench(current,
				player, pos)) {
				spawnAsEntity(world, pos, getDropWithNBT(world, pos));
				world.setBlockToAir(pos);
				return true;
			}

		}

		if (!player.isSneaking()) {
			if (interactWithFluidHandler(current, world, pos, side, player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity != null && tileEntity instanceof TileEntityCertusTank){
			TileEntityCertusTank certusTank = (TileEntityCertusTank) tileEntity;
			FluidStack fluidStack = certusTank.tank.getFluid();
			if(fluidStack != null) {
				extendedBlockState = extendedBlockState.withProperty(FLUID, fluidStack);
			}
		}
		return extendedBlockState;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)){
			IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
			FluidStack fluidStack = fluidHandler.getTankProperties()[0].getContents();
			state = state.withProperty(EMPTY, fluidStack == null);
		}
		TileEntity tileAbove = world.getTileEntity(pos.up());
		TileEntity tileBelow = world.getTileEntity(pos.down());
		return state.withProperty(TANK_ABOVE, tileAbove instanceof TileEntityCertusTank).withProperty(TANK_BELOW, tileBelow instanceof TileEntityCertusTank);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{EMPTY, TANK_ABOVE, TANK_BELOW}, new IUnlistedProperty[]{FLUID});
	}

	public ItemStack getDropWithNBT(World world, BlockPos pos) {
		TileEntity worldTE = world.getTileEntity(pos);
		if (worldTE != null && worldTE instanceof TileEntityCertusTank) {
			ItemStack dropStack = new ItemStack(BlockEnum.CERTUSTANK.getBlock());
			IFluidHandler fluidHandler = FluidUtil.getFluidHandler(dropStack);
			FluidStack fluidStack = ((TileEntityCertusTank) worldTE).tank.getFluid();
			if(fluidStack != null) {
				fluidHandler.fill(fluidStack, true);
			}
			return dropStack;
		}
		return null;
	}

	//TODO: 1.12 use FluidHandler#interactWithFluidHandler
	private static boolean interactWithFluidHandler(ItemStack currentItem, World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(pos);

		IFluidHandler blockFluidHandler = FluidUtil.getFluidHandler(world, pos, side);
		return blockFluidHandler != null && FluidUtil.interactWithFluidHandler(currentItem, blockFluidHandler, player);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		if (!world.isRemote) {

			ChannelHandler.sendPacketToAllPlayers(world.getTileEntity(pos).getUpdatePacket(), world);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerStateMapper() {
		//ModelLoader.setCustomStateMapper(this, new StateMap.Builder().build());
	}
}
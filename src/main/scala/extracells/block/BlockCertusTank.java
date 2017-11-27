package extracells.block;

import com.google.common.base.Preconditions;


import extracells.api.IWrenchHandler;
import extracells.util.WrenchUtil;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.block.properties.PropertyFluid;
import extracells.models.IStateMapperRegister;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityCertusTank;
import extracells.util.TileUtil;

public class BlockCertusTank extends BlockEC implements IStateMapperRegister {

	public static final PropertyFluid FLUID = new PropertyFluid("fluid");
	public static final PropertyBool EMPTY = PropertyBool.create("empty");
	public static final PropertyBool TANK_ABOVE = PropertyBool.create("above");
	public static final PropertyBool TANK_BELOW = PropertyBool.create("below");
	public static final PropertyFluid FLUID_ABOVE = new PropertyFluid("fluid_above");
	public static final PropertyFluid FLUID_BELOW = new PropertyFluid("fluid_below");
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
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack current = player.inventory.getCurrentItem();

		if (player.isSneaking()) {
			RayTraceResult rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
			IWrenchHandler wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand);
			if (wrenchHandler != null) {
				spawnAsEntity(world, pos, getDropWithNBT(world, pos));
				world.setBlockToAir(pos);
				wrenchHandler.wrenchUsed(current, player, rayTraceResult, hand);
				return true;
			}

		}

		if (!player.isSneaking()) {
			if (interactWithFluidHandler(hand, world, pos, side, player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack dropStack = new ItemStack(BlockEnum.CERTUSTANK.getBlock());
		TileEntityCertusTank tileEntityCertusTank = TileUtil.getTile(world, pos, TileEntityCertusTank.class);
		if (tileEntityCertusTank == null) {
			return dropStack;
		}
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(dropStack);
		FluidStack fluidStack = tileEntityCertusTank.tank.getFluid();
		if (fluidStack != null) {
			fluidHandler.fill(fluidStack, true);
		}
		return dropStack;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileEntityCertusTank) {
			TileEntityCertusTank certusTank = (TileEntityCertusTank) tileEntity;
			FluidStack fluidStack = certusTank.tank.getFluid();
			if (fluidStack != null) {
				extendedBlockState = extendedBlockState.withProperty(FLUID, fluidStack);
			}

		}
		//ABOVE
		if(extendedBlockState.getValue(TANK_ABOVE)) {
			tileEntity = world.getTileEntity(pos.up());
			if (tileEntity != null && tileEntity instanceof TileEntityCertusTank) {
				TileEntityCertusTank certusTank = (TileEntityCertusTank) tileEntity;
				FluidStack fluidStack = certusTank.tank.getFluid();
				if (fluidStack != null) {
					extendedBlockState = extendedBlockState.withProperty(FLUID_ABOVE, fluidStack);
				}

			}
		}
		//BELOW
		if(extendedBlockState.getValue(TANK_BELOW)) {
			tileEntity = world.getTileEntity(pos.down());
			if (tileEntity != null && tileEntity instanceof TileEntityCertusTank) {
				TileEntityCertusTank certusTank = (TileEntityCertusTank) tileEntity;
				FluidStack fluidStack = certusTank.tank.getFluid();
				if (fluidStack != null) {
					extendedBlockState = extendedBlockState.withProperty(FLUID_BELOW, fluidStack);
				}

			}
		}
		return extendedBlockState;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileEntityCertusTank) {
			FluidStack fluidStack = ((TileEntityCertusTank)tileEntity).tank.getFluid();
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
		return new ExtendedBlockState(this, new IProperty[]{EMPTY, TANK_ABOVE, TANK_BELOW}, new IUnlistedProperty[]{FLUID, FLUID_ABOVE, FLUID_BELOW});
	}

	public ItemStack getDropWithNBT(World world, BlockPos pos) {
		TileEntity worldTE = world.getTileEntity(pos);
		if (worldTE != null && worldTE instanceof TileEntityCertusTank) {
			ItemStack dropStack = new ItemStack(BlockEnum.CERTUSTANK.getBlock());
			IFluidHandler fluidHandler = FluidUtil.getFluidHandler(dropStack);
			FluidStack fluidStack = ((TileEntityCertusTank) worldTE).tank.getFluid();
			if (fluidStack != null) {
				fluidHandler.fill(fluidStack, true);
			}
			return dropStack;
		}
		return null;
	}

	//TODO: 1.12 use FluidHandler#interactWithFluidHandler
	private static boolean interactWithFluidHandler(EnumHand hand, World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(pos);

		IFluidHandler blockFluidHandler = FluidUtil.getFluidHandler(world, pos, side);

		return blockFluidHandler != null && FluidUtil.interactWithFluidHandler(player, hand, blockFluidHandler);
	}

	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos pos, BlockPos neighbor) {
		World world = (World) blockAccess;
		if (!world.isRemote) {

			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 0);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerStateMapper() {
		//ModelLoader.setCustomStateMapper(this, new StateMap.Builder().build());
	}
}
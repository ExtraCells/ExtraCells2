/*package extracells.block;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import extracells.network.ChannelHandler;
import extracells.registries.BlockEnum;
import extracells.render.RenderHandler;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockCertusTank extends BlockEC {

	IIcon breakIcon;
	IIcon topIcon;
	IIcon bottomIcon;
	IIcon sideIcon;
	IIcon sideMiddleIcon;
	IIcon sideTopIcon;
	IIcon sideBottomIcon;

	public BlockCertusTank() {
		super(Material.glass, 2.0F, 10.0F);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	@Override
	public boolean canRenderInPass(int pass) {
		RenderHandler.renderPass = pass;
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityCertusTank();
	}



	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replace("tile.", "");
	}



	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z,
			EntityPlayer entityplayer, int blockID, float offsetX,
			float offsetY, float offsetZ) {
		ItemStack current = entityplayer.inventory.getCurrentItem();

		if (entityplayer.isSneaking() && current != null) {
			try {
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
			}
			if (current.getItem() instanceof IAEWrench
					&& ((IAEWrench) current.getItem()).canWrench(current,
							entityplayer, x, y, z)) {
				dropBlockAsItem(worldObj, x, y, z,
						getDropWithNBT(worldObj, x, y, z));
				worldObj.setBlockToAir(x, y, z);
				return true;
			}

		}
		if (current != null) {
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
			TileEntityCertusTank tank = (TileEntityCertusTank) worldObj.getTileEntity(x, y, z);

			if (liquid != null) {
				int amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

				if (amountFilled != 0
						&& !entityplayer.capabilities.isCreativeMode) {
					if (current.stackSize > 1) {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
						entityplayer.inventory.addItemStackToInventory(current.getItem().getContainerItem(current));
					} else {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = current.getItem().getContainerItem(current);
					}
				}

				return true;

				// Handle empty containers
			} else {

				FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
				if (available != null) {
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

					liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

					if (liquid != null) {
						if (!entityplayer.capabilities.isCreativeMode) {
							if (current.stackSize > 1) {
								if (!entityplayer.inventory.addItemStackToInventory(filled)) {
									return false;
								} else {
									entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
								}
							} else {
								entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = filled;
							}
						}
						tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
			Block neighborBlock) {
		if (!world.isRemote) {

			ChannelHandler.sendPacketToAllPlayers(world.getTileEntity(x, y, z).getDescriptionPacket(), world);
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
		this.breakIcon = iconregister.registerIcon("extracells:certustank");
		this.topIcon = iconregister.registerIcon("extracells:CTankTop");
		this.bottomIcon = iconregister.registerIcon("extracells:CTankBottom");
		this.sideIcon = iconregister.registerIcon("extracells:CTankSide");
		this.sideMiddleIcon = iconregister.registerIcon("extracells:CTankSideMiddle");
		this.sideTopIcon = iconregister.registerIcon("extracells:CTankSideTop");
		this.sideBottomIcon = iconregister.registerIcon("extracells:CTankSideBottom");
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
*/
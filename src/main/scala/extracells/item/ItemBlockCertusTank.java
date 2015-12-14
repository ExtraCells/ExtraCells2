package extracells.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.List;

public class ItemBlockCertusTank extends ItemBlock implements
		IFluidContainerItem {

	private final int capacity = 32000;

	public ItemBlockCertusTank(Block block) {
		super(block);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list,
			boolean par4) {
		if (stack != null && stack.hasTagCompound()) {
			if (FluidStack.loadFluidStackFromNBT(stack.getTagCompound()
					.getCompoundTag("tileEntity")) != null)
				list.add(FluidStack.loadFluidStackFromNBT(stack
						.getTagCompound().getCompoundTag("tileEntity")).amount
						+ "mB");
		}
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		if (container.stackTagCompound == null
				|| !container.stackTagCompound.hasKey("tileEntity")
				|| container.stackTagCompound.getCompoundTag("tileEntity")
						.hasKey("Empty")) {
			return null;
		}

		FluidStack stack = FluidStack
				.loadFluidStackFromNBT(container.stackTagCompound
						.getCompoundTag("tileEntity"));
		if (stack == null) {
			return null;
		}

		int currentAmount = stack.amount;
		stack.amount = Math.min(stack.amount, maxDrain);
		if (doDrain) {
			if (currentAmount == stack.amount) {
				container.stackTagCompound.removeTag("tileEntity");

				if (container.stackTagCompound.hasNoTags()) {
					container.stackTagCompound = null;
				}
				return stack;
			}

			NBTTagCompound fluidTag = container.stackTagCompound
					.getCompoundTag("tileEntity");
			fluidTag.setInteger("Amount", currentAmount - stack.amount);
			container.stackTagCompound.setTag("tileEntity", fluidTag);
		}
		return stack;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		if (resource == null) {
			return 0;
		}

		if (!doFill) {
			if (container.stackTagCompound == null
					|| !container.stackTagCompound.hasKey("tileEntity")) {
				return Math.min(this.capacity, resource.amount);
			}

			FluidStack stack = FluidStack
					.loadFluidStackFromNBT(container.stackTagCompound
							.getCompoundTag("tileEntity"));

			if (stack == null) {
				return Math.min(this.capacity, resource.amount);
			}

			if (!stack.isFluidEqual(resource)) {
				return 0;
			}

			return Math.min(this.capacity - stack.amount, resource.amount);
		}

		if (container.stackTagCompound == null) {
			container.stackTagCompound = new NBTTagCompound();
		}

		if (!container.stackTagCompound.hasKey("tileEntity")
				|| container.stackTagCompound.getCompoundTag("tileEntity")
						.hasKey("Empty")) {
			NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

			if (this.capacity < resource.amount) {
				fluidTag.setInteger("Amount", this.capacity);
				container.stackTagCompound.setTag("tileEntity", fluidTag);
				return this.capacity;
			}

			container.stackTagCompound.setTag("tileEntity", fluidTag);
			return resource.amount;
		}

		NBTTagCompound fluidTag = container.stackTagCompound
				.getCompoundTag("tileEntity");
		FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

		if (!stack.isFluidEqual(resource)) {
			return 0;
		}

		int filled = this.capacity - stack.amount;
		if (resource.amount < filled) {
			stack.amount += resource.amount;
			filled = resource.amount;
		} else {
			stack.amount = this.capacity;
		}

		container.stackTagCompound.setTag("tileEntity",
				stack.writeToNBT(fluidTag));
		return filled;
	}

	@Override
	public int getCapacity(ItemStack container) {
		return this.capacity;
	}

	@Override
	public FluidStack getFluid(ItemStack container) {
		if (container.stackTagCompound == null
				|| !container.stackTagCompound.hasKey("tileEntity")) {
			return null;
		}
		return FluidStack.loadFluidStackFromNBT(container.stackTagCompound
				.getCompoundTag("tileEntity"));
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		if (itemstack != null) {
			if (itemstack.hasTagCompound()) {
				try {
					FluidStack fluidInTank = FluidStack
							.loadFluidStackFromNBT(itemstack.getTagCompound()
									.getCompoundTag("tileEntity"));

					if (fluidInTank != null && fluidInTank.getFluid() != null) {
						return StatCollector
								.translateToLocal(getUnlocalizedName(itemstack))
								+ " - "
								+ fluidInTank.getFluid().getLocalizedName(
										fluidInTank);
					}
				} catch (Throwable ignored) {}
			}
			return StatCollector
					.translateToLocal(getUnlocalizedName(itemstack));
		}
		return "";
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player,
			World world, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ, int metadata) {
		if (!world.setBlock(x, y, z, this.field_150939_a, metadata, 3)) {
			return false;
		}

		if (world.getBlock(x, y, z) == this.field_150939_a) {
			this.field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
			this.field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
		}

		if (stack != null && stack.hasTagCompound()) {
			((TileEntityCertusTank) world.getTileEntity(x, y, z))
					.readFromNBTWithoutCoords(stack.getTagCompound()
							.getCompoundTag("tileEntity"));
		}
		return true;
	}
}
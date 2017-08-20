package extracells.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import extracells.item.storage.ItemStorageCellPhysical;

public enum EnumBlockContainerMode {
	PLACE{
		@Override
		public void useMode(ItemStorageCellPhysical storagePhysical, ItemStack itemStack, IAEItemStack storageStack, IAEItemStack request, World world, BlockPos pos, EntityPlayer player, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ) {
			super.useMode(storagePhysical, itemStack, storageStack, request, world, pos, player, side, hand, hitX, hitY, hitZ);
			request.setStackSize(1);
			ItemBlock itemblock = (ItemBlock) itemStack.getItem();
			itemblock.onItemUseFirst(request.getItemStack(), player, world, pos, side, hitX, hitY, hitZ, hand);
			itemblock.onItemUse(request.getItemStack(), player, world, pos, hand, side, hitX, hitY, hitZ);
			storagePhysical.extractAEPower(player.getHeldItem(hand), 20.0D);
		}
	},
	TRADE{
		@Override
		public void useMode(ItemStorageCellPhysical storagePhysical, ItemStack itemStack, IAEItemStack storageStack, IAEItemStack request, World world, BlockPos pos, EntityPlayer player, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ) {
			super.useMode(storagePhysical, itemStack, storageStack, request, world, pos, player, side, hand, hitX, hitY, hitZ);
			request.setStackSize(1);
			world.destroyBlock(pos, true);
			placeBlock(storagePhysical, request.getItemStack(), world, player, pos, side, hand, hitX, hitY, hitZ);
		}
	},
	TRADE_BIG{
		@Override
		public void useMode(ItemStorageCellPhysical storagePhysical, ItemStack itemStack, IAEItemStack storageStack, IAEItemStack request, World world, BlockPos pos, EntityPlayer player, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ) {
			super.useMode(storagePhysical, itemStack, storageStack, request, world, pos, player, side, hand, hitX, hitY, hitZ);
			request.setStackSize(9);
			if (storageStack.getStackSize() > 9 && storagePhysical.getAECurrentPower(itemStack) >= 180.0D) {
				switch (side) {
					case NORTH:
					case SOUTH:
						placeBlocks(storagePhysical, request.getItemStack(), world, player, pos, side, hand, hitX, hitY, hitZ, new BlockPos(-1, -1, 0), new BlockPos(1, 1, 0));
						break;
					case DOWN:
					case UP:
						placeBlocks(storagePhysical, request.getItemStack(), world, player, pos, side, hand, hitX, hitY, hitZ, new BlockPos(-1, 0, -1), new BlockPos(1, 0, 1));
						break;
					case EAST:
					case WEST:
						placeBlocks(storagePhysical, request.getItemStack(), world, player, pos, side, hand, hitX, hitY, hitZ, new BlockPos(0, -1, -1), new BlockPos(0, 1, 1));
						break;
				}
			}
		}
	};

	public static EnumBlockContainerMode get(int modeIndex){
		if(values().length <= modeIndex){
			return values()[0];
		}
		return values()[modeIndex];
	}

	public void useMode(ItemStorageCellPhysical storagePhysical, ItemStack itemStack, IAEItemStack storageStack, IAEItemStack request, World world, BlockPos pos, EntityPlayer player, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ){
		IMEInventoryHandler cellInventory = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.ITEMS);
		cellInventory.extractItems(request, Actionable.MODULATE, new PlayerSource(player, null));
	}

	public void placeBlocks(ItemStorageCellPhysical storagePhysical, ItemStack itemstack, World world, EntityPlayer player, BlockPos pos, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ, BlockPos minOffset, BlockPos maxOffset) {
		for(int xOffset = minOffset.getX();xOffset <= maxOffset.getX();xOffset++){
			for(int yOffset = minOffset.getY();yOffset <= maxOffset.getY();yOffset++){
				for(int zOffset = minOffset.getZ();zOffset <= maxOffset.getZ();zOffset++){
					BlockPos position = pos.add(xOffset, yOffset, zOffset);
					IBlockState blockState = world.getBlockState(position);
					if(blockState.getBlock() != Blocks.BEDROCK && blockState.getBlockHardness(world, pos) >= 0.0F) {
						world.destroyBlock(position, true);
						placeBlock(storagePhysical, itemstack, world, player, position, side, hand, hitX, hitY, hitZ);
					}
				}
			}
		}
	}

	public void placeBlock(ItemStorageCellPhysical storagePhysical, ItemStack itemstack, World world, EntityPlayer player, BlockPos pos, EnumFacing side, EnumHand hand, float xOffset, float yOffset, float zOffset) {
		storagePhysical.extractAEPower(player.getHeldItem(hand), 20.0D);
		ItemBlock itemblock = (ItemBlock) itemstack.getItem();
		BlockPos position = pos.offset(side.getOpposite());
		itemblock.onItemUseFirst(itemstack, player, world, position, side, xOffset, yOffset, zOffset, hand);
		itemblock.onItemUse(itemstack, player, world,  position, hand, side, xOffset, yOffset, zOffset);
	}

}

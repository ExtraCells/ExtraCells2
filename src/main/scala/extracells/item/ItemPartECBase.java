package extracells.item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import extracells.api.ECApi;
import extracells.models.ModelManager;
import extracells.registries.PartEnum;
import extracells.util.Log;

public class ItemPartECBase extends ItemECBase implements IPartItem, IItemGroup, IColoredItem {

	public ItemPartECBase() {
		setMaxDamage(0);
		setHasSubtypes(true);
		//AEApi.instance().partHelper().setItemBusRenderer(this);

		for (PartEnum part : PartEnum.values()) {
			Map<Upgrades, Integer> possibleUpgradesList = part.getUpgrades();
			for (Upgrades upgrade : possibleUpgradesList.keySet()) {
				upgrade.registerItem(new ItemStack(this, 1, part.ordinal()),
					possibleUpgradesList.get(upgrade));
			}
		}
	}

	@Override
	public IPart createPartFromItemStack(ItemStack itemStack) {
		try {
			return PartEnum.values()[MathHelper.clamp(
				itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
				.newInstance(itemStack);
		} catch (Throwable ex) {
			Log.error(
				"ExtraCells2 severe error - could not create AE2 Part from ItemStack! This should not happen!\n"
					+ "[ExtraCells2 SEVERE] Contact Leonelf/M3gaFr3ak with the following stack trace.\n"
					+ "[ExtraCells2 SEVERE] Offending item: '%s'",
				itemStack.toString(), ex);
			return null;
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack == null) {
			return super.getItemStackDisplayName(null);
		}
		if (stack.getItemDamage() == PartEnum.INTERFACE.ordinal()) {
			return ECApi.instance().blocks().blockInterface().maybeItem().get().getItemStackDisplayName(
				ECApi.instance().blocks().blockInterface().maybeStack(1).get());
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		if (itemStack != null
			&& itemStack.getItemDamage() == PartEnum.OREDICTEXPORTBUS
			.ordinal()) {
			return super.getRarity(itemStack);
		}
		return EnumRarity.RARE;
	}

	@Override
	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	public void getSubItems( CreativeTabs creativeTab, NonNullList itemList) {
		if (!this.isInCreativeTab(creativeTab))
			return;
		for (int i = 0; i < PartEnum.values().length; i++) {
			PartEnum part = PartEnum.values()[i];
			if (part.getMod() == null || part.getMod().isEnabled()) {
				itemList.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public String getUnlocalizedGroupName(Set<ItemStack> otherItems,
		ItemStack itemStack) {
		return PartEnum.values()[MathHelper.clamp(
			itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
			.getGroupName();
	}

	@Override
	public String getTranslationKey(ItemStack itemStack) {
		return PartEnum.values()[MathHelper.clamp(
			itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
			.getTranslationKey();
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return AEApi.instance().partHelper().placeBus(playerIn.getHeldItem(hand), pos, facing, playerIn, hand, worldIn);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (PartEnum type : PartEnum.values()) {
			Optional<ModelResourceLocation> location = type.getItemModel();
			if (location.isPresent()) {
				ModelLoader.setCustomModelResourceLocation(item, type.ordinal(), location.get());
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		int colorIndex = tintIndex / 10;
		int index = tintIndex % 10;
		AEColor color = AEColor.values()[colorIndex];
		return color.getVariantByTintIndex(index);
	}
}

package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.api.ECApi;
import extracells.registries.PartEnum;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemPartECBase extends Item implements IPartItem, IItemGroup {

	public ItemPartECBase() {
		setMaxDamage(0);
		setHasSubtypes(true);
		AEApi.instance().partHelper().setItemBusRenderer(this);

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
			return PartEnum.values()[MathHelper.clamp_int(
					itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
					.newInstance(itemStack);
		} catch (Throwable ex) {
			FMLLog.log(
					Level.ERROR,
					ex,
					"ExtraCells2 severe error - could not create AE2 Part from ItemStack! This should not happen!\n"
							+ "[ExtraCells2 SEVERE] Contact Leonelf/M3gaFr3ak with the following stack trace.\n"
							+ "[ExtraCells2 SEVERE] Offending item: '%s'",
					itemStack.toString());
			return null;
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack == null)
			return super.getItemStackDisplayName(null);
		if (stack.getItemDamage() == PartEnum.INTERFACE.ordinal())
			return ECApi.instance().blocks().blockInterface().maybeItem().get().getItemStackDisplayName(
							ECApi.instance().blocks().blockInterface().maybeStack(1).get());
		return super.getItemStackDisplayName(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (int i = 0; i < PartEnum.values().length; i++) {
			PartEnum part = PartEnum.values()[i];
			if(part.getMod() == null || part.getMod().isEnabled())
				itemList.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String getUnlocalizedGroupName(Set<ItemStack> otherItems,
			ItemStack itemStack) {
		return PartEnum.values()[MathHelper.clamp_int(
				itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
				.getGroupName();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return PartEnum.values()[MathHelper.clamp_int(
				itemStack.getItemDamage(), 0, PartEnum.values().length - 1)]
				.getUnlocalizedName();
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return AEApi.instance().partHelper()
				.placeBus(is, x, y, z, side, player, world);
	}

	@Override
	public void registerIcons(IIconRegister _iconRegister) {}
}

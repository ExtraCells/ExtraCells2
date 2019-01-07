package extracells.item.storage;

import java.util.List;

import appeng.api.storage.IStorageChannel;
import extracells.api.IHandlerStorageBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import extracells.item.ItemECBase;
import extracells.models.ModelManager;
import extracells.registries.ItemEnum;

public abstract class ItemStorageCell extends ItemECBase implements ICellWorkbenchItem {
	protected final CellDefinition definition;
	protected final IStorageChannel channel;

	public ItemStorageCell(CellDefinition definition, IStorageChannel channel) {
		this.definition = definition;
		this.channel = channel;
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, World world, List list, ITooltipFlag advanced) {
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		IMEInventoryHandler handler = cellRegistry.getCellInventory(itemStack, null, channel);
		if (!(handler instanceof IHandlerStorageBase)) {
			return;
		}
		IHandlerStorageBase cellHandler = (IHandlerStorageBase) handler;
		boolean partitioned = cellHandler.isFormatted();
		long usedBytes = cellHandler.usedBytes();
		long storedCount = cellHandler.storedCount();

		list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".bytes"), usedBytes, cellHandler.totalBytes()));
		list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
		if (storedCount != 0) {
			list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".content"), storedCount));
		}

		if (partitioned) {
			list.add(I18n.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + I18n.translateToLocal("gui.appliedenergistics2.Precise"));
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.RARE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs creativeTab, NonNullList listSubItems) {
		if (!this.isInCreativeTab(creativeTab))
			return;
		for (StorageType type : definition.cells) {
			listSubItems.add(new ItemStack(this, 1, type.getMeta()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		StorageType type = definition.cells.fromMeta(itemStack.getItemDamage());
		return "extracells.item.storage." + type.getIdentifier();
	}

	@Override
	public boolean isEditable(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		return itemStack.getItem() == this;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStack = player.getHeldItem(hand);
		if (!player.isSneaking()) {
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
		}
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		IMEInventoryHandler<IAEFluidStack> handler = cellRegistry.getCellInventory(itemStack, null, channel);
		if (!(handler instanceof IHandlerStorageBase)) {
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
		}
		IHandlerStorageBase cellHandler = (IHandlerStorageBase) handler;
		if (cellHandler.usedBytes() == 0 && player.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(definition.ordinal()))) {
			return new ActionResult<>(EnumActionResult.SUCCESS, ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack.getItemDamage() + definition.componentMetaStart));
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (StorageType type : definition.cells) {
			manager.registerItemModel(item, type.getMeta(), type.getModelName());
		}
	}
}

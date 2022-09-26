package extracells.item;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IItemGroup;
import extracells.api.IStorageCellAdvanced;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.java.games.input.Keyboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.client.renderer.texture.IIconRegister;
import extracells.inventory.AdvancedCellInventoryHandler;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.HashMap;

import static appeng.util.Utility.formatNumbers;

public final class ItemAdvancedStorageCell extends AEBaseItem implements IStorageCellAdvanced, IItemGroup
{
	private final long totalBytes;
	private final int totalTypes;
	private final int perType;
	private final double idleDrain;
	private final String suffix;
	private IIcon icon;
	Random r = new Random();

	public ItemAdvancedStorageCell(long bytes, int types, int bytesPerType, double drain, String suffix)
	{

		this.setFeature( EnumSet.of( AEFeature.StorageCells ) );
		this.setMaxStackSize( 1 );
		this.totalBytes = bytes;
		this.totalTypes = types;
		this.perType = bytesPerType;
		this.idleDrain = drain;
		this.suffix = suffix;

	}

    @SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( inventory instanceof AdvancedCellInventoryHandler )
		{
			final ICellInventoryHandler handler = (ICellInventoryHandler) inventory;

			final ICellInventory cellInventory = handler.getCellInv();

			if( cellInventory != null )
			{
				lines.add( formatNumbers(cellInventory.getUsedBytes()) + " " + GuiText.Of.getLocal() + ' ' + formatNumbers(cellInventory.getTotalBytes()) + ' ' + GuiText.BytesUsed.getLocal() );

				format(lines, handler, cellInventory);
			}
		}
	}

	static void format(List<String> lines, ICellInventoryHandler handler, ICellInventory cellInventory) {
		lines.add( formatNumbers(cellInventory.getStoredItemTypes()) + " " + GuiText.Of.getLocal() + ' ' + formatNumbers(cellInventory.getTotalItemTypes()) + ' ' + GuiText.Types.getLocal() );

		if( handler.isPreformatted() )
		{
			String filter = cellInventory.getOreFilter();

			if (filter.isEmpty()) {
				final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).getLocal();

				if (handler.isFuzzy()) {
					lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
				} else {
					lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
				}
			}
			else {
				lines.add(GuiText.PartitionedOre.getLocal() + " : " + filter);
			}
		}
	}

	@Override
	public long getBytes( final ItemStack cellItem )
	{
		return this.totalBytes;
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return this.perType;
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return this.totalTypes;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain( ItemStack i )
	{
		return this.idleDrain;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.storage.physical.advanced." + this.suffix;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icon = iconRegister.registerIcon("extracells:storage.physical.advanced." + this.suffix);
	}

	@Override
	public IIcon getIconFromDamage(int dmg) {
		return this.icon;
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public String getOreFilter(ItemStack is) {
		return Platform.openNbtData( is ).getString( "OreFilter" );
	}

	@Override
	public void setOreFilter(ItemStack is, String filter) {
		Platform.openNbtData( is ).setString("OreFilter", filter);
	}

	@Override
	public ItemStack getContainerItem( final ItemStack itemStack )
	{
		for( final ItemStack stack : AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 ).asSet() )
		{
			return stack;
		}

		throw new MissingDefinition( "Tried to use empty storage cells while basic storage cells are defined." );
	}

	@Override
	public boolean hasContainerItem( final ItemStack stack )
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.EnableDisassemblyCrafting );
	}
}

package extracells.item;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IItemGroup;
import extracells.api.IStorageCellVoid;
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
import extracells.inventory.VoidCellInventoryHandler;

import javax.annotation.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.HashMap;

public final class ItemVoidStorageCell extends AEBaseItem implements IStorageCellVoid, IItemGroup
{
	private final int totalBytes;
	private final int totalTypes;
	private final int perType;
	private final double idleDrain;
	private final String suffix;
	private IIcon icon;

	public ItemVoidStorageCell()
	{

		this.setFeature( EnumSet.of( AEFeature.StorageCells ) );
		this.setMaxStackSize( 1 );
		this.totalBytes = 1024;
		this.totalTypes = 63;
		this.perType = 8;
		this.idleDrain = 0.0;
		this.suffix = "void";

	}

    @SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( inventory instanceof VoidCellInventoryHandler )
		{
			final ICellInventoryHandler handler = (ICellInventoryHandler) inventory;

			final ICellInventory cellInventory = handler.getCellInv();

			if( cellInventory != null )
			{
                lines.add( "Deletes \u00A74everything\u00A77 stored on it ");

				lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + " \u00A7k9999\u00A77 " + GuiText.BytesUsed.getLocal() );

				ItemAdvancedStorageCell.format(lines, handler, cellInventory);
			}
		}
	}

	@Override
	public int getBytes( final ItemStack cellItem )
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
	public double getIdleDrain()
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
		return "extracells.item.storage.physical.void";
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icon = iconRegister.registerIcon("extracells:storage.physical.void");
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

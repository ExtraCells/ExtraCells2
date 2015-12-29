package extracells.api;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.definitions.IBlockDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface ExtraCellsApi {

	public void addFluidToShowBlacklist(Class<? extends Fluid> clazz);

	public void addFluidToShowBlacklist(Fluid fluid);

	public void addFluidToStorageBlacklist(Class<? extends Fluid> clazz);

	public void addFluidToStorageBlacklist(Fluid fluid);

	public IBlockDefinition blocks();

	public boolean canFluidSeeInTerminal(Fluid fluid);

	public boolean canStoreFluid(Fluid fluid);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	public String getVerion();

	public String getVersion();

	@Deprecated
	public IWirelessFluidTermHandler getWirelessFluidTermHandler(ItemStack is);

	public IWirelessGasFluidTermHandler getWirelessTermHandler(ItemStack is);

	public boolean isWirelessFluidTerminal(ItemStack is);

	public IItemDefinition items();

	@Deprecated
	public ItemStack openPortableCellGui(EntityPlayer player, ItemStack stack, World world);

	public ItemStack openPortableGasCellGui(EntityPlayer player, ItemStack stack, World world);

	public ItemStack openPortableFluidCellGui(EntityPlayer player, ItemStack stack, World world);

	@Deprecated
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world);

	public ItemStack openWirelessFluidTerminal(EntityPlayer player, ItemStack stack, World world);

	public ItemStack openWirelessGasTerminal(EntityPlayer player, ItemStack stack, World world);

	@Deprecated
	public ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world, int x, int y, int z, Long key);

	public IPartDefinition parts();

	public void registerWirelessTermHandler(IWirelessGasFluidTermHandler handler);

	@Deprecated
	public void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	public void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	public void registerFuelBurnTime(Fluid fuel, int burnTime);

	public boolean isGasStack(IAEFluidStack stack);

	public boolean isGasStack(FluidStack stack);

	public boolean isGas(Fluid fluid);

	/**
	 * Converts an IAEFluid stack to a GasStack
	 *
	 * @param fluidStack
	 * @return GasStack
     */
	public Object createGasStack(IAEFluidStack fluidStack);

	/**
	 * Create the fluidstack from the specific gas
	 *
	 * @param gasStack
	 * @return FluidStack
     */
	public IAEFluidStack createFluidStackFromGas(Object gasStack);

	/**
	 * Create the ec fluid from the specific gas
	 *
	 * @param gas
	 * @return Fluid
     */
	public Fluid getGasFluid(Object gas);

	/**
	 * A registry for StorageBus interactions
	 *
	 * @param esh storage handler
	 */
	void addExternalStorageInterface( IExternalGasStorageHandler esh );

	/**
	 * @param te       tile entity
	 * @param opposite direction
	 * @param mySrc    source
	 *
	 * @return the handler for a given tile / forge direction
	 */
	IExternalGasStorageHandler getHandler(TileEntity te, ForgeDirection opposite, BaseActionSource mySrc );
}

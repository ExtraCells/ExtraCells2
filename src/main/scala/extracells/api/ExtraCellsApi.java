package extracells.api;

import appeng.api.networking.security.IActionSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import extracells.api.definitions.IBlockDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;

public interface ExtraCellsApi {

	void addFluidToShowBlacklist(Class<? extends Fluid> clazz);

	void addFluidToShowBlacklist(Fluid fluid);

	void addFluidToStorageBlacklist(Class<? extends Fluid> clazz);

	void addFluidToStorageBlacklist(Fluid fluid);

	IBlockDefinition blocks();

	boolean canFluidSeeInTerminal(Fluid fluid);

	boolean canStoreFluid(Fluid fluid);

	String getVersion();

	IWirelessGasFluidTermHandler getWirelessTermHandler(ItemStack is);

	boolean isWirelessFluidTerminal(ItemStack is);

	IItemDefinition items();

	ItemStack openPortableGasCellGui(EntityPlayer player, EnumHand hand, World world);

	ItemStack openPortableFluidCellGui(EntityPlayer player, EnumHand hand, World world);

	ItemStack openWirelessFluidTerminal(EntityPlayer player, EnumHand hand, World world);

	ItemStack openWirelessGasTerminal(EntityPlayer player, EnumHand hand, World world);

	IPartDefinition parts();

	void registerWirelessTermHandler(IWirelessGasFluidTermHandler handler);

	@Deprecated
	void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	void registerFuelBurnTime(Fluid fuel, int burnTime);

	boolean isGasStack(IAEFluidStack stack);

	boolean isGasStack(FluidStack stack);

	boolean isGas(Fluid fluid);

	/**
	 * Converts an IAEFluid stack to a GasStack
	 *
	 * @param fluidStack
	 * @return GasStack
	 */
	Object createGasStack(IAEFluidStack fluidStack);

	/**
	 * Create the fluidstack from the specific gas
	 *
	 * @param gasStack
	 * @return FluidStack
	 */
	IAEFluidStack createFluidStackFromGas(Object gasStack);

	/**
	 * Create the ec fluid from the specific gas
	 *
	 * @param gas
	 * @return Fluid
	 */
	Fluid getGasFluid(Object gas);

	/**
	 * A registry for StorageBus interactions
	 *
	 * @param esh storage handler
	 */
	void addExternalStorageInterface(IExternalGasStorageHandler esh);

	/**
	 * @param te       tile entity
	 * @param opposite direction
	 * @param mySrc    source
	 * @return the handler for a given tile / forge direction
	 */
	IExternalGasStorageHandler getHandler(TileEntity te, EnumFacing opposite, IActionSource mySrc);

	boolean isGasSystemEnabled();

	void registerWrenchHandler(IWrenchHandler wrenchHandler);
}

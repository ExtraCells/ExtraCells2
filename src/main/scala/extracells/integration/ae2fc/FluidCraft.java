package extracells.integration.ae2fc;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

import com.glodblock.github.util.NameConst;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLModIdMappingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.registries.PartEnum;

public final class FluidCraft {

    private final static Map<PartEnum, Short> REPLACER = new HashMap<>(16);

    @Optional.Method(modid = "ae2fc")
    public static void onRemapEvent(FMLModIdMappingEvent event) {
        REPLACER.put(
                PartEnum.FLUIDEXPORT,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_EXPORT)));
        REPLACER.put(
                PartEnum.FLUIDIMPORT,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_IMPORT)));
        REPLACER.put(
                PartEnum.FLUIDSTORAGE,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_STORAGE_BUS)));
        REPLACER.put(
                PartEnum.FLUIDLEVELEMITTER,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_LEVEL_EMITTER)));
        REPLACER.put(
                PartEnum.FLUIDMONITOR,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_STORAGE_MONITOR)));
        REPLACER.put(
                PartEnum.FLUIDCONVERSIONMONITOR,
                (short) Item
                        .getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_CONVERSION_MONITOR)));
        REPLACER.put(
                PartEnum.INTERFACE,
                (short) Item.getIdFromItem(GameRegistry.findItem("ae2fc", NameConst.ITEM_PART_FLUID_INTERFACE)));
    }

    public static void replace(NBTTagCompound def, PartEnum part) {
        def.setShort("id", REPLACER.getOrDefault(part, (short) 0));
        def.setShort("Damage", (short) 0);
    }

    @Optional.Method(modid = "ae2fc")
    public static NBTTagCompound createFluidDisplay(String fluidName) {
        NBTTagCompound fluidDisplay = new NBTTagCompound();
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid != null) {
            ItemStack fluidPacket = new ItemStack(GameRegistry.findItem("ae2fc", "fluid_packet"), 1, 0);
            NBTTagCompound fluidPacketTag = new NBTTagCompound();
            // FluidStack
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            NBTTagCompound fluidStackNbt = new NBTTagCompound();
            fluidStack.writeToNBT(fluidStackNbt);
            fluidPacketTag.setTag("FluidStack", fluidStackNbt);
            // FluidPacket
            fluidPacketTag.setBoolean("DisplayOnly", true);
            fluidPacket.setTagCompound(fluidPacketTag);
            // Final Item
            IAEItemStack aeStack = AEItemStack.create(fluidPacket);
            aeStack.writeToNBT(fluidDisplay);
        }
        return fluidDisplay;
    }

    public static NBTTagCompound createFluidNBT(String fluidName, long amount) {
        NBTTagCompound fluid = new NBTTagCompound();
        fluid.setString("FluidName", fluidName);
        fluid.setBoolean("Craft", false);
        fluid.setLong("Req", 0);
        fluid.setLong("Cnt", amount);
        return fluid;
    }
}

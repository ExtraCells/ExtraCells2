package extracells.integration.opencomputers;


import appeng.api.AEApi;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.oc.api.API;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class OCRecipes {

    public static void loadRecipes(){
        GameRegistry.addShapedRecipe(new ItemStack(GameRegistry.findItem("extracells", "oc.upgrade")), "DAD", "MBM", "DCD",
                'A', AEApi.instance().definitions().materials().wireless().maybeStack(1).get(),
                'C', API.items.get("printedCircuitBoard").createItemStack(1),
                'B', API.items.get("wlanCard").createItemStack(1),
                'D', Items.diamond,
                'M', API.items.get("chip3").createItemStack(1));
    }

}

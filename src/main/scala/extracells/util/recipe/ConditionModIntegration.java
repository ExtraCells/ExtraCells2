package extracells.util.recipe;


import com.google.gson.JsonObject;
import extracells.integration.Integration;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class ConditionModIntegration implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        try{
            return () -> Integration.Mods.valueOf(JsonUtils.getString(json, "mod").toUpperCase()).isEnabled();
        }catch (Exception e){
            return () -> false;
        }
    }
}

package extracells.model.render;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import extracells.model.*;

public class ItemSolderingStationRenderer implements IItemRenderer {

    private ModelSolderingStation model;

    public ItemSolderingStationRenderer() {
        model = new ModelSolderingStation();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
            ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

        FMLClientHandler.instance().getClient().renderEngine
                .bindTexture("/mods/extracells/textures/blocks/SolderingStation.png"); // texture
        GL11.glPushMatrix();
        GL11.glTranslatef((float) 0.0D + 0.5F, (float) 0.0D + 1.5F,
                (float) 0.0D + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        model.renderAll(0.0625f);
        GL11.glPopMatrix(); // end
    }

}

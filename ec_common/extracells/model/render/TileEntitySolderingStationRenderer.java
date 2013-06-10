package extracells.model.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import extracells.model.*;
import extracells.tile.TileEntitySolderingStation;

public class TileEntitySolderingStationRenderer extends TileEntitySpecialRenderer {

    private ModelSolderingStation model;
    
    public TileEntitySolderingStationRenderer() {
        model = new ModelSolderingStation();
    }

    public void renderAModelAt(TileEntity tile, double d, double d1, double d2, float f) {

        bindTextureByName("/mods/extracells/textures/blocks/SolderingStation.png"); // texture
        GL11.glPushMatrix();
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        int orientation = tile.worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord);
        if(orientation == 0){
        	GL11.glRotatef(0,0,1,0);
        }else if(orientation == 1){
        	GL11.glRotatef(90,0,1,0);
        }else if(orientation == 2){
        	GL11.glRotatef(180,0,1,0);
        }else if(orientation == 3){
        	GL11.glRotatef(270,0,1,0);
        }
        model.renderAll(0.0625f);
        GL11.glPopMatrix(); // end
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float tick) {
        this.renderAModelAt((TileEntitySolderingStation) tileentity, x, y, z, tick);

    }

}

package extracells.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSolderingStation extends ModelBase {
	  //fields
    ModelRenderer Stand1;
    ModelRenderer Stand2;
    ModelRenderer Stand3;
    ModelRenderer Stand4;
    ModelRenderer Surface;
    ModelRenderer SolderingStand;
    ModelRenderer SolderingHolder;
    ModelRenderer Base;
    ModelRenderer Solder;
    
    public ModelSolderingStation()
    {
      textureWidth = 64;
      textureHeight = 64;
      
        Stand1 = new ModelRenderer(this, 0, 20);
        Stand1.addBox(0F, 0F, 0F, 1, 15, 1);
        Stand1.setRotationPoint(6.9F, 9F, 6.9F);
        Stand1.setTextureSize(64, 64);
        Stand1.mirror = true;
        setRotation(Stand1, 0F, 0F, 0F);
        Stand2 = new ModelRenderer(this, 4, 20);
        Stand2.addBox(0F, 0F, 0F, 1, 15, 1);
        Stand2.setRotationPoint(-7.933333F, 9F, -7.9F);
        Stand2.setTextureSize(64, 64);
        Stand2.mirror = true;
        setRotation(Stand2, 0F, 0F, 0F);
        Stand3 = new ModelRenderer(this, 8, 20);
        Stand3.addBox(0F, 0F, 0F, 1, 15, 1);
        Stand3.setRotationPoint(-7.9F, 9F, 6.9F);
        Stand3.setTextureSize(64, 64);
        Stand3.mirror = true;
        setRotation(Stand3, 0F, 0F, 0F);
        Stand4 = new ModelRenderer(this, 12, 20);
        Stand4.addBox(0F, 0F, 0F, 1, 15, 1);
        Stand4.setRotationPoint(6.9F, 9F, -7.9F);
        Stand4.setTextureSize(64, 64);
        Stand4.mirror = true;
        setRotation(Stand4, 0F, 0F, 0F);
        Surface = new ModelRenderer(this, 0, 0);
        Surface.addBox(0F, 0F, 0F, 16, 4, 16);
        Surface.setRotationPoint(-8F, 8F, -8F);
        Surface.setTextureSize(64, 64);
        Surface.mirror = true;
        setRotation(Surface, 0F, 0F, 0F);
        SolderingStand = new ModelRenderer(this, 16, 20);
        SolderingStand.addBox(0F, 0F, 0F, 6, 1, 4);
        SolderingStand.setRotationPoint(1F, 7.5F, -7F);
        SolderingStand.setTextureSize(64, 64);
        SolderingStand.mirror = true;
        setRotation(SolderingStand, 0F, 0F, 0F);
        SolderingHolder = new ModelRenderer(this, 36, 20);
        SolderingHolder.addBox(0F, 0F, 0F, 1, 3, 1);
        SolderingHolder.setRotationPoint(4F, 6.1F, -6F);
        SolderingHolder.setTextureSize(64, 64);
        SolderingHolder.mirror = true;
        setRotation(SolderingHolder, 0F, 0F, -0.7853982F);
        Base = new ModelRenderer(this, 40, 22);
        Base.addBox(0F, 0F, 0F, 6, 2, 2);
        Base.setRotationPoint(0F, 2.5F, -6.5F);
        Base.setTextureSize(64, 64);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0.1745329F);
        Solder = new ModelRenderer(this, 40, 20);
        Solder.addBox(0F, 0F, 0F, 2, 1, 1);
        Solder.setRotationPoint(5.8F, 4F, -6F);
        Solder.setTextureSize(64, 64);
        Solder.mirror = true;
        setRotation(Solder, 0F, 0F, 0.5205006F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3,
            float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Stand1.render(f5);
        Stand2.render(f5);
        Stand3.render(f5);
        Stand4.render(f5);
        Surface.render(f5);
        SolderingStand.render(f5);
        SolderingHolder.render(f5);
        Base.render(f5);
        Solder.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }


    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3,
            float f4, float f5, Entity entity) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

    public void renderAll(float f1) {
		Stand1.render(f1);
		Stand2.render(f1);
		Stand3.render(f1);
		Stand4.render(f1);
		Surface.render(f1);
		SolderingStand.render(f1);
		SolderingHolder.render(f1);
	    Base.render(f1);
	    Solder.render(f1);
		
    }

}

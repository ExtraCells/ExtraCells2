package extracells.part;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import extracells.util.WrenchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;

public class PartStorageMonitor extends PartECBase implements IStackWatcherHost {
	
	Fluid fluid = null;
	long amount = 0L;
	private Object dspList;
	
	boolean locked = false;
	
	IStackWatcher watcher = null;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
        rh.setTexture(side);
        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderInventoryBox(renderer);
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        ts.setBrightness(13 << 20 | 13 << 4);

        rh.setInvColor(0xFFFFFF);
        rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

        rh.setBounds(3, 3, 15, 13, 13, 16);
        rh.setInvColor(AEColor.Transparent.blackVariant);
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.mediumVariant);
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.whiteVariant);
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        renderInventoryBusLights(rh, renderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
        rh.setTexture(side);
        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        if (isActive())
            Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

        ts.setColorOpaque_I(0xFFFFFF);
        rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

        IPartHost host = getHost();
        rh.setBounds(3, 3, 15, 13, 13, 16);
        ts.setColorOpaque_I(host.getColor().mediumVariant);
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.getTextures()[0], ForgeDirection.SOUTH, renderer);
        ts.setColorOpaque_I(host.getColor().whiteVariant);
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.getTextures()[1], ForgeDirection.SOUTH, renderer);
        ts.setColorOpaque_I(host.getColor().blackVariant);
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        renderStaticBusLights(x, y, z, rh, renderer);
	}
	
	@Override
    public boolean requireDynamicRender() {
        return true;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		if(fluid == null)
			return;
		
		if (this.dspList == null)
			this.dspList = GLAllocation.generateDisplayLists( 1 );

		Tessellator tess = Tessellator.instance;

		if (!isActive())
			return;

		IAEFluidStack ais = FluidUtil.createAEFluidStack(fluid);
		ais.setStackSize(amount);
		if ( ais != null )
		{
			GL11.glPushMatrix();
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );



				GL11.glNewList( (Integer) this.dspList, GL11.GL_COMPILE_AND_EXECUTE );
				this.renderFluid( tess, ais );
				GL11.glEndList();

			GL11.glPopMatrix();
		}
	}
    
    @SideOnly(Side.CLIENT)
    private void renderFluid(Tessellator tess, IAEFluidStack fluidStack)
	{
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		ForgeDirection d = this.getSide();
		GL11.glTranslated( d.offsetX * 0.77, d.offsetY * 0.77, d.offsetZ * 0.77 );

		if (d == ForgeDirection.UP)
		{
			GL11.glScalef(1.0f, -1.0f, 1.0f);
			GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(90.0F, 0, 0, 1);
		}

		if (d == ForgeDirection.DOWN)
		{
			GL11.glScalef(1.0f, -1.0f, 1.0f);
			GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
			GL11.glRotatef(-90.0F, 0, 0, 1);
		}

		if ( d == ForgeDirection.EAST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( d == ForgeDirection.WEST )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
		}

		if ( d == ForgeDirection.NORTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
		}

		if ( d == ForgeDirection.SOUTH )
		{
			GL11.glScalef( -1.0f, -1.0f, -1.0f );
			GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
		}

		GL11.glPushMatrix();
		try
		{

			int br = 16 << 20 | 16 << 4;
			int var11 = br % 65536;
			int var12 = br / 65536;
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

			GL11.glDisable( GL11.GL_LIGHTING );
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );
			// RenderHelper.enableGUIStandardItemLighting();
			tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

			IIcon fluidIcon = fluid.getIcon();
			
			GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
			GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );
			GL11.glTranslated(-8.6F, -16.3, -1.2F);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Tessellator cake = Tessellator.instance;
			cake.startDrawingQuads();
			cake.setBrightness(255);
			cake.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
			cake.addVertexWithUV(0, 16, 0, fluidIcon.getMinU(), fluidIcon.getMaxV());
			cake.addVertexWithUV(16, 16, 0, fluidIcon.getMaxU(), fluidIcon.getMaxV());
			cake.addVertexWithUV(16, 0, 0, fluidIcon.getMaxU(), fluidIcon.getMinV());
			cake.addVertexWithUV(0, 0, 0, fluidIcon.getMinU(), fluidIcon.getMinV());
			cake.draw();
		}
		catch (Exception e)
		{
			
		}

		GL11.glPopMatrix();

		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		long qty = fluidStack.getStackSize();
		if ( qty > 999999999999L )
			qty = 999999999999L;

		String msg = Long.toString( qty ) + "mB";
		if ( qty > 1000000000 )
			msg = Long.toString( qty / 1000000000 ) + 'B';
		else if ( qty > 1000000 )
			msg = Long.toString( qty / 1000000 ) + "MB";
		else if ( qty > 9999 )
			msg = Long.toString( qty / 1000 ) + "KB";

		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		int width = fr.getStringWidth( msg );
		GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
		fr.drawString( msg, 0, 0, 0 );

		GL11.glPopAttrib();
	}


	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
        bch.addBox(5, 5, 12, 11, 11, 13);
	}

	@Override
	public int cableConnectionRenderTo() {
		return 1;
	}
	
	@Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
		if(player == null || player.worldObj == null)
			return false;
		if(player.worldObj.isRemote)
			return true;
		ItemStack s = player.getCurrentEquippedItem();
		if(s == null){
			if(locked)
				return false;
			if(fluid == null)
				return true;
			if(watcher != null)
				watcher.remove(FluidUtil.createAEFluidStack(fluid));
			fluid = null;
			amount = 0L;
			IPartHost host = getHost();
			if(host != null)
				host.markForUpdate();
			return true;
		}
		if(WrenchUtil.canWrench(s, player, tile.xCoord, tile.yCoord, tile.zCoord)){
			locked = !locked;
			WrenchUtil.wrenchUsed(s, player, tile.xCoord, tile.zCoord, tile.yCoord);
			IPartHost host = getHost();
			if(host != null)
				host.markForUpdate();
			if(locked)
				player.addChatMessage(new ChatComponentTranslation("chat.appliedenergistics2.isNowLocked"));
			else
				player.addChatMessage(new ChatComponentTranslation("chat.appliedenergistics2.isNowUnlocked"));
			return true;
		}
		if(locked)
			return false;
		if(FluidUtil.isFilled(s)){
			if(fluid != null && watcher != null)
				watcher.remove(FluidUtil.createAEFluidStack(fluid));
			fluid = FluidUtil.getFluidFromContainer(s).getFluid();
			if(watcher != null)
				watcher.add(FluidUtil.createAEFluidStack(fluid));
			onStackChange(null, null, null, null, null);
			IPartHost host = getHost();
			if(host != null)
				host.markForUpdate();
			return true;
		}
		return false;
	}
	
	protected void dropItems(World world, int x, int y, int z, ItemStack stack)
    {
		if(world == null)
			return;
        if (!world.isRemote)
        {
            float f = 0.7F;
            double d0 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d1 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d2 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(world, (double)x + d0, (double)y + d1, (double)z + d2, stack);
            entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
        }
    }
	
	protected IMEMonitor<IAEFluidStack> getFluidStorage(){
		IGridNode n = getGridNode();
		if(n == null)
			return null;
		IGrid g = n.getGrid();
		if(g == null)
			return null;
		IStorageGrid storage = g.getCache(IStorageGrid.class);
		if(storage == null)
			return null;
		return storage.getFluidInventory();
	}

	@Override
	public void onStackChange(IItemList arg0, IAEStack arg1, IAEStack arg2, BaseActionSource arg3, StorageChannel arg4) {
		if(fluid != null){
			IGridNode n = getGridNode();
			if(n == null)
				return;
			IGrid g = n.getGrid();
			if(g == null)
				return;
			IStorageGrid storage = g.getCache(IStorageGrid.class);
			if(storage == null)
				return;
			IMEMonitor<IAEFluidStack> fluids = getFluidStorage();;
			if(fluids == null)
				return;
			for(IAEFluidStack s : fluids.getStorageList()){
				if(s.getFluid() == fluid){
					amount = s.getStackSize();
					IPartHost host = getHost();
					if(host != null)
						host.markForUpdate();
				}
			}
		}
		
	}

	@Override
	public void updateWatcher(IStackWatcher w) {
		watcher = w;
		if(fluid != null)
			w.add(FluidUtil.createAEFluidStack(fluid));
		onStackChange(null, null, null, null, null);
	}
	
	 @Override
	    public void writeToNBT(NBTTagCompound data) {
	    	super.writeToNBT(data);
	    	data.setLong("amount", amount);
	    	if(fluid == null)
				 data.setInteger("fluid", -1);
			 else
				 data.setInteger("fluid", fluid.getID());
	    	data.setBoolean("locked", locked);
	 }

	 @Override
	 public void readFromNBT(NBTTagCompound data) {
		 super.readFromNBT(data);
		 if(data.hasKey("amount"))
			 amount = data.getLong("amount");
		 if(data.hasKey("fluid")){
			 int id = data.getInteger("fluid");
			 if(id == -1)
				 fluid = null;
			 else
				 fluid = FluidRegistry.getFluid(id);
		 }
		 if(data.hasKey("locked"))
			 locked = data.getBoolean("locked");
	 }
	 
	 @Override
	 public void writeToStream(ByteBuf data) throws IOException {
		 super.writeToStream(data);
		 data.writeLong(amount);
		 if(fluid == null)
			 data.writeInt(-1);
		 else
			 data.writeInt(fluid.getID());
		 data.writeBoolean(locked);
		 
	 }

	 @Override
	 public boolean readFromStream(ByteBuf data) throws IOException {
		 super.readFromStream(data);
		 amount = data.readLong();
		 int id = data.readInt();
		 if(id == -1)
			 fluid = null;
		 else
			 fluid = FluidRegistry.getFluid(id);
		 locked = data.readBoolean();
		 return true;
	 }
}

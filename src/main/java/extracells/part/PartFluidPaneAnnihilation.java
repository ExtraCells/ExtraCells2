package extracells.part;

import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class PartFluidPaneAnnihilation extends PartECBase
{

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		rh.setInvColor(AEColor.Cyan.blackVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
		Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
		rh.setInvColor(AEColor.Cyan.mediumVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Cyan.whiteVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;
		IIcon side = TextureManager.BUS_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		if (host != null)
		{
			ts.setColorOpaque_I(host.getColor().blackVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
			if (isActive())
				ts.setBrightness(13 << 20 | 13 << 4);
			ts.setColorOpaque_I(host.getColor().mediumVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
			ts.setColorOpaque_I(host.getColor().whiteVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);
		}
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox(2, 2, 14, 14, 14, 16);
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 2;
	}
}

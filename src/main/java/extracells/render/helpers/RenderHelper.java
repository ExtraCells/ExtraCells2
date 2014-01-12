package extracells.render.helpers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class RenderHelper
{
	int[] blue =
	{ 0x2D29A0, 0x514AFF, 0xDDE6FF };
	int[] black =
	{ 0x2B2B2B, 0x565656, 0x848484 };
	int[] white =
	{ 0xBEBEBE, 0xDBDBDB, 0xFAFAFA };
	int[] brown =
	{ 0x724E35, 0xB7967F, 0xE0D2C8 };
	int[] red =
	{ 0xA50029, 0xFF003C, 0xFFE6ED };
	int[] yellow =
	{ 0xFFF7AA, 0xF8FF4A, 0xFFFFE8 };
	int[] green =
	{ 0x45A021, 0x60E32E, 0xE3F2E3 };
	int[] fluix =
	{ 0x1B2344, 0x895CA8, 0xDABDEF };

	public void drawFace(ForgeDirection side, Block block, double x, double y, double z, Icon icon, RenderBlocks renderer)
	{
		switch (side)
		{
		case UP:
			renderer.renderFaceYPos(block, x, y, z, icon);
			break;
		case DOWN:
			renderer.renderFaceYNeg(block, x, y, z, icon);
			break;
		case NORTH:
			renderer.renderFaceZNeg(block, x, y, z, icon);
			break;
		case EAST:
			renderer.renderFaceXPos(block, x, y, z, icon);
			break;
		case SOUTH:
			renderer.renderFaceZPos(block, x, y, z, icon);
			break;
		case WEST:
			renderer.renderFaceXNeg(block, x, y, z, icon);
			break;
		case UNKNOWN:
			break;
		}
	}
}

package extracells.render.helpers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class RenderHelper
{
	static int[] blue =
	{ 0x2D29A0, 0x514AFF, 0xDDE6FF };
	static int[] black =
	{ 0x2B2B2B, 0x565656, 0x848484 };
	static int[] white =
	{ 0xBEBEBE, 0xDBDBDB, 0xFAFAFA };
	static int[] brown =
	{ 0x724E35, 0xB7967F, 0xE0D2C8 };
	static int[] red =
	{ 0xA50029, 0xFF003C, 0xFFE6ED };
	static int[] yellow =
	{ 0xFFF7AA, 0xF8FF4A, 0xFFFFE8 };
	static int[] green =
	{ 0x45A021, 0x60E32E, 0xE3F2E3 };
	static int[] fluix =
	{ 0x1B2344, 0x895CA8, 0xDABDEF };

	public static void drawFace(ForgeDirection side, Block block, double x, double y, double z, Icon icon, RenderBlocks renderer)
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

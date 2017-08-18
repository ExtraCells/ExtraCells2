package extracells.models;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IStateMapperRegister {

	@SideOnly(Side.CLIENT)
	void registerStateMapper();

}

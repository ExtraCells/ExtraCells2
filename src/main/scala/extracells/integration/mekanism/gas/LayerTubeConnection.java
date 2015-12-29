package extracells.integration.mekanism.gas;


import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraftforge.common.util.ForgeDirection;

public class LayerTubeConnection extends LayerBase implements ITubeConnection{
    @Override
    public boolean canTubeConnect(ForgeDirection side) {
        IPart part = this.getPart(side);
        if(part instanceof ITubeConnection) {
            return ((ITubeConnection) part).canTubeConnect(side);
        }
        return part instanceof IGasHandler;
    }
}

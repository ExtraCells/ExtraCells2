package extracells.network.packet.part;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import appeng.api.config.AccessRestriction;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.GuiBusFluidStorage;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class PacketBusFluidStorage extends AbstractPacket {

    PartFluidStorage part;
    AccessRestriction access;

    public PacketBusFluidStorage() {
    }

    public PacketBusFluidStorage(EntityPlayer _player, PartFluidStorage _part) {
        super(_player);
        mode = 0;
        player = _player;
        part = _part;
    }
    
    public PacketBusFluidStorage(EntityPlayer _player, AccessRestriction _access, boolean toClient){
    	super(_player);
    	if(toClient)
    		mode = 1;
    	else
    		mode = 2;
    	access = _access;
    }

    public void writeData(ByteBuf out) {
    	switch (mode){
    		case 0:
    			writePart(part, out);
    			break;
    		case 1:
    		case 2:
    			writeString(access.name(), out);
    	}
        
    }

    public void readData(ByteBuf in) {
    	switch (mode){
		case 0:
			part = (PartFluidStorage) readPart(in);
			break;
		case 1:
		case 2:
			access = AccessRestriction.valueOf(readString(in));
    	}
        
    }

    @Override
    public void execute() {
        switch (mode) {
            case 0:
                part.sendInformation(player);
                break;
            case 1:
            	try{
            		handleClient();
            	}catch(Throwable e){}
            	break;
            case 2:
            	Container con = player.openContainer;
            	if(con != null && con instanceof ContainerBusFluidStorage){
            		((ContainerBusFluidStorage)con).part.updateAccess(access);
            		new PacketBusFluidStorage(player, access, true).sendPacketToPlayer(player);
            	}
        }
    }
    
    @SideOnly(Side.CLIENT)
    void handleClient(){
    	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
    	if(screen != null && screen instanceof GuiBusFluidStorage){
    		((GuiBusFluidStorage)screen).updateAccessRestriction(access);
    	}
    }
}

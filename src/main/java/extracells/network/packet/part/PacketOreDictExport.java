package extracells.network.packet.part;

import appeng.api.config.RedstoneMode;
import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiBusFluidIO;
import extracells.gui.GuiOreDictExport;
import extracells.network.AbstractPacket;
import extracells.part.PartFluidIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

import java.util.List;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketOreDictExport extends AbstractPacket {

    private String filter;
    private Side side;

    public PacketOreDictExport() {
    }

    public PacketOreDictExport(EntityPlayer _player, String filter, Side side) {
        super(_player);
        mode = 0;
        this.filter = filter;
        this.side =  side;
    }

    @Override
    public void writeData(ByteBuf out) {
        switch (mode) {
            case 0:
                out.writeBoolean(side.isServer());
                ByteBufUtils.writeUTF8String(out, filter);
                break;

        }
    }

    @Override
    public void readData(ByteBuf in) {
        switch (mode) {
            case 0:
                if(in.readBoolean())
                	side = Side.SERVER;
                else
                	side = Side.CLIENT;
                filter = ByteBufUtils.readUTF8String(in);
                break;
        }
    }

    public void execute() {
        switch (mode) {
            case 0:
            	if(side.isClient())
            		try{
            			handleClient();
            		}catch(Throwable e){}
            	else
            		handleServer();
            	break;
        }
    }
    
    private void handleServer(){
    	Container con = player.openContainer;
    	if(con != null && con instanceof ContainerOreDictExport){
    		ContainerOreDictExport c = (ContainerOreDictExport) con;
    		c.part.filter = filter;
    	}
    }
    
    @SideOnly(Side.CLIENT)
    private void handleClient(){
    	GuiOreDictExport.updateFilter(filter);
    }
}

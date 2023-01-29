package extracells.network.handler.other;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import extracells.network.GuiHandler;
import extracells.network.packet.other.PacketGuiSwitch;

public class HandlerGuiSwitch implements IMessageHandler<PacketGuiSwitch, IMessage> {

    @Override
    public IMessage onMessage(PacketGuiSwitch message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        TileEntity te = message.te;
        if (te == null) {
            GuiHandler.launchGui(message.guiIndex, player, null, 0, 0, 0);
        } else {
            GuiHandler.launchGui(message.guiIndex, player, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
        }
        return null;
    }
}

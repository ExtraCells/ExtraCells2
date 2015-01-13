package extracells.network.packet.other;

import extracells.network.AbstractPacket;
import extracells.part.PartECBase;
import extracells.tileentity.TileEntityFluidFiller;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketFluidContainerSlot extends AbstractPacket {

    private ItemStack container;
    private TileEntityFluidFiller fluidFiller;

    public PacketFluidContainerSlot() {
    }

    public PacketFluidContainerSlot(TileEntityFluidFiller _fluidFiller, ItemStack _container, EntityPlayer _player) {
        super(_player);
        mode = 0;
        fluidFiller = _fluidFiller;
        container = _container;
    }

    public void writeData(ByteBuf out) {
        switch (mode) {
            case 0:
            	writeTileEntity((TileEntity) fluidFiller, out);
                ByteBufUtils.writeItemStack(out, container);
                break;
        }
    }

    public void readData(ByteBuf in) {
        switch (mode) {
            case 0:
            	fluidFiller = (TileEntityFluidFiller) readTileEntity(in);
                container = ByteBufUtils.readItemStack(in);
                break;
        }
    }

    @Override
    public void execute() {
        switch (mode) {
            case 0:
            	container.stackSize = 1;
            	fluidFiller.containerItem = container;
            	if(fluidFiller.hasWorldObj())
            		fluidFiller.getWorldObj().markBlockForUpdate(fluidFiller.xCoord, fluidFiller.yCoord, fluidFiller.zCoord);
            	fluidFiller.postUpdateEvent();
                break;
        }
    }
}

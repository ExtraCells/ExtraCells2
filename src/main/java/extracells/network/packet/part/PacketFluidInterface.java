package extracells.network.packet.part;

import cpw.mods.fml.common.network.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import io.netty.buffer.ByteBuf;
import extracells.container.ContainerFluidInterface;
import extracells.gui.GuiFluidInterface;
import extracells.network.AbstractPacket;

public class PacketFluidInterface extends AbstractPacket {
	
	FluidStack[] tank;
	Integer[] filter;
	int fluidID;
	int filterSlot;
	
	public PacketFluidInterface(){}
	
	public PacketFluidInterface(FluidStack[] _tank, Integer[] _filter, EntityPlayer _player){
		super(_player);
		mode = 0;
		tank = _tank;
		filter = _filter;
	}
	
	public PacketFluidInterface(int _fluidID, int _filterSlot, EntityPlayer _player){
		super(_player);
		mode = 1;
		fluidID = _fluidID;
		filterSlot = _filterSlot;
	}

	@Override
	public void writeData(ByteBuf out) {
		switch(mode){
			case 0:
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("lengthTank", tank.length);
				for (int i = 0; i < tank.length; i++){
					if(tank[i] != null){
						tag.setTag("tank#"+i, tank[i].writeToNBT(new NBTTagCompound()));
					}
				}
				tag.setInteger("lengthFilter", filter.length);
				for (int i = 0; i < filter.length; i++){
					if(filter[i] != null){
						tag.setInteger("filter#"+i, filter[i]);
					}
				}
				ByteBufUtils.writeTag(out, tag);
				break;
			case 1:
				out.writeInt(filterSlot);
				out.writeInt(fluidID);
				break;
			default:
		}
			
				

	}

	@Override
	public void readData(ByteBuf in) {
		switch(mode){
			case 0:
				NBTTagCompound tag = ByteBufUtils.readTag(in);
				tank = new FluidStack[tag.getInteger("lengthTank")];
				for (int i = 0; i < tank.length; i++){
					if(tag.hasKey("tank#"+i))
						tank[i] = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("tank#"+i));
					else
						tank[i] = null;
				}
				filter = new Integer[tag.getInteger("lengthFilter")];
				for (int i = 0; i < filter.length; i++){
					if(tag.hasKey("filter#"+i))
						filter[i] = tag.getInteger("filter#"+i);
					else
						filter[i] = -1;
				}
				break;
			case 1:
				filterSlot = in.readInt();
				fluidID = in.readInt();
				break;
			default:
		}

	}

	@Override
	public void execute() {
		switch(mode){
		case 0:
			EntityPlayer p = Minecraft.getMinecraft().thePlayer;
			if(p.openContainer != null && p.openContainer instanceof ContainerFluidInterface){
				ContainerFluidInterface container = (ContainerFluidInterface) p.openContainer;
				if(Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiFluidInterface){
					GuiFluidInterface gui = (GuiFluidInterface) Minecraft.getMinecraft().currentScreen;
					for (int i = 0; i < tank.length; i++){
						container.fluidInterface.setFluidTank(ForgeDirection.getOrientation(i), tank[i]);
					}
					for (int i = 0; i < filter.length; i++){
						gui.filter[i].setFluid(FluidRegistry.getFluid(filter[i]));
					}
				}
			}
			break;
		case 1:
			if(player.openContainer != null && player.openContainer instanceof ContainerFluidInterface){
				ContainerFluidInterface container = (ContainerFluidInterface) player.openContainer;
				container.fluidInterface.setFilter(ForgeDirection.getOrientation(filterSlot), FluidRegistry.getFluid(fluidID));
			}
			break;
		default:
	}

	}

}

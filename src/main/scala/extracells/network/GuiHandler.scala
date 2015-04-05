package extracells.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.network.IGuiHandler;
import extracells.Extracells;
import extracells.api.IFluidInterface;
import extracells.api.IPortableFluidStorageCell;
import extracells.api.IWirelessFluidTermHandler;
import extracells.container.ContainerFluidCrafter;
import extracells.container.ContainerFluidFiller;
import extracells.container.ContainerFluidInterface;
import extracells.container.ContainerFluidStorage;
import extracells.gui.GuiFluidCrafter;
import extracells.gui.GuiFluidFiller;
import extracells.gui.GuiFluidInterface;
import extracells.gui.GuiFluidStorage;
import extracells.part.PartECBase;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidCrafter;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.tileentity.TileEntityFluidInterface;

object GuiHandler extends IGuiHandler {

	 def getContainer(ID: Int, player: EntityPlayer, args: Array[Any]) : Any = {
		ID match {
		case 0 =>
			val fluidInventory = args.apply(0).asInstanceOf[IMEMonitor[IAEFluidStack]]
			return new ContainerFluidStorage(fluidInventory, player)
		case 1 =>
			val fluidInventory2 = args.apply(0).asInstanceOf[IMEMonitor[IAEFluidStack]]
			val handler = args.apply(1).asInstanceOf[IWirelessFluidTermHandler]
			return new ContainerFluidStorage(fluidInventory2, player, handler)
		case 3 =>
			val fluidInventory3 = args.apply(0).asInstanceOf[IMEMonitor[IAEFluidStack]]
			val storageCell =  args.apply(1).asInstanceOf[IPortableFluidStorageCell]
			return new ContainerFluidStorage(fluidInventory3, player, storageCell)
		case _ =>
			return null
		}
	}

	def getGui(ID: Int, player: EntityPlayer) : Any = {
		ID match {
		case 0 =>
			return new GuiFluidStorage(player, "extracells.part.fluid.terminal.name");
		case 1 =>
			return new GuiFluidStorage(player, "extracells.part.fluid.terminal.name");
		case 3 =>
			return new GuiFluidStorage(player, "extracells.item.storage.fluid.portable.name");
		case _ =>
			return null;
		}
	}

	def getGuiId(guiId: Int) = guiId + 6

	def getGuiId(part: PartECBase) = part.getSide().ordinal()

	def getPartContainer(side: ForgeDirection, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) : Any =
		world.getTileEntity(x, y, z).asInstanceOf[IPartHost].getPart(side).asInstanceOf[PartECBase]
			.getServerGuiElement(player)


	def getPartGui(side: ForgeDirection, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) : Any =
		world.getTileEntity(x, y, z).asInstanceOf[IPartHost].getPart(side).asInstanceOf[PartECBase]
			.getClientGuiElement(player)

	def launchGui(ID: Int, player: EntityPlayer,  args: Array[Any]) {
		temp = args
		player.openGui(Extracells, ID, null, 0, 0, 0);
	}

	def launchGui(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z:Int) : Any =
		player.openGui(Extracells, ID, world, x, y, z);


	var temp: Array[Any] = Array[Any]()

	override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) : AnyRef =  {
		val side = ForgeDirection.getOrientation(ID);
		if (world.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.getBlock()) {
			val tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity == null || !(tileEntity.isInstanceOf[TileEntityFluidCrafter]))
				return null
			return new GuiFluidCrafter(player.inventory, tileEntity.asInstanceOf[TileEntityFluidCrafter].getInventory())
		}
		if (world != null
				&& world.getBlock(x, y, z) == BlockEnum.ECBASEBLOCK.getBlock()) {
			val tileEntity = world.getTileEntity(x, y, z)
			if (tileEntity == null)
				return null
			if (tileEntity.isInstanceOf[TileEntityFluidInterface])
				return new GuiFluidInterface(player,tileEntity.asInstanceOf[IFluidInterface])
			else if (tileEntity.isInstanceOf[TileEntityFluidFiller])
				return new GuiFluidFiller(player, tileEntity.asInstanceOf[TileEntityFluidFiller])
			return null;
		}
		if (world != null && side != ForgeDirection.UNKNOWN)
			return getPartGui(side, player, world, x, y, z).asInstanceOf[AnyRef]
		getGui(ID - 6, player).asInstanceOf[AnyRef]
	}

	override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) : AnyRef =  {
		val side = ForgeDirection.getOrientation(ID)
		if (world != null && world.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.getBlock()) {
			val tileEntity = world.getTileEntity(x, y, z)
			if (tileEntity == null || !(tileEntity.isInstanceOf[TileEntityFluidCrafter]))
				return null
			return new ContainerFluidCrafter(player.inventory,
					tileEntity.asInstanceOf[TileEntityFluidCrafter].getInventory())
		}
		if (world != null && world.getBlock(x, y, z) == BlockEnum.ECBASEBLOCK.getBlock()) {
			val tileEntity = world.getTileEntity(x, y, z)
			if (tileEntity == null)
				return null
			if (tileEntity.isInstanceOf[TileEntityFluidInterface])
				return new ContainerFluidInterface(player, tileEntity.asInstanceOf[IFluidInterface]);
			else if (tileEntity.isInstanceOf[TileEntityFluidFiller])
				return new ContainerFluidFiller(player.inventory, tileEntity.asInstanceOf[TileEntityFluidFiller])
			return null
		}
		if (world != null && side != ForgeDirection.UNKNOWN)
			return getPartContainer(side, player, world, x, y, z).asInstanceOf[AnyRef]
		getContainer(ID - 6, player, temp).asInstanceOf[AnyRef]
	}
}

package extracells.integration.mekanism.gas

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}

import appeng.api.config.FuzzyMode
import appeng.api.storage.IStorageChannel
import extracells.api.gas.IAEGasStack
import extracells.item.ItemGas
import extracells.registries.ItemEnum
import extracells.util.StorageChannels
import io.netty.buffer.ByteBuf
import mekanism.api.gas.{Gas, GasStack}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompressedStreamTools, NBTTagCompound}


class AEGasStack extends IAEGasStack{

  private var canCraft = false
  private var stackSize: Long = 0L
  private var requestable: Long = 0L
  private var gas: Gas = null

  def this(oldStack: AEGasStack){
    this

    this.gas = oldStack.gas
    this.setStackSize(oldStack.getStackSize)

    this.setCraftable(oldStack.isCraftable)
    this.setCountRequestable(oldStack.getCountRequestable)

  }

  def this(gasStack: GasStack){
    this

    if(gasStack == null || gasStack.getGas == null)
      throw new IllegalArgumentException("Gas is null")

    this.gas = gasStack.getGas
    this.setStackSize(gasStack.amount)
    this.setCraftable(false)
    this.setCountRequestable(0)
  }

  def this(nbt: NBTTagCompound){
    this
    this.gas = Gas.readFromNBT(nbt)
    this.setStackSize(nbt.getLong("amount"))
    this.setCraftable(nbt.getBoolean("isCraftable"))
    this.setCountRequestable(nbt.getLong("countRequestable"))
  }

  def this(data: ByteBuf){
    this
    val lenght = data.readInt()

    var bytes: Array[Byte] = new Array[Byte](lenght)
    data.readBytes(bytes)

    val inputStream = new DataInputStream( new ByteArrayInputStream(bytes))
    val nbt = CompressedStreamTools.readCompressed(inputStream)
    this.gas = Gas.readFromNBT(nbt)
    this.setStackSize(nbt.getLong("amount"))
    this.setCraftable(nbt.getBoolean("isCraftable"))
    this.setCountRequestable(nbt.getLong("countRequestable"))
  }

  /**
    *
    * @return { @link mekanism.api.gas.GasStack}
    */
  override def getGasStack: AnyRef = new GasStack(gas, Math.min(Int.MaxValue, stackSize).toInt)

  override def add(stack: IAEGasStack): Unit = {
    if (stack == null) return
    this.incStackSize(stack.getStackSize)
    this.setCountRequestable(this.getCountRequestable + stack.getCountRequestable)
    this.setCraftable(this.isCraftable || stack.isCraftable)
  }

  override def copy(): IAEGasStack = new AEGasStack(this)

  /**
    *
    * @return { @link mekanism.api.gas.Gas}
    */
  override def getGas: AnyRef = gas

  override def decCountRequestable(l: Long): Unit = this.requestable -= l

  override def setStackSize(l: Long): IAEGasStack = {
    stackSize = l
    this
  }

  override def isMeaningful: Boolean = this.stackSize != 0 || this.getCountRequestable > 0 || this.isCraftable

  override def setCountRequestable(l: Long): IAEGasStack = {
    requestable = l
    this
  }

  override def incStackSize(l: Long): Unit = stackSize += l

  override def getCountRequestable: Long = requestable

  override def fuzzyComparison(o: scala.Any, fuzzyMode: FuzzyMode): Boolean = {
    o match{
      case gas: Gas => gas == this.gas
      case gasStack: GasStack => gasStack.getGas == this.gas
      case aeGasStack: AEGasStack => aeGasStack.gas == gas
      case _ => false
    }
  }

  override def empty(): IAEGasStack = {
    val newStack = copy
    newStack.reset
    newStack
  }

  override def isCraftable: Boolean = canCraft

  override def writeToNBT(nbt: NBTTagCompound): Unit = {
    gas.write(nbt)
    nbt.setLong("amount", getStackSize)
    nbt.setBoolean("isCraftable", isCraftable)
    nbt.setLong("countRequestable", getCountRequestable)
  }

  override def setCraftable(b: Boolean): IAEGasStack = {
    canCraft = b
    this
  }

  override def incCountRequestable(l: Long): Unit = requestable += l

  override def getChannel: IStorageChannel[IAEGasStack] = StorageChannels.GAS

  override def writeToPacket(byteBuf: ByteBuf): Unit = {

    val byteOutputStream = new ByteArrayOutputStream()
    val outputStream = new DataOutputStream(byteOutputStream)
    val nbt = new NBTTagCompound

    this.writeToNBT(nbt)

    CompressedStreamTools.writeCompressed(nbt, outputStream)

    val bytes: Array[Byte] = byteOutputStream.toByteArray
    val lenght = bytes.length

    byteBuf.writeInt(lenght)
    byteBuf.writeBytes(bytes)
  }

  override def decStackSize(l: Long): Unit = stackSize -= l

  override def asItemStackRepresentation(): ItemStack = {
    val stack = ItemEnum.GASITEM.getSizedStack(1)
    ItemGas.setGasName(stack, gas.getName)
    stack
  }

  override def getStackSize: Long = stackSize

  override def isItem: Boolean = false

  override def isFluid: Boolean = false

  override def reset(): IAEGasStack = {
    stackSize = 0
    requestable = 0
    canCraft = false
    this
  }

  override def hashCode() = gas.hashCode

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case gasStack : AEGasStack => (this.canCraft == gasStack.canCraft &&
        this.requestable == gasStack.requestable && this.canCraft == gasStack.canCraft && this.gas == gasStack.gas)
      case _ => false
    }
  }
}

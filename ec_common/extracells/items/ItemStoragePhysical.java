package extracells.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.me.items.IStorageCell;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;

public class ItemStoragePhysical extends Item implements IStorageCell
{

	// Localization suffixes
	public static final String[] suffixes = new String[]
	{ "256k", "1024k", "4096k", "16348k", "container", "adjustable" };

	// Bytes
	public static final int[] bytes_cell = new int[]
	{ 262144, 1048576, 4194304, 16777216, 65536 };

	public static final int[] types_cell = new int[]
	{ 63, 63, 63, 63, 1 };

	// Icons
	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemStoragePhysical(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1)
	{
		int j = MathHelper.clamp_int(par1, 0, 5);
		return this.icons[j];
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		this.icons = new Icon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			this.icons[i] = iconRegister.registerIcon("extracells:" + "storage.physical." + suffixes[i]);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		return "item.storage.physical." + suffixes[i];
	}

	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		Boolean hasName = !Util.getCellRegistry().getHandlerForCell(stack).getName().isEmpty();
		String partitionName = Util.getCellRegistry().getHandlerForCell(stack).getName();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		if (stack.getItemDamage() == 4)
		{
			if (used_bytes != 0)
			{
				return StatCollector.translateToLocal(getUnlocalizedName(stack)) + " - " + Util.getCellRegistry().getHandlerForCell(stack).getAvailableItems().getItems().get(0).getDisplayName();
			} else
			{
				return StatCollector.translateToLocal("tooltip.empty") + " " + StatCollector.translateToLocal(getUnlocalizedName(stack));
			}
		} else
		{

			if (hasName)
			{
				return StatCollector.translateToLocal(getUnlocalizedName(stack)) + " - " + partitionName;
			} else
			{
				return StatCollector.translateToLocal(getUnlocalizedName(stack));
			}
		}

	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	public void getSubItems(int i, CreativeTabs creativeTab, List listSubItems)
	{
		for (int j = 0; j < 6; ++j)
		{
			ItemStack toAdd = new ItemStack(i, 1, j);
			
			if (j == 5)
			{
				if (toAdd.stackTagCompound == null)
					toAdd.stackTagCompound = new NBTTagCompound();
				toAdd.stackTagCompound.setInteger("costum_size", 4096);
				toAdd.stackTagCompound.setInteger("costum_types", 27);
			}
			
			listSubItems.add(toAdd);
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		Boolean partitioned = Util.getCellRegistry().getHandlerForCell(stack).isPreformatted();
		Boolean fuzzy = Util.getCellRegistry().getHandlerForCell(stack).isFuzzyPreformatted();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		long total_bytes = Util.getCellRegistry().getHandlerForCell(stack).totalBytes();
		long used_types = Util.getCellRegistry().getHandlerForCell(stack).storedItemTypes();
		long total_types = Util.getCellRegistry().getHandlerForCell(stack).getTotalItemTypes();
		if (stack.getItemDamage() != 4)
		{
			list.add(used_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Of") + " " + total_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.BytesUsed"));
			list.add(used_types + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Of") + " " + total_types + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Types"));
		} else if (stack.getItemDamage() == 4)
		{
			if (used_bytes != 0)
			{
				list.add(StatCollector.translateToLocal("tooltip.block") + ": " + Util.getCellRegistry().getHandlerForCell(stack).getAvailableItems().getItems().get(0).getDisplayName());
			} else
			{
				list.add(StatCollector.translateToLocal("tooltip.block") + ": -");
			}
			list.add(used_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Of") + " " + total_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.BytesUsed"));
		}
		if (partitioned)
		{
			if (fuzzy)
			{
				list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Fuzzy"));
			} else
			{
				list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
			}
		}
	}

	@Override
	public int getBytes(ItemStack i)
	{
		if (i.getItemDamage() == 5)
		{
			if (i.hasTagCompound())
			{
				return i.getTagCompound().getInteger("costum_size");
			} else
			{
				return 0;
			}
		} else
		{
			return bytes_cell[i.getItemDamage()];
		}
	}

	@Override
	public int BytePerType(ItemStack i)
	{
		if (i.getItemDamage() == 5)
		{
			if (i.hasTagCompound())
			{
				if (Math.round(i.getTagCompound().getInteger("costum_types") / 128) == 0)
				{
					return 1;
				} else
				{
					return Math.round(i.getTagCompound().getInteger("costum_types") / 128);
				}
			} else
			{
				return 1;
			}
		} else
		{
			return bytes_cell[i.getItemDamage()] / 128;
		}
	}

	public int getTotalTypes(ItemStack i)
	{
		if (i.getItemDamage() == 5)
		{
			if (i.hasTagCompound())
			{
				return i.getTagCompound().getInteger("costum_types");
			} else
			{
				return 0;
			}
		} else
		{
			return types_cell[i.getItemDamage()];
		}
	}

	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requesetedAddition)
	{
		return false;
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}

	@ForgeSubscribe
	@Override
	public ItemStack onItemRightClick(ItemStack i, World w, EntityPlayer p)
	{
		if (p.isSneaking())
		{
			if (i.getItemDamage() == 0 || i.getItemDamage() == 1 || i.getItemDamage() == 2 || i.getItemDamage() == 3)
			{
				if (Util.getCellRegistry().getHandlerForCell(i).storedItemCount() == 0)
				{
					p.inventory.decrStackSize(p.inventory.currentItem, 1);
					p.inventory.addItemStackToInventory(new ItemStack(Extracells.Cluster, 1, i.getItemDamage()));
					p.inventory.addItemStackToInventory(new ItemStack(Extracells.Casing, 1));
				}
			} else if (i.getItemDamage() == 4)
			{
				if (FMLCommonHandler.instance().getEffectiveSide().isServer())
				{
					switch (i.getTagCompound().getInteger("mode"))
					{
					case 0:
						i.getTagCompound().setInteger("mode", 1);
						p.addChatMessage(StatCollector.translateToLocal("tooltip.mode") + ": " + StatCollector.translateToLocal("tooltip.equaltrade1"));
						break;
					case 1:
						i.getTagCompound().setInteger("mode", 2);
						p.addChatMessage(StatCollector.translateToLocal("tooltip.mode") + ": " + StatCollector.translateToLocal("tooltip.equaltrade3"));
						break;
					case 2:
						i.getTagCompound().setInteger("mode", 0);
						p.addChatMessage(StatCollector.translateToLocal("tooltip.mode") + ": " + StatCollector.translateToLocal("tooltip.placement"));
						break;
					}
				}
			}
		}
		return i;

	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset)
	{
		if (itemstack.getItemDamage() == 4)
		{
			ForgeDirection face = ForgeDirection.getOrientation(side);
			if (world.getBlockId(x + face.offsetX, y + face.offsetY, z + face.offsetZ) == 0 && Util.getCellRegistry().getHandlerForCell(itemstack).storedItemTypes() != 0)
			{
				if (FMLCommonHandler.instance().getEffectiveSide().isServer())
				{
					IAEItemStack request = Util.createItemStack(Util.getCellRegistry().getHandlerForCell(itemstack).getAvailableItems().getItems().get(0).copy());
					ItemStack block = request.getItemStack();
					if (block.getItem() instanceof ItemBlock)
					{
						ItemBlock itemblock = (ItemBlock) request.getItem();
						if (world.getBlockId(x, y, z) != 7)
						{
							switch (itemstack.getTagCompound().getInteger("mode"))
							{
							case 0:
								request.setStackSize(1);
								itemblock.onItemUseFirst(request.getItemStack(), player, world, x, y, z, side, xOffset, yOffset, zOffset);
								itemblock.onItemUse(request.getItemStack(), player, world, x, y, z, side, xOffset, yOffset, zOffset);
								Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
								break;
							case 1:
								request.setStackSize(1);
								world.destroyBlock(x, y, z, true);
								placeBlock(request.getItemStack(), world, player, x, y, z, side, xOffset, yOffset, zOffset);
								Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
								break;
							case 2:

								request.setStackSize(9);
								if (Util.getCellRegistry().getHandlerForCell(itemstack).storedItemCount() > 9)
								{
									switch (ForgeDirection.getOrientation(side))
									{
									case DOWN:
										for (int posX = x - 1; posX < x + 2; posX++)
										{
											for (int posZ = z - 1; posZ < z + 2; posZ++)
											{
												world.destroyBlock(posX, y, posZ, true);
												placeBlock(request.getItemStack(), world, player, x, y, z, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									case EAST:
										for (int posZ = z - 1; posZ < z + 2; posZ++)
										{
											for (int posY = y - 1; posY < y + 2; posY++)
											{
												world.destroyBlock(x, posY, posZ, true);
												placeBlock(request.getItemStack(), world, player, x, posY, posZ, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									case NORTH:
										for (int posX = x - 1; posX < x + 2; posX++)
										{
											for (int posY = y - 1; posY < y + 2; posY++)
											{
												world.destroyBlock(posX, posY, z, true);
												placeBlock(request.getItemStack(), world, player, posX, posY, z, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									case SOUTH:
										for (int posX = x - 1; posX < x + 2; posX++)
										{
											for (int posY = y - 1; posY < y + 2; posY++)
											{
												world.destroyBlock(posX, posY, z, true);
												placeBlock(request.getItemStack(), world, player, posX, posY, z, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									case UNKNOWN:
										break;
									case UP:
										for (int posX = x - 1; posX < x + 2; posX++)
										{
											for (int posZ = z - 1; posZ < z + 2; posZ++)
											{
												world.destroyBlock(posX, y, posZ, true);
												placeBlock(request.getItemStack(), world, player, posX, y, posZ, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									case WEST:
										for (int posZ = z - 1; posZ < z + 2; posZ++)
										{
											for (int posY = y - 1; posY < y + 2; posY++)
											{
												world.destroyBlock(x, posY, posZ, true);
												placeBlock(request.getItemStack(), world, player, x, posY, posZ, side, xOffset, yOffset, zOffset);
											}
										}
										Util.getCellRegistry().getHandlerForCell(itemstack).extractItems(request);
										break;
									default:
										break;
									}
								}
							}
							return true;
						} else
						{
							return false;
						}
					} else
					{
						player.addChatMessage(StatCollector.translateToLocal("tooltip.onlyblocks"));
						return false;
					}
				} else
				{
					return false;
				}
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}

	public void placeBlock(ItemStack itemstack, World world, EntityPlayer player, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset)
	{
		ItemBlock itemblock = (ItemBlock) itemstack.getItem();
		switch (ForgeDirection.getOrientation(side))
		{
		case DOWN:
			itemblock.onItemUseFirst(itemstack, player, world, x, y++, z, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y++, z, side, xOffset, yOffset, zOffset);
			break;
		case EAST:
			itemblock.onItemUseFirst(itemstack, player, world, x--, y, z, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x--, y, z, side, xOffset, yOffset, zOffset);
			break;
		case NORTH:
			itemblock.onItemUseFirst(itemstack, player, world, x, y, z++, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y, z++, side, xOffset, yOffset, zOffset);
			break;
		case SOUTH:
			itemblock.onItemUseFirst(itemstack, player, world, x, y, z--, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y, z--, side, xOffset, yOffset, zOffset);
			break;
		case UNKNOWN:
			break;
		case UP:
			itemblock.onItemUseFirst(itemstack, player, world, x, y--, z, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y--, z, side, xOffset, yOffset, zOffset);
			break;
		case WEST:
			itemblock.onItemUseFirst(itemstack, player, world, x++, y, z, side, xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x++, y, z, side, xOffset, yOffset, zOffset);
			break;
		default:
			break;

		}

	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return i != null && i.getItem() == this;
	}
}

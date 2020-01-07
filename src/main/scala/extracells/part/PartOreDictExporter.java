package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.util.item.AEItemStack;
import extracells.container.ContainerOreDictExport;
import extracells.gui.GuiOreDictExport;
import extracells.models.PartModels;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.util.ItemHandlerUtil;
import extracells.util.ItemUtils;
import extracells.util.MachineSource;
import extracells.util.StorageChannels;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class PartOreDictExporter extends PartECBase implements IGridTickable {

    private String filter = "";
    /**
     * Used when there are ModID(@) and path (~) matchers. Tick-time matching.
     */
    private Predicate<ItemStack> filterPredicate = null;
    /**
     * White list of itemstacks to extract. OreDict only mode.
     */
    private ItemStack[] oreDictFilteredItems = new ItemStack[0];

    @Override
    public float getCableConnectionLength(AECableType aeCableType) {
        return 5.0F;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updateFilter();
        saveData();
    }

    /**
     * Call when the filter string has changed to parse and recompile the filter.
     */
    private void updateFilter() {
        if (!this.filter.trim().isEmpty()) {
            ArrayList<String> matchingNames = new ArrayList<>();
            Predicate<ItemStack> matcher = null;

            String[] filters = this.filter.split("[&|]");
            String lastFilter = null;

            for (String filter : filters) {
                filter = filter.trim();
                boolean negated = filter.startsWith("!");
                if (negated)
                    filter = filter.substring(1);

                Predicate<ItemStack> test = filterToItemStackPredicate(filter);

                if (negated)
                    test = test.negate();

                if (matcher == null) {
                    matcher = test;
                    lastFilter = filter;
                } else {
                    int endLast = this.filter.indexOf(lastFilter) + lastFilter.length();
                    int startThis = this.filter.indexOf(filter);
                    boolean or = this.filter.substring(endLast, startThis).contains("|");

                    if (or) {
                        matcher = matcher.or(test);
                    } else {
                        matcher = matcher.and(test);
                    }
                }
            }

            if (matcher == null) {
                filterPredicate = null;
                oreDictFilteredItems = new ItemStack[0];
                return;
            }

            //Mod name and path evaluation can only be done during tick, can't precompile whitelist for this.
            if (!this.filter.contains("@") && !this.filter.contains("~")) {
                //Precompiled whitelist of oredict itemstacks.
                this.oreDictFilteredItems = Arrays.stream(OreDictionary.getOreNames())
                        .flatMap(name -> OreDictionary.getOres(name).stream())
                        .filter(matcher)
                        .toArray(ItemStack[]::new);
                this.filterPredicate = null;
            } else {
                //Runtime evaluation of filter.
                filterPredicate = matcher;
                this.oreDictFilteredItems = new ItemStack[0];
            }
        } else {
            this.filterPredicate = null;
            this.oreDictFilteredItems = new ItemStack[0];
        }
    }

    /**
     * Given a filter string, returns a predicate that matches a given ItemStack
     *
     * @param filter Filter string.
     * @return Predicate for filter string.
     */
    private Predicate<ItemStack> filterToItemStackPredicate(String filter) {
        if (filter.startsWith("@")) {
            final Predicate<String> test = filterToPredicate(filter.substring(1));
            return (is) -> is != null && !is.isEmpty() &&
                    Optional.ofNullable(is.getItem().getRegistryName())
                            .map(ResourceLocation::getNamespace)
                            .map(test::test)
                            .orElse(false);
        } else if (filter.startsWith("~")) {
            final Predicate<String> test = filterToPredicate(filter.substring(1));
            return (is) -> is != null && !is.isEmpty() &&
                    Optional.ofNullable(is.getItem().getRegistryName())
                            .map(ResourceLocation::getPath)
                            .map(test::test)
                            .orElse(false);
        } else {
            final Predicate<String> test = filterToPredicate(filter);
            return (is) -> is != null && !is.isEmpty() &&
                    IntStream.of(OreDictionary.getOreIDs(is))
                            .mapToObj(OreDictionary::getOreName)
                            .anyMatch(test);
        }
    }

    /**
     * Given a filter string, returns a Predicate that matches a string.
     *
     * @param filter Filter string
     * @return Predicate for filter string.
     */
    private Predicate<String> filterToPredicate(String filter) {
        int numStars = StringUtils.countMatches(filter, '*');
        if (numStars == filter.length()) {
            return (str) -> true;
        } else if (filter.length() > 2 && filter.startsWith("*") && filter.endsWith("*") && numStars == 2) {
            final String pattern = filter.substring(1, filter.length() - 1);
            return (str) -> str.contains(pattern);
        } else if (filter.length() >= 2 && filter.startsWith("*") && numStars == 1) {
            final String pattern = filter.substring(1);
            return (str) -> str.endsWith(pattern);
        } else if (filter.length() >= 2 && filter.endsWith("*") && numStars == 1) {
            final String pattern = filter.substring(0, filter.length() - 1);
            return (str) -> str.startsWith(pattern);
        } else if (numStars == 0) {
            return (str) -> str.equals(filter);
        } else {
            String filterRegexFragment = filter.replace("*", ".*");
            String regexPattern = "^" + filterRegexFragment + "$";
            final Pattern pattern = Pattern.compile(regexPattern);
            return pattern.asPredicate();
        }
    }

//    private boolean isItemValid(IAEItemStack s) {
//        if (s == null || s.getStackSize() < 1 || this.filter.equals("")) {
//            return false;
//        }
//
//        int[] ids = OreDictionary.getOreIDs(s.createItemStack());
//        ResourceLocation identifier = s.getItem().getRegistryName();
//
//        String[] filters = StringUtils.split(this.filter, '&');
//
//        boolean result = true;
//        for (String filter : filters) {
//            filter = filter.trim();
//
//            boolean exclude = filter.startsWith("!");
//            if (exclude) {
//                filter = filter.substring(1);
//            }
//
//            if (filter.startsWith("@")) {
//                filter = filter.substring(1);
//                if (!checkFilter(exclude, filter, identifier.getNamespace())) {
//                    result = false;
//                }
//            } else if (filter.startsWith("~")) {
//                filter = filter.substring(1);
//                if (!checkFilter(exclude, filter, identifier.getPath())) {
//                    result = false;
//                }
//            } else {
//                if (ids.length == 0) {
//                    result = false;
//                } else {
//                    boolean oreDictResult = true;
//                    for (int id : ids) {
//                        if (!checkFilter(exclude, filter, OreDictionary.getOreName(id))) {
//                            oreDictResult = false;
//                        } else if (!exclude) {
//                            oreDictResult = true;
//                            break;
//                        }
//                    }
//
//                    if (!oreDictResult) {
//                        result = false;
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private boolean checkFilter(Boolean exclude, String filter, String name) {
//        if (filter.startsWith("*") && filter.endsWith("*")) {
//            filter = filter.replace("*", "");
//            if (filter.equals("")) {
//                return true;
//            }
//
//            if (exclude && name.contains(filter)) {
//                return false;
//            }
//            if (!exclude && !name.contains(filter)) {
//                return false;
//            }
//        } else if (filter.startsWith("*")) {
//            filter = filter.replace("*", "");
//            if (exclude && name.endsWith(filter)) {
//                return false;
//            }
//            if (!exclude && !name.endsWith(filter)) {
//                return false;
//            }
//        } else if (filter.endsWith("*")) {
//            filter = filter.replace("*", "");
//            if (exclude && name.startsWith(filter)) {
//                return false;
//            }
//            if (!exclude && !name.startsWith(filter)) {
//                return false;
//            }
//        } else {
//            if (exclude && name.equals(filter)) {
//                return false;
//            }
//            if (!exclude && !name.equals(filter)) {
//                return false;
//            }
//        }
//
//        return true;
//    }

    public boolean doWork(int rate, int ticksSinceLastCall) {
        int amount = Math.min(rate * ticksSinceLastCall, 64);
        IStorageGrid storage = getStorageGrid();
        assert storage != null;
        IMEMonitor<IAEItemStack> inv = storage.getInventory(StorageChannels.ITEM());
        MachineSource src = new MachineSource(this);

        if (this.filterPredicate != null) {
            //Tick-time filter evaluation.
            IItemList<IAEItemStack> items = inv.getStorageList();
            for (IAEItemStack stack : items) {
                if (stack == null || !this.filterPredicate.test(stack.createItemStack()))
                    continue;

                IAEItemStack toExtract = stack.copy();
                toExtract.setStackSize(amount);

                IAEItemStack extracted = inv.extractItems(toExtract, Actionable.SIMULATE, src);
                if (extracted != null) {
                    IAEItemStack exported = exportStack(extracted.copy());
                    if (exported != null) {
                        inv.extractItems(exported, Actionable.MODULATE, src);
                        return true;
                    }
                }
            }
            return false;
        } else {
            //Precompiled oredict whitelist
            for (ItemStack is : this.oreDictFilteredItems) {
                if (is == null)
                    continue;

                ItemStack toExtract = is.copy();
                toExtract.setCount(amount);

                if (toExtract.isEmpty())
                    continue;

                IAEItemStack extracted = inv.extractItems(AEItemStack.fromItemStack(toExtract), Actionable.SIMULATE, src);
                if (extracted != null) {
                    IAEItemStack exported = exportStack(extracted.copy());
                    if (exported != null) {
                        inv.extractItems(exported, Actionable.MODULATE, src);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public IAEItemStack exportStack(IAEItemStack stack0) {
        DimensionalCoord location = getLocation();
        if (location == null || stack0 == null) {
            return null;
        }
        EnumFacing facing = getFacing();
        BlockPos pos = location.getPos();
        TileEntity tile = location.getWorld().getTileEntity(pos.offset(facing));
        if (tile == null) {
            return null;
        }
        IAEItemStack stack = stack0.copy();
        if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
            IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            ItemStack itemStack = stack.createItemStack();
            ItemStack notExported = ItemHandlerUtil.insertItemStack(itemHandler, itemStack, false);
            if (notExported.isEmpty())
                return stack;
            else
                return stack.setStackSize(stack.getStackSize() - notExported.getCount());
        } else if (tile instanceof IInventory) {
            if (tile instanceof ISidedInventory) {
                ISidedInventory inv = (ISidedInventory) tile;
                for (int i : inv.getSlotsForFace(facing.getOpposite())) {
                    if (inv.canInsertItem(i, stack.createItemStack(), facing.getOpposite())) {
                        ItemStack invStack = inv.getStackInSlot(i);
                        if (invStack == null || invStack.isEmpty()) {
                            inv.setInventorySlotContents(i,
                                    stack.createItemStack());
                            return stack0;
                        } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                inv.getStackInSlot(i), stack.createItemStack())) {
                            int max = inv.getInventoryStackLimit();
                            int current = inv.getStackInSlot(i).getCount();
                            int outStack = (int) stack.getStackSize();
                            if (max == current) {
                                continue;
                            }
                            if (current + outStack <= max) {
                                ItemStack s = inv.getStackInSlot(i).copy();
                                s.setCount(s.getCount() + outStack);
                                inv.setInventorySlotContents(i, s);
                                return stack0;
                            } else {
                                ItemStack s = inv.getStackInSlot(i).copy();
                                s.setCount(max);
                                inv.setInventorySlotContents(i, s);
                                stack.setStackSize(max - current);
                                return stack;
                            }
                        }
                    }
                }
            } else {
                IInventory inv = (IInventory) tile;
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (inv.isItemValidForSlot(i, stack.createItemStack())) {
                        ItemStack invStack = inv.getStackInSlot(i);
                        if (invStack == null || invStack.isEmpty()) {
                            inv.setInventorySlotContents(i, stack.createItemStack());
                            return stack0;
                        } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                inv.getStackInSlot(i), stack.createItemStack())) {
                            int max = inv.getInventoryStackLimit();
                            int current = inv.getStackInSlot(i).getCount();
                            int outStack = (int) stack.getStackSize();
                            if (max == current) {
                                continue;
                            }
                            if (current + outStack <= max) {
                                ItemStack s = inv.getStackInSlot(i).copy();
                                s.setCount(s.getCount() + outStack);
                                inv.setInventorySlotContents(i, s);
                                return stack0;
                            } else {
                                ItemStack s = inv.getStackInSlot(i).copy();
                                s.setCount(max);
                                inv.setInventorySlotContents(i, s);
                                stack.setStackSize(max - current);
                                return stack;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 12, 10, 10, 13);
        bch.addBox(4, 4, 13, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiOreDictExport(player, this);
    }

    @Override
    public ItemStack getItemStack(PartItemStack type) {
        ItemStack is = new ItemStack(ItemEnum.PARTITEM.getItem(), 1,
                PartEnum.getPartID(this));
        if (type != PartItemStack.BREAK) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("filter", this.filter);
            is.setTagCompound(tag);
        }
        return is;
    }

    @Override
    public double getPowerUsage() {
        return 10.0D;
    }

    @Override
    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerOreDictExport(player, this);
    }

    private IStorageGrid getStorageGrid() {
        IGridNode node = getGridNode();
        if (node == null) {
            return null;
        }
        IGrid grid = node.getGrid();
        if (grid == null) {
            return null;
        }
        return grid.getCache(IStorageGrid.class);
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public List<String> getWailaBodey(NBTTagCompound data, List<String> list) {
        super.getWailaBodey(data, list);
        if (data.hasKey("name")) {
            list.add(I18n
                    .translateToLocal("extracells.tooltip.oredict")
                    + ": "
                    + data.getString("name"));
        } else {
            list.add(I18n
                    .translateToLocal("extracells.tooltip.oredict") + ":");
        }
        return list;
    }

    @Override
    public NBTTagCompound getWailaTag(NBTTagCompound tag) {
        super.getWailaTag(tag);
        tag.setString("name", this.filter);
        return tag;
    }

    @MENetworkEventSubscribe
    public void powerChange(MENetworkPowerStatusChange event) {
        IGridNode node = getGridNode();
        if (node != null) {
            boolean isNowActive = node.isActive();
            if (isNowActive != isActive()) {
                setActive(isNowActive);
                onNeighborChanged();
                getHost().markForUpdate();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("filter")) {
            this.filter = data.getString("filter");
        } else {
            this.filter = "";
        }
        updateFilter();
    }

    @Override
    public IPartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return PartModels.EXPORT_HAS_CHANNEL;
        } else if (isPowered()) {
            return PartModels.EXPORT_ON;
        }
        return PartModels.EXPORT_OFF;
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node,
                                                   int ticksSinceLastCall) {
        if (isActive()) {
            return doWork(10, ticksSinceLastCall) ? TickRateModulation.FASTER
                    : TickRateModulation.SLOWER;
        }
        return TickRateModulation.SLOWER;
    }

    @MENetworkEventSubscribe
    public void updateChannels(MENetworkChannelsChanged channel) {
        IGridNode node = getGridNode();
        if (node != null) {
            boolean isNowActive = node.isActive();
            if (isNowActive != isActive()) {
                setActive(isNowActive);
                onNeighborChanged();
                getHost().markForUpdate();
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("filter", this.filter);
    }
}

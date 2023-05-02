package extracells.util;

import java.util.List;

import net.minecraft.util.EnumChatFormatting;

public final class DeprecationWarning {

    private DeprecationWarning() {}

    public static void addContentTransferInfo(List<String> tooltip) {
        tooltip.add(EnumChatFormatting.RED + "Contents will be kept when converting it in crafting table.");
        tooltip.add(EnumChatFormatting.RED + "You don't need to ME-IO to transfer contents.");
    }

    public static void addGeneralDeprecationWarning(List<String> tooltip) {
        tooltip.add(EnumChatFormatting.RED + "EC2 is going to be removed in the future!");
        tooltip.add(EnumChatFormatting.RED + "Try to put it in crafting table to convert it into AE2/AE2FC device.");
    }
}

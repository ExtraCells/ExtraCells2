package extracells.render;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public enum TextureManager {
    BUS_SIDE(TextureType.PART, "bus_side"),
    BUS_BORDER(TextureType.PART, "bus_border"),
    BUS_COLOR(TextureType.PART, "bus_color_border", "bus_color_point"),
    EXPORT_FRONT(TextureType.PART, "export_front_1", "export_front_2"),
    EXPORT_SIDE(TextureType.PART, "export_side"),
    IMPORT_FRONT(TextureType.PART, "import_front_1", "import_front_2"),
    IMPORT_SIDE(TextureType.PART, "import_side"),
    STORAGE_FRONT(TextureType.PART, "storage_front_1", "storage_front_2"),
    STORAGE_SIDE(TextureType.PART, "storage_side"),
    TERMINAL_FRONT(TextureType.PART, "terminal_front_1", "terminal_front_2", "terminal_front_3"),
    TERMINAL_SIDE(TextureType.PART, "terminal_side"),
    PANE_FRONT(TextureType.PART, "pane_front_1", "pane_front_2", "pane_front_3"),
    PANE_SIDE(TextureType.PART, "pane_side"),
    DRIVE_FRONT(TextureType.PART, "drive_front_1", "drive_front_2", "drive_front_3", "drive_front_4"),
    DRIVE_SIDE(TextureType.PART, "drive_side"),
    BATTERY_FRONT(TextureType.PART, "battery_front_1"),
    BATTERY_SIDE(TextureType.PART, "battery_side"),
    LEVEL_FRONT(TextureType.PART, "level_front_1", "level_front_2", "level_front_3"),
    LEVEL_SIDE(TextureType.PART, "level_side");

    private TextureType textureType;
    private String[] textureNames;
    private IIcon[] textures;

    TextureManager(TextureType _textureType, String... _textureName) {
        textureType = _textureType;
        textureNames = _textureName;
        textures = new IIcon[textureNames.length];
    }

    public void registerTexture(TextureMap textureMap) {

        if (!(textureMap.getTextureType() == 0 && (textureType == TextureType.BLOCK || textureType == TextureType.PART)) && !(textureMap.getTextureType() == 1 && textureType == TextureType.ITEM))
            return;

        for (int i = 0; i < textureNames.length; i++) {
            if (textureType == TextureType.PART)
                textures[i] = textureMap.registerIcon("extracells:part/" + textureNames[i]);
            if (textureType == TextureType.BLOCK || textureType == TextureType.ITEM)
                textures[i] = textureMap.registerIcon("extracells:" + textureNames[i]);
        }
    }

    public IIcon[] getTextures() {
        return textures;
    }

    public IIcon getTexture() {
        return textures[0];
    }

    private enum TextureType {
        ITEM,
        BLOCK,
        PART
    }
}

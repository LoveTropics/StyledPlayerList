package eu.pb4.styledplayerlist;

import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.resources.ResourceLocation;

public class SPLHelper {
    public static final PlaceholderContext.ViewObject PLAYER_LIST_VIEW = PlaceholderContext.ViewObject.of(new ResourceLocation("styled_player_list", "player_list"));
    public static final PlaceholderContext.ViewObject PLAYER_NAME_VIEW = PlaceholderContext.ViewObject.of(new ResourceLocation("styled_player_list", "player_name"));
}

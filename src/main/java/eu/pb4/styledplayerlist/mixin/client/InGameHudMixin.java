package eu.pb4.styledplayerlist.mixin.client;

import eu.pb4.styledplayerlist.config.ConfigManager;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;size()I"), require = 0)
    private int styledPlayerList$replaceWithZero(Collection instance) {
        return ConfigManager.getConfig().configData.displayOnSingleplayer ? 999 : instance.size();
    }
}

package eu.pb4.styledplayerlist.mixin.client;

import eu.pb4.styledplayerlist.config.ConfigManager;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(ForgeGui.class)
public class InGameHudMixin {
    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Ljava/util/Collection;size()I"), require = 0, remap = false)
    private int styledPlayerList$replaceWithZero(Collection instance) {
        return ConfigManager.getConfig().configData.displayOnSingleplayer ? 999 : instance.size();
    }
}

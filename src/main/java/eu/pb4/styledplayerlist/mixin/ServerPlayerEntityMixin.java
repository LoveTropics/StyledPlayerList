package eu.pb4.styledplayerlist.mixin;

import eu.pb4.styledplayerlist.config.ConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void styledPlayerList$changePlayerListName(CallbackInfoReturnable<Component> cir) {
        try {
            if (ConfigManager.isEnabled() && ConfigManager.getConfig().configData.playerName.changePlayerName) {
                cir.setReturnValue(ConfigManager.getConfig().formatPlayerUsername((ServerPlayer) (Object) this));
            }
        } catch (Exception e) {

        }
    }
}

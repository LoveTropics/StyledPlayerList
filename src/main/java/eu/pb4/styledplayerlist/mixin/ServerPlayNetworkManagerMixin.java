package eu.pb4.styledplayerlist.mixin;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.styledplayerlist.SPLHelper;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.config.ConfigManager;
import eu.pb4.styledplayerlist.config.DefaultValues;
import eu.pb4.styledplayerlist.config.PlayerListStyle;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkManagerMixin extends ServerCommonPacketListenerImpl implements PlayerListViewerHolder {

    @Shadow public ServerPlayer player;

    @Unique
    private PlayerListStyle styledPlayerList$style = DefaultValues.EMPTY_STYLE;

    @Unique
    private int styledPlayerList$animationTick = 0;

    public ServerPlayNetworkManagerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void styledPlayerList$loadData(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        try {
            this.styledPlayerList$reloadStyle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void styledPlayerList$updatePlayerList(CallbackInfo ci) {
        if (ConfigManager.isEnabled()) {
            var tick = this.server.getTickCount();
            ConfigData config = ConfigManager.getConfig().configData;

            if (tick % this.styledPlayerList$style.updateRate == 0) {
                var context = PlaceholderContext.of(this.player, SPLHelper.PLAYER_LIST_VIEW);
                this.send(new ClientboundTabListPacket(this.styledPlayerList$style.getHeader(context, this.styledPlayerList$animationTick), this.styledPlayerList$style.getFooter(context, this.styledPlayerList$animationTick)));
                this.styledPlayerList$animationTick += 1;
            }
        }
    }

    @Override
    public void styledPlayerList$reloadStyle() {
        var style = ConfigManager.getStyle();
        if (style != this.styledPlayerList$style) {
            this.styledPlayerList$style = style;
            this.styledPlayerList$animationTick = 0;
        }
    }
}

package eu.pb4.styledplayerlist.mixin;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.styledplayerlist.SPLHelper;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.config.ConfigManager;
import eu.pb4.styledplayerlist.config.DefaultValues;
import eu.pb4.styledplayerlist.config.PlayerListStyle;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

import static eu.pb4.styledplayerlist.PlayerList.id;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkManagerMixin implements PlayerListViewerHolder {

    @Shadow public ServerPlayer player;

    @Shadow public abstract void send(Packet<?> packet);

    @Shadow @Final private MinecraftServer server;

    @Unique
    private String styledPlayerList$activeStyle = ConfigManager.getDefault();

    @Unique
    private PlayerListStyle styledPlayerList$style = DefaultValues.EMPTY_STYLE;

    @Unique
    private int styledPlayerList$animationTick = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void styledPlayerList$loadData(MinecraftServer server, Connection connection, ServerPlayer player, CallbackInfo ci) {
        try {
            StringTag style = PlayerDataApi.getGlobalDataFor(player, id("style"), StringTag.TYPE);

            if (style != null) {
                this.styledPlayerList$setStyle(style.getAsString());
            } else {
                this.styledPlayerList$reloadStyle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void styledPlayerList$updatePlayerList(CallbackInfo ci) {
        if (ConfigManager.isEnabled() && SPLHelper.shouldSendPlayerList(this.player)) {
            var tick = this.server.getTickCount();
            ConfigData config = ConfigManager.getConfig().configData;

            if (tick % this.styledPlayerList$style.updateRate == 0) {
                var context = PlaceholderContext.of(this.player, SPLHelper.PLAYER_LIST_VIEW);
                this.send(new ClientboundTabListPacket(this.styledPlayerList$style.getHeader(context, this.styledPlayerList$animationTick), this.styledPlayerList$style.getFooter(context, this.styledPlayerList$animationTick)));
                this.styledPlayerList$animationTick += 1;
            }

            if (config.playerName.playerNameUpdateRate > 0 && tick % config.playerName.playerNameUpdateRate == 0) {
                this.styledPlayerList$updateName();
            }

        }
    }

    @Inject(method = "broadcastChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;detectRateSpam()V"))
    private void styledPlayerList$onMessage(PlayerChatMessage signedMessage, CallbackInfo ci) {
        if (ConfigManager.isEnabled() && ConfigManager.getConfig().configData.playerName.updatePlayerNameEveryChatMessage) {
            this.styledPlayerList$updateName();
        }
    }

    @Override
    public void styledPlayerList$setStyle(String key) {
        if (ConfigManager.isEnabled()) {
            if (ConfigManager.styleExist(key)) {
                this.styledPlayerList$activeStyle = key;
            } else {
                this.styledPlayerList$activeStyle = ConfigManager.getDefault();
            }
        } else {
            this.styledPlayerList$activeStyle = "default";
        }
        styledPlayerList$reloadStyle();

        PlayerDataApi.setGlobalDataFor(this.player, id("style"), StringTag.valueOf(this.styledPlayerList$activeStyle));
    }


    @Override
    public String styledPlayerList$getStyle() {
        return this.styledPlayerList$activeStyle;
    }

    @Override
    public void styledPlayerList$updateName() {
        try {
            if (ConfigManager.isEnabled() && ConfigManager.getConfig().configData.playerName.changePlayerName) {
                ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), List.of(this.player));
                this.server.getPlayerList().broadcastAll(packet);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void styledPlayerList$reloadStyle() {
        var style = ConfigManager.getStyle(this.styledPlayerList$activeStyle);
        if (style != this.styledPlayerList$style) {
            this.styledPlayerList$style = style;
            this.styledPlayerList$animationTick = 0;
        }
    }

    @Override
    public int styledPlayerList$getAndIncreaseAnimationTick() {
        return this.styledPlayerList$animationTick++;
    }

    @Override
    public PlayerListStyle styledPlayerList$getStyleObject() {
        return this.styledPlayerList$style;
    }
}

package eu.pb4.styledplayerlist.access;

import eu.pb4.styledplayerlist.config.PlayerListStyle;

public interface PlayerListViewerHolder {
    void styledPlayerList$reloadStyle();
    int styledPlayerList$getAndIncreaseAnimationTick();

    PlayerListStyle styledPlayerList$getStyleObject();
}

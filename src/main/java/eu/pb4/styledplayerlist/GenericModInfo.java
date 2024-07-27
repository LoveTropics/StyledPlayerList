package eu.pb4.styledplayerlist;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericModInfo {
    private static final int COLOR = 0x3d8eff;

    private static Component[] icon = new Component[0];
    private static Component[] about = new Component[0];
    private static Component[] consoleAbout = new Component[0];

    public static void build(ModContainer container) {
        var github = container.getMetadata().getContact().get("sources").orElse("UNKNOWN");
        {
            final String chr = "█";
            var icon = new ArrayList<MutableComponent>();
            try {
                var source = ImageIO.read(Files.newInputStream(container.getPath("assets/styled_player_list/icon_ingame.png")));

                for (int y = 0; y < source.getHeight(); y++) {
                    var base = Component.literal("");
                    int line = 0;
                    int color = source.getRGB(0, y) & 0xFFFFFF;
                    for (int x = 0; x < source.getWidth(); x++) {
                        int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                        if (color == colorPixel) {
                            line++;
                        } else {
                            base.append(Component.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                            color = colorPixel;
                            line = 1;
                        }
                    }

                    base.append(Component.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                    icon.add(base);
                }
                GenericModInfo.icon = icon.toArray(new Component[0]);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

        var contributors = new ArrayList<String>();

        container.getMetadata().getAuthors().forEach(x -> contributors.add(x.getName()));
        container.getMetadata().getContributors().forEach(x -> contributors.add(x.getName()));

        var about = new ArrayList<Component>();
        var extraData = Component.empty();
        try {
            extraData.append(Component.literal("[")
                    .append(Component.literal("Contributors")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal(String.join("\n", contributors))
                                    ))
                            ))
                    .append("] ")
            ).append(Component.literal("[")
                    .append(Component.literal("Github")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, github))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal(github)
                                    ))
                            ))
                    .append("]")).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));

            about.add(Component.empty()
                    .append(Component.literal( container.getMetadata().getName() + " ").setStyle(Style.EMPTY.withColor(COLOR).withBold(true)))
                    .append(Component.literal(container.getMetadata().getVersion().getFriendlyString()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

            about.add(Component.literal("» " + container.getMetadata().getDescription()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

            about.add(extraData);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        GenericModInfo.consoleAbout = about.toArray(new Component[0]);

        if (icon.length == 0) {
            GenericModInfo.about = GenericModInfo.consoleAbout;
        } else {
            var output = new ArrayList<Component>();
            about.clear();
            try {
                about.add(Component.literal(container.getMetadata().getName()).setStyle(Style.EMPTY.withColor(COLOR).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, github))));
                about.add(Component.literal("Version: ").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                        .append(Component.literal(container.getMetadata().getVersion().getFriendlyString()).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

                about.add(extraData);
                about.add(Component.empty());

                var desc = new ArrayList<>(List.of(container.getMetadata().getDescription().split(" ")));

                if (desc.size() > 0) {
                    StringBuilder descPart = new StringBuilder();
                    while (!desc.isEmpty()) {
                        (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));

                        if (descPart.length() > 16) {
                            about.add(Component.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
                            descPart = new StringBuilder();
                        }
                    }

                    if (descPart.length() > 0) {
                        about.add(Component.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
                    }
                }

                if (icon.length > about.size() + 2) {
                    int a = 0;
                    for (int i = 0; i < icon.length; i++) {
                        if (i == (icon.length - about.size() - 1) / 2 + a && a < about.size()) {
                            output.add(icon[i].copy().append("  ").append(about.get(a++)));
                        } else {
                            output.add(icon[i]);
                        }
                    }
                } else {
                    Collections.addAll(output, icon);
                    output.addAll(about);
                }
            } catch (Exception e) {
                e.printStackTrace();
                var invalid = Component.literal("/!\\ [ Invalid about mod info ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true));

                output.add(invalid);
                about.add(invalid);
            }

            GenericModInfo.about = output.toArray(new Component[0]);
        }
    }

    public static Component[] getIcon() {
        return icon;
    }

    public static Component[] getAboutFull() {
        return about;
    }

    public static Component[] getAboutConsole() {
        return consoleAbout;
    }
}

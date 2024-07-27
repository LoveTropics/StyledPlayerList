package eu.pb4.styledplayerlist.config;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.styledplayerlist.config.data.StyleData;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PlayerListStyle {
    public final String id;
    public final String name;

    public final AnimatedText header;
    public final AnimatedText footer;

    public final int updateRate;

    public final boolean hidden;

    public PlayerListStyle(String id, StyleData data) {
        this.id = id;
        this.name = data.name;
        this.updateRate = data.updateRate;

        this.header = AnimatedText.from(data.header);
        this.footer = AnimatedText.from(data.footer);
        this.hidden = data.hidden;
    }

    public Component getHeader(PlaceholderContext context, int tick) {
        return this.header.getFor(tick).toText(context);
    }

    public Component getFooter(PlaceholderContext context, int tick) {
        return this.footer.getFor(tick).toText(context);
    }

    public interface AnimatedText {
        TextNode getFor(int tick);

        static AnimatedText from(StyleData.ElementList elementList) {
            if (elementList.values.isEmpty()) {
                return AnimatedText.of(TextNode.empty());
            }

            var joiner = "\n<r>";

            if (elementList.values.size() == 1) {
                return AnimatedText.of(Config.PARSER.parseNode(String.join(joiner, elementList.values.get(0))));
            } else {
                var list = new ArrayList<TextNode>();
                for (var x : elementList.values) {
                    list.add(Config.PARSER.parseNode(String.join(joiner, x)));
                }
                return of(list, Math.max(elementList.changeRate, 1));
            }
        }

        static AnimatedText of(TextNode node) {
            return x -> node;
        }

        static AnimatedText of(List<TextNode> node, int time) {
            return x -> node.get((x / time) % node.size());
        }
    }
}

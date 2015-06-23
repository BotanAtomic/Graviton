package graviton.game.packet.manager.account;

import graviton.api.Packet;
import graviton.api.PacketParser;
import graviton.network.game.GameClient;

/**
 * Created by Botan on 21/06/2015.
 */
@Packet("AP")
public class GetRandomPseudo implements PacketParser {
    @Override
    public void parse(GameClient client, String packet) {
        String[] dictionary = {"ae", "au", "ao", "ap", "ka", "ha", "ah",
                "na", "hi", "he", "eh", "an", "ma", "wa", "we", "wh", "sk", "sa",
                "se", "ne", "ra", "re", "ru", "ri", "ro", "za", "zu", "ta", "te",
                "ty", "tu", "ti", "to", "pa", "pe", "py", "pu", "pi", "po", "da",
                "de", "du", "di", "do", "fa", "fe", "fu", "fi", "fo", "ga", "gu",
                "ja", "je", "ju", "ji", "jo", "la", "le", "lu", "ma", "me", "mu",
                "mo", "radio", "kill", "explode", "craft", "fight", "shadow",
                "bouftou", "bouf", "piou", "piaf", "champ", "abra", "grobe",
                "krala", "sasa", "nianne", "miaou", "was", "killed", "born",
                "storm", "lier", "arm", "hand", "mind", "create", "random", "nick",
                "error", "end", "life", "die", "cut", "make", "spawn", "respawn",
                "zaap", "zaapis", "mobs", "google", "firefox", "rapta", "ewplorer",
                "men", "women", "dark", "eau", "get", "set", "geek", "nolife",
                "spell", "boost", "gift", "leave", "smiley", "blood", "jean",
                "yes", "eays", "skha", "rock", "stone", "fefe", "sadi", "sacri",
                "osa", "panda", "xel", "rox", "stuff", "spoon", "days", "mouarf", "beau", "sexe"};

        client.send("AP" + dictionary[(int) (Math.random() * dictionary.length - 1)] + dictionary[(int) (Math.random() * dictionary.length - 1)]);
    }
}

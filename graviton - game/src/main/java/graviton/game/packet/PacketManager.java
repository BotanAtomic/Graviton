package graviton.game.packet;

import com.google.common.reflect.ClassPath;
import com.google.inject.Singleton;
import graviton.api.Manager;
import graviton.api.PacketParser;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Created by Botan on 20/06/2015.
 */
@Singleton
public class PacketManager implements Manager {

    @Getter
    private Map<String, PacketParser> packets;

    public PacketManager() {
        this.packets = new ConcurrentHashMap<>();
    }

    @Override
    public void configure() {/**
        try {
            for (Class<?> packetClass : getAllClass()) {
                for (Annotation annotation : packetClass.getAnnotations()) {
                    if (annotation instanceof Packet)
                        this.packets.put(((Packet) annotation).value(), (PacketParser) packetClass.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }**/
    }

    private List<Class<?>> getAllClass() {
        List<Class<?>> allClass = new ArrayList<>();
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            allClass.addAll(ClassPath.from(loader).getTopLevelClasses().stream().filter(info -> info.getName().startsWith("graviton.game.packet.manager.")).map(ClassPath.ClassInfo::load).collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allClass;
    }

    @Override
    public void stop() {
        this.packets.clear();
    }
}

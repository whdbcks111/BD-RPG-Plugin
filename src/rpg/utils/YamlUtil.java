package rpg.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import rpg.main.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SuppressWarnings("ALL")
public class YamlUtil {
    public static void saveYaml(String path, Map<String, Object> map) {
        File file = new File(Main.getPlugin().getDataFolder(), path);
        try {
            if(!file.exists()) {
                if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
                file.createNewFile();
            }
            final DumperOptions options = new DumperOptions();
            options.setAllowUnicode(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            final Yaml yaml = new Yaml(options);
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            yaml.dump(map, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> readYaml(String path) {
        File file = new File(Main.getPlugin().getDataFolder(), path);
        if(!file.exists()) return null;
        try {
            return new Yaml().load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

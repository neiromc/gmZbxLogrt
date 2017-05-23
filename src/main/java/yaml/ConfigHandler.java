package yaml;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Usage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Created by neiro on 22.05.17.
 */
public class ConfigHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigHandler.class);

    private Config config;

    private static ConfigHandler configHandler;

    //Singleton
    public ConfigHandler() {
    }

    public synchronized static ConfigHandler getInstanse() {
        if ( configHandler == null)
            configHandler = new ConfigHandler();

        return configHandler;
    }

    private boolean argsValidator(String[] args) {
        if (args == null || args.length == 0) {
            return Usage.showWithError("Config file must sets as command line argument");
        }

        if (args[0] == null) {
            return Usage.showWithError("Config file must sets as command line argument");
        }

        return true;
    }

    public boolean load(String[] args) {
        if ( ! argsValidator(args) )
            return false;

        String fileName = args[0];

        try {
            YamlReader yamlReader = new YamlReader(new FileReader(fileName));
            config = yamlReader.read(Config.class);
        } catch (FileNotFoundException e) {
            Usage.showWithError("Config file " + fileName + "not found");
//            e.printStackTrace();
            return false;
        } catch (YamlException e) {
            Usage.showWithError("Can't read config file: " + fileName);
            if ( logger.isDebugEnabled() ) {
                logger.debug(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
//                e.printStackTrace();
            }
            return false;
        }

        logger.info("Config file {} loaded succesfull", fileName);
        return true;
    }

    public Config getConfig() {
        return config;
    }
}

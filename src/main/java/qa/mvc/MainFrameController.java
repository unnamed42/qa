package qa.mvc;

import lombok.extern.java.Log;
import lombok.val;
import qa.api.QAApi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;

@Log
class MainFrameController implements AutoCloseable {

    private static final String configFileName = "app.conf";

    private final QAApi api = new QAApi();
    private final Properties configs = new Properties();

    MainFrameController() {
        this.init();
    }

    private void init() {
        val conf = new File(configFileName);
        if(!conf.exists()) return;
        try(val is = new FileInputStream(conf);
            val reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            configs.load(reader);
            if(configs.containsKey("password")) {
                val username = configs.getProperty("username");
                val password = configs.getProperty("password");
                api.getJsessinId(username, password).map(api::getUserId);
            }
        } catch(FileNotFoundException ignored) {
        } catch(IOException e) {
            log.log(Level.WARNING, "IOException thrown while reading config file");
        }
    }

    public void close() {
        api.close();
    }


}

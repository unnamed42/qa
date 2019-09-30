package qa.api;

import lombok.val;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Getter
@Log
public class QAProperties {

    private final String baseUrl;
    private final String dateFormat;

    private final Pattern patternUserId;
    private final Pattern patternJsessionId;
    private final Pattern patternDate;

    QAProperties() {
        val props = new Properties();
        val loader = Thread.currentThread().getContextClassLoader();
        try(val stream = loader.getResourceAsStream("application.properties")) {
            props.load(stream);
        } catch(IOException e) {
            log.log(Level.SEVERE, "error while reading application properties");
        }

        baseUrl = props.getProperty("api.baseUrl");
        dateFormat = props.getProperty("api.dateFormat");
        patternUserId = pattern(props, "api.pattern.userId");
        patternJsessionId = pattern(props, "api.pattern.jsessionId");
        patternDate = pattern(props, "api.pattern.date");
    }

    private static Pattern pattern(Properties props, String key) {
        return Pattern.compile(props.getProperty(key));
    }

}

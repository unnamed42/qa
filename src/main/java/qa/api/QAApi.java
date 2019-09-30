package qa.api;

import lombok.extern.java.Log;
import lombok.val;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Log
public class QAApi implements AutoCloseable {

    private final QAProperties props;
    private final CloseableHttpClient httpClient;

    public QAApi() {
        this.props = new QAProperties();
        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(new BasicCookieStore()).build();
    }

    public Optional<String> getJsessinId(String username, String password) {
        val request = post("login.action")
                .addParameter("username", username)
                .addParameter("password", password).build();
        try(val response = httpClient.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            if(status != HttpStatus.SC_OK && status != HttpStatus.SC_MOVED_TEMPORARILY)
                return Optional.empty();
            val content = response.getFirstHeader("Location").toString();
            return extract(props.getPatternJsessionId(), content, 1);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<String> getUserId(String jsessionId) {
        val request = get("main.action").build();
        try(val response = httpClient.execute(request)) {
            if(!isOk(response))
                return Optional.empty();
            val content = EntityUtils.toString(response.getEntity());
            val userId = extract(props.getPatternUserId(), content, 1);
            EntityUtils.consume(response.getEntity());
            return userId;
        } catch(IOException e) {
            return Optional.empty();
        }
    }

    public Optional<String> addEvent(String title, String content) {
        if(nullOrEmpty(title) || nullOrEmpty(content))
            return Optional.empty();
        val formatter = new SimpleDateFormat(props.getDateFormat());
        val today = formatter.format(new Date());

        val request = post("addEvent.action")
                .addParameter("event.title", title)
                .addParameter("event.start", today)
                .addParameter("event.end", today)
                .addParameter("event.status", "1")
                .addParameter("event.location", "self")
                .addParameter("event.description", content).build();
        try(val response = httpClient.execute(request)) {
            if(isOk(response))
                return Optional.of(parseJson(response).getJSONObject("event").getString("id"));
        } catch (IOException ignored) {}
        return Optional.empty();
    }

    public boolean updateEvent(String title, String content, String date, String eventId, String userId) {
        if(nullOrEmpty(title) || nullOrEmpty(content))
            return false;
        val request = post("updateEvent.action")
                .addParameter("event.title", title)
                .addParameter("event.start", date)
                .addParameter("event.end", date)
                .addParameter("event.id", eventId)
                .addParameter("event.status", "1")
                .addParameter("event.allDay", "true")
                .addParameter("event.userId", userId)
                .addParameter("event.location", "self")
                .addParameter("event.description", content).build();
        try(val response = httpClient.execute(request)) {
            return isOk(response);
        } catch(IOException e) {
            return false;
        }
    }

    public boolean deleteEvent(String eventId) {
        if(nullOrEmpty(eventId)) return false;
        val request = get("deleteEvent.action").build();
        try(val response = httpClient.execute(request)) {
            return isOk(response);
        } catch(IOException e) {
            return false;
        }
    }

    public Optional<List<QAEvent>> getEvents(String startDate, String endDate, String userId) {
        val request = get("eventList.action")
                .addParameter("userId", userId)
                .addParameter("startDate", startDate)
                .addParameter("endDate", endDate).build();
        try(val response = httpClient.execute(request)) {
            if(!isOk(response)) return Optional.empty();
            val events = new ArrayList<QAEvent>();
            for(Object event : parseJson(response).getJSONArray("events")) {
                val json = (JSONObject)event;
                events.add(new QAEvent()
                    .setId(json.getString("id"))
                    .setTitle(json.getString("title"))
                    .setDescription(json.getString("description"))
                    .setDate(json.getString("start").split("T")[0])
                );
            }
            return Optional.of(events);
        } catch(IOException e) {
            return Optional.empty();
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "IOException thrown while closing HttpClient");
        }
    }

    private RequestBuilder post(String action) {
        return RequestBuilder.post(String.format("%s/%s", props.getBaseUrl(), action))
                .setCharset(StandardCharsets.UTF_8)
                .addHeader("Content-Type", "application/x-www-form-urlencoded");
    }

    private RequestBuilder get(String action) {
        return RequestBuilder.get(String.format("%s/%s", props.getBaseUrl(), action))
                .setCharset(StandardCharsets.UTF_8);
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static Optional<String> extract(Pattern re, String content, int group) {
        val matcher = re.matcher(content);
        if(matcher.find())
            return Optional.of(matcher.group(group));
        return Optional.empty();
    }

    private static boolean isOk(HttpResponse response) {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }

    private static JSONObject parseJson(HttpResponse response) throws IOException {
        val json = new JSONObject(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());
        return json;
    }

}

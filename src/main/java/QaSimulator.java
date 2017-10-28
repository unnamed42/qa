import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * create with PACKAGE_NAME
 * USER: husterfox
 *
 * @author husterfox
 */
public class QaSimulator {
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static int STATUS_OK = 200;
    private final static int STATUS_JUMP = 302;
    private final static int MONTH_UPPER_BOUND = 12;
    private final static int MONTH_LOWER_BOUND = 1;
    private final static int DAY_UPPER_BOUND = 31;
    private final static int DAY_LOWER_BOUND = 1;


    private final static Pattern JSESSION_ID_PATTERN = Pattern.compile("\\w+=(.*)");
    private final static Pattern USER_ID_PATTERN = Pattern.compile("cal.data.userId='(\\d+)'");
    private final static Pattern DATE_PATTERN = Pattern.compile("(\\d+)-(\\d+)-(\\d+)");


    private String userName;
    private String passWord;
    private String jsessionId;
    private String userId;
    private String eventId;
    private CloseableHttpClient httpClient;

    private BasicCookieStore cookieStore = new BasicCookieStore();
    private boolean ifLogin;

    QaSimulator(String un, String pw) {
        userName = un;
        passWord = pw;
    }

    public static void main(String[] args) throws IOException {
        QaSimulator qaSimulator = new QaSimulator("M201773011", "hu19960207");
        if (qaSimulator.login()) {
            qaSimulator.deleteEvent("234934");
        }
    }

    boolean isIfLogin() {
        return ifLogin;
    }

    boolean login() throws IOException {
        httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

        String loginUrl = "http://grid.hust.edu.cn/qa/login.action";
        HttpUriRequest loginActionRequest = RequestBuilder.post().setUri(loginUrl)
                .addParameter(USERNAME, userName).addParameter(PASSWORD, passWord)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        CloseableHttpResponse loginActionResponse = null;
        CloseableHttpResponse mainActionResponse = null;

        try {
            loginActionResponse = httpClient.execute(loginActionRequest);
            if (loginActionResponse.getStatusLine().getStatusCode() != STATUS_JUMP) {
                System.err.println("login: 用户名或密码错误，登陆失败  error code: " + loginActionResponse.getStatusLine().getStatusCode());
                ifLogin = false;
                return false;
            }
            Matcher jsessionIdMatcher = JSESSION_ID_PATTERN.matcher(loginActionResponse.getFirstHeader("Location").toString());
            if (jsessionIdMatcher.find()) {
                jsessionId = jsessionIdMatcher.group(1);
            } else {
                System.err.println("login: jsessionId not exist");
                ifLogin = false;
                return false;
            }

        } catch (IOException | UnsupportedOperationException e) {
            e.printStackTrace();
        } finally {
            if (loginActionResponse != null) {
                loginActionResponse.close();
            }
        }

        try {
            if (null == jsessionId) {
                ifLogin = false;
                return false;
            }
            String mainActionUrl = "http://grid.hust.edu.cn/qa/main.action";
            HttpUriRequest mainActionRequest = RequestBuilder.get(mainActionUrl).setCharset(Charset.forName("UTF-8")).build();
            mainActionResponse = httpClient.execute(mainActionRequest);
            if (mainActionResponse.getStatusLine().getStatusCode() != STATUS_OK) {
                ifLogin = false;
                return false;
            }
            Matcher userIdMatcher = USER_ID_PATTERN.matcher(EntityUtils.toString(mainActionResponse.getEntity()));
            if (userIdMatcher.find()) {
                userId = userIdMatcher.group(1);
            } else {
                System.err.println("login: useId is not exist");
                ifLogin = false;
                return false;
            }
            EntityUtils.consume(mainActionResponse.getEntity());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            if (mainActionResponse != null) {
                mainActionResponse.close();
            }
        }
        ifLogin = true;
        return true;
    }

    private boolean generateEventId(String title, String content) throws IOException {
        if ("".equals(title) || "".equals(content)) {
            System.err.println("addEvent: title or content can't be empty!");
            return false;
        }
        Date dateToday = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(dateToday);

        CloseableHttpResponse addEventResponse = null;
        String addEventUrl = "http://grid.hust.edu.cn/qa/addEvent.action";
        HttpUriRequest addEventActionRequest = RequestBuilder.post(addEventUrl).
                addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addParameter("event.title", title)
                .addParameter("event.start", date)
                .addParameter("event.end", date)
                .addParameter("event.status", "1")
                .addParameter("event.location", "self")
                .addParameter("event.description", content).build();

        try {
            addEventResponse = httpClient.execute(addEventActionRequest);
            if (addEventResponse.getStatusLine().getStatusCode() != STATUS_OK) {
                System.err.println("add event failed code: " + addEventResponse.getStatusLine().getStatusCode());
                return false;
            } else {
                JSONObject addEventResponseContent = new JSONObject(EntityUtils.toString(addEventResponse.getEntity()));
                eventId = ((JSONObject) addEventResponseContent.get("event")).get("id").toString();
                EntityUtils.consume(addEventResponse.getEntity());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (addEventResponse != null) {
                addEventResponse.close();
            }
        }
        return true;
    }

    boolean addEvent(String title, String date, String content) throws IOException {
        if (!generateEventId(title, content)) {
            System.err.println("GenerateEventID failed");
            return false;
        }
        if (!updateEvent(title, date, content, eventId)) {
            System.err.println("add failed while invoking update action");
            return false;
        }

        return true;
    }

    boolean updateEvent(String title, String date, String content, String eventId) throws IOException {
        if (Objects.equals(title, "") || Objects.equals(content, "")) {
            System.err.println("updateEvent: title or content can't be empty!");
            return false;
        }
        if (!checkDateIfLegal(date)) {
            System.err.println("updateEvent: Date Style is illegal, legal style eg. 2017-10-22!");
            return false;
        }
        CloseableHttpResponse updateEventResponse = null;
        String updateEventUrl = "http://grid.hust.edu.cn/qa/updateEvent.action";
        HttpUriRequest updateEventActionRequest = RequestBuilder.post(updateEventUrl).setCharset(Charset.forName("UTF-8"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addParameter("event.title", title)
                .addParameter("event.start", date)
                .addParameter("event.end", date)
                .addParameter("eventId", eventId)
                .addParameter("event.id", eventId)
                .addParameter("event.status", "1")
                .addParameter("event.allDay", "true")
                .addParameter("event.userId", userId)
                .addParameter("event.location", "self")
                .addParameter("event.description", content).build();

        try {
            updateEventResponse = httpClient.execute(updateEventActionRequest);
            if (updateEventResponse.getStatusLine().getStatusCode() != STATUS_OK) {
                System.err.println("update event failed code: " + updateEventResponse.getStatusLine().getStatusCode());
                System.err.println("update event failed: " + updateEventResponse.getStatusLine());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (updateEventResponse != null) {
                updateEventResponse.close();
            }
        }
        return true;

    }

    boolean deleteEvent(String eventId) throws IOException {
        CloseableHttpResponse deleteEventResponse = null;
        String deleteEventUrl = "http://grid.hust.edu.cn/qa/deleteEvent.action";
        HttpUriRequest deleteEventActionRequest = RequestBuilder.get(deleteEventUrl)
                .addParameter("eventId", eventId)
                .build();
        try {
            deleteEventResponse = httpClient.execute(deleteEventActionRequest);
            if (deleteEventResponse.getStatusLine().getStatusCode() != STATUS_OK) {
                System.err.println("deleteEvent: delete failed code: " + deleteEventResponse.getStatusLine());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (deleteEventResponse != null) {
                deleteEventResponse.close();
            }
        }
        return true;
    }

    Object[][] getEventIdList(String startDate, String endDate) throws IOException {
        if (!(checkDateIfLegal(startDate) && checkDateIfLegal(endDate))) {
            System.out.println("getEventIdList: startDate or endDate is illegal");
            return new Object[][]{};
        }
        if (startDate.compareTo(endDate) > 0) {
            System.err.println("getEventIdList: startDate less than endDate");
            return new Object[][]{};
        }
        CloseableHttpResponse eventListResponse = null;
        String eventListUrl = "http://grid.hust.edu.cn/qa/eventList.action";
        HttpUriRequest eventListActionRequest = RequestBuilder.get(eventListUrl)
                .addParameter("userId", userId)
                .addParameter("startDate", startDate)
                .addParameter("endDate", endDate)
                .build();
        Object[][] rawData = null;
        try {
            eventListResponse = httpClient.execute(eventListActionRequest);
            if (eventListResponse.getStatusLine().getStatusCode() != STATUS_OK) {
                System.err.println("eventListAction failed code: " + eventListResponse.getStatusLine());
                return new Object[][]{};
            } else {
                JSONObject eventList = new JSONObject(EntityUtils.toString(eventListResponse.getEntity()));
                JSONArray eventJsonArray = new JSONArray(eventList.get("events").toString());
                System.out.println(eventList.get("events").toString());
                rawData = new Object[eventJsonArray.length()][QaUserInterface.COLUMN_NAMES.length];

                int i = 0;
                for (Object event : eventJsonArray) {
                    JSONObject eventJsonObject = (JSONObject) event;
                    System.out.printf("event_id\tdate\ttitle\tcontent\n");
                    System.out.printf("%s\t%s\t%s\t%s\n", eventJsonObject.get("id"),
                            eventJsonObject.getString("start").split("T")[0],
                            eventJsonObject.getString("title"),
                            eventJsonObject.getString("description"));

                    rawData[i][QaUserInterface.EVENT_ID_COLUMN_POS] = eventJsonObject.get("id");
                    rawData[i][QaUserInterface.TITLE_COLUMN_POS] = eventJsonObject.getString("title");
                    rawData[i][QaUserInterface.CONTENT_COLUMN_POS] = eventJsonObject.getString("description");
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (eventListResponse != null) {
                eventListResponse.close();
            }
        }
        return rawData;
    }

    private boolean checkDateIfLegal(String date) {
        Matcher dateMatcher = DATE_PATTERN.matcher(date);
        int dateGroupCountUpperBound = 3;
        if (dateMatcher.matches()) {
            if (dateMatcher.groupCount() != dateGroupCountUpperBound) {
                System.err.print("Date Style is illegal, legal style eg. 2017-10-22");
                return false;
            }
            int month = Integer.parseInt(dateMatcher.group(2));
            int day = Integer.parseInt(dateMatcher.group(3));
            if (month > MONTH_UPPER_BOUND || month < MONTH_LOWER_BOUND || day > DAY_UPPER_BOUND || day < DAY_LOWER_BOUND) {
                System.err.print("Date Style is illegal, legal style eg. 2017-10-22");
                return false;
            }

        } else {
            System.err.println("Date Style is illegal, legal style eg. 2017-10-22");
            return false;
        }
        return true;
    }

    void closeHttpClient() throws IOException {
        if (httpClient != null) {
            httpClient.close();

        }
    }

}

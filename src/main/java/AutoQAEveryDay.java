import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AutoQAEveryDay {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("please input <username> <password> <title> <content>");
            System.exit(-1);
        }
        String userName = args[0];
        String passWord = args[1];
        String title = args[2];
        String content = args[3];
        QaSimulator qaSimulator = new QaSimulator(userName, passWord);
        try {
            if (qaSimulator.login()) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                String currDay = df.format(date);
                if (!BuildDateTable.checkWeekends(date)) {
                    if (qaSimulator.addEvent(title, currDay, content)) {
                        System.out.printf("添加title: %s content: %s day: %s succeed\n", title, content, currDay);
                    } else {
                        System.out.printf("添加day: %s 失败\n", currDay);
                    }

                }
            } else {
                System.err.println("登陆失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package qa.mvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class DateFormatter extends AbstractFormatter {

    private final SimpleDateFormat formatter;

    public DateFormatter(String pattern) {
        this.formatter = new SimpleDateFormat(pattern);
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        return formatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) {
        if(value != null)
            return formatter.format(((Calendar)value).getTime());
        return "";
    }
}

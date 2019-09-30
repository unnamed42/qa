package qa.mvc;

import javax.swing.table.DefaultTableModel;

public class QATableModel extends DefaultTableModel {

    private static final String[] COLUMN_NAMES = {
        "eventId", "title", "content", "date"
    };

    QATableModel(Object[][] source) {
        super(source, COLUMN_NAMES);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // not eventId or date
        return column != 0 && column != 3;
    }

}

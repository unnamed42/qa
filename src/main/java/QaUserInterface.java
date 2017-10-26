import com.eltima.components.ui.DatePicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * create with PACKAGE_NAME
 * USER: husterfox
 *
 * @author husterfox
 */
public class QaUserInterface {
    private final Object[] columnNames = {"eventId", "title", "content"};
    Document document;
    String preDateStr = "";
    String nextDateStr = "";
    private DatePicker datePickerField;
    private JTextField titleJTextField;
    private JTextArea contentJTextArea;
    private JButton loginJButton;
    private JButton addJButton;
    private JButton showJButton;
    private JButton deleteJButton;
    private JPanel leftSideJpanel;
    private JPanel rightSideJpanel;
    private JScrollPane jInfoJTableScrollPane;
    private JTable infoJTable;
    private JPopupMenu jPopupMenu;
    private QaSimulator qaSimulator;
    private DefaultTableModel infoDefaultModel;
    private Object[][] originData = {
            {"", "", ""},
            {"", "", ""},
            {"", "", ""}
    };

    public static void main(String[] args) {
        QaUserInterface qaUserInterface = new QaUserInterface();
        qaUserInterface.createMainFrame();
    }

    private void addListener() {
        loginJButton.addActionListener(new ButtonListener());
        addJButton.addActionListener(new ButtonListener());
        showJButton.addActionListener(new ButtonListener());
        deleteJButton.addActionListener(new ButtonListener());
    }

    private void createMainFrame() {
        JFrame mainFrame = new JFrame("QA-beta");
        mainFrame.setSize(800, 500);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    clearSource();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        GridBagLayout mainFrameGridLayout = new GridBagLayout();
        mainFrame.setLayout(mainFrameGridLayout);
        GridBagConstraints leftSideJPanelConstraints = createLeftSideJPanel();
        GridBagConstraints rightSideJPanelConstraints = createRightSideJpanel();
        GridBagConstraints jInfoTableJscrollPanelConstraints = createJscrollPanel();
        mainFrame.add(leftSideJpanel);
        mainFrame.add(rightSideJpanel);
        mainFrame.add(jInfoJTableScrollPane);

        mainFrameGridLayout.setConstraints(leftSideJpanel, leftSideJPanelConstraints);
        mainFrameGridLayout.setConstraints(rightSideJpanel, rightSideJPanelConstraints);
        mainFrameGridLayout.setConstraints(jInfoJTableScrollPane, jInfoTableJscrollPanelConstraints);
        addListener();
        mainFrame.setVisible(true);
    }

    private GridBagConstraints createJscrollPanel() {

        infoDefaultModel = new DefaultTableModel(originData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        infoJTable = new JTable(infoDefaultModel);
        infoJTable.setShowHorizontalLines(true);
        infoJTable.setShowVerticalLines(true);
        infoJTable.setGridColor(Color.black);
        infoJTable.setRowHeight(30);
        infoJTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        operateOnJTable();
        jInfoJTableScrollPane = new JScrollPane(infoJTable);
        //必须在构造方法构建JScrollPane才能显示Table

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        return gridBagConstraints;

    }

    private void operateOnJTable() {
        addPopMenu2JTable();
        infoJTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON3)) {
                    if (qaSimulator != null && qaSimulator.isIfLogin()) {
                        jPopupMenu.show(infoJTable, e.getX(), e.getY());
                    } else {
                        JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                                "未登陆", JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });

    }

    private boolean checkDateLegal() throws ParseException {
        if ("".equals(datePickerField.getText())) {
            JOptionPane.showMessageDialog(null, "请先输入日期",
                    "未输入日期", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String currentDay = format.format(new Date());
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || datePickerField.getText().equals(currentDay)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "不是周六周末也不是当前日期，不能操作的亲！",
                    "非法日期", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void addPopMenu2JTable() {
        jPopupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("删除");
        JMenuItem updateMenuItem = new JMenuItem("更新");
        jPopupMenu.add(deleteMenuItem);
        jPopupMenu.add(updateMenuItem);
        deleteMenuItem.addActionListener(e -> {
            int[] selectedRows = infoJTable.getSelectedRows();
            StringBuilder deleteFailedInfo = new StringBuilder();
            StringBuilder deleteSuccessInfo = new StringBuilder();
            for (int selectedRow : selectedRows) {
                System.out.println("选中的行数：" + selectedRow);
                try {
                    String eventId = infoDefaultModel.getValueAt(selectedRow, 0).toString();
                    if (!qaSimulator.deleteEvent(eventId)) {
                        deleteFailedInfo.append(" eventId: ").append(eventId).append(" 删除失败; ");
                    } else {
                        deleteSuccessInfo.append(" eventId: ").append(eventId).append(" 删除成功; ");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            refreshJTable();
            JOptionPane.showMessageDialog(null,
                    deleteSuccessInfo.toString() + deleteFailedInfo.toString(),
                    "删除信息", JOptionPane.INFORMATION_MESSAGE);

        });
        updateMenuItem.addActionListener(e -> {
            try {
                if (checkDateLegal()) {
                    int[] selectedRows = infoJTable.getSelectedRows();
                    StringBuilder updateFailedStr = new StringBuilder();
                    StringBuilder updateSuccessStr = new StringBuilder();
                    for (int selectedRow : selectedRows) {
                        System.out.println("选中的行数：" + selectedRow);
                        try {
                            String eventId = infoDefaultModel.getValueAt(selectedRow, 0).toString();
                            String title = infoDefaultModel.getValueAt(selectedRow, 1).toString();
                            String content = infoDefaultModel.getValueAt(selectedRow, 2).toString();
                            String date = datePickerField.getText();
                            if (!qaSimulator.updateEvent(title, date, content, eventId)) {
                                updateFailedStr.append(" eventId ").append(eventId).append(" update failed ");
                            } else {
                                updateSuccessStr.append(" eventId ").append(eventId).append(" update success; ");
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    refreshJTable();
                    JOptionPane.showMessageDialog(null,
                            updateSuccessStr.toString() + updateFailedStr.toString(),
                            "更新信息", JOptionPane.INFORMATION_MESSAGE);

                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        });
    }

    private GridBagConstraints createLeftSideJPanel() {
        leftSideJpanel = new JPanel();
        leftSideJpanel.setLayout(new GridLayout(5, 1));
        JPanel dateLabelAndTextFieldJPanel = new JPanel(new FlowLayout());
        JLabel dateLabel = new JLabel("日期");
        datePickerField = new DatePicker(new Date(), "yyyy-MM-dd",
                new Font("Times New Roman", Font.BOLD, 14), new Dimension(200, 30));
        document = datePickerField.getInnerTextField().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

                boolean isInit = "".equals(preDateStr) && "".equals(nextDateStr);
                boolean isLogin = qaSimulator == null || !qaSimulator.isIfLogin();
                if (isLogin) {
                    System.out.println("登陆前赋值" + (preDateStr = datePickerField.getText()));
                    preDateStr = datePickerField.getText();
                } else {
                    nextDateStr = datePickerField.getText();
                    if (preDateStr.equals(nextDateStr)) {
                        System.out.println("登陆后: " + nextDateStr);
                    } else {
                        preDateStr = nextDateStr;
                        System.out.println("刷新！");
                        refreshJTable();
                    }
                }
            }


            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        dateLabelAndTextFieldJPanel.add(dateLabel);
        dateLabelAndTextFieldJPanel.add(datePickerField);
        loginJButton = new JButton("登陆");
        addJButton = new JButton("添加");
        showJButton = new JButton("显示");
        deleteJButton = new JButton("删除");
        leftSideJpanel.add(dateLabelAndTextFieldJPanel);
        leftSideJpanel.add(loginJButton);
        leftSideJpanel.add(addJButton);
        leftSideJpanel.add(showJButton);
        leftSideJpanel.add(deleteJButton);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        return gridBagConstraints;
    }

    private GridBagConstraints createRightSideJpanel() {
        rightSideJpanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints rightJPanelGridBagConstraints = new GridBagConstraints();
        rightSideJpanel.setLayout(gridBagLayout);
        titleJTextField = new JTextField(30);
        titleJTextField.setToolTipText("输入标题");
        contentJTextArea = new JTextArea(5, 30);
        contentJTextArea.setToolTipText("输入具体内容");
        JScrollPane jScrollPane = new JScrollPane(contentJTextArea);
        rightJPanelGridBagConstraints.gridx = 0;
        rightJPanelGridBagConstraints.gridy = 0;
        rightJPanelGridBagConstraints.gridheight = 1;
        rightJPanelGridBagConstraints.gridwidth = 5;
        rightJPanelGridBagConstraints.weightx = 1;
        rightJPanelGridBagConstraints.weighty = 0.5;
        rightJPanelGridBagConstraints.insets = new Insets(10, 10, 10, 10);
        rightJPanelGridBagConstraints.fill = GridBagConstraints.BOTH;
        rightSideJpanel.add(titleJTextField, rightJPanelGridBagConstraints);
        rightJPanelGridBagConstraints.gridx = 0;
        rightJPanelGridBagConstraints.gridy = 1;
        rightJPanelGridBagConstraints.gridheight = 4;
        rightJPanelGridBagConstraints.gridwidth = 5;
        rightJPanelGridBagConstraints.weightx = 1;
        rightJPanelGridBagConstraints.weighty = 1.5;
        rightJPanelGridBagConstraints.fill = GridBagConstraints.BOTH;
        rightJPanelGridBagConstraints.insets = new Insets(10, 10, 10, 10);
        rightSideJpanel.add(jScrollPane, rightJPanelGridBagConstraints);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        return gridBagConstraints;

    }

    private void clearSource() throws IOException {
        System.out.println("清理资源");
        if (qaSimulator != null) {
            qaSimulator.closeHttpClient();
        }
    }

    private void refreshJTable() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            String date = datePickerField.getText();
            try {
                Object[][] rawData = qaSimulator.getEventIdList(date, date);
                if (rawData.length == 0) {
                    JOptionPane.showMessageDialog(null, date + "event为空，或者获取失败",
                            "event为空", JOptionPane.ERROR_MESSAGE);
                }
                int deleteCount = infoDefaultModel.getRowCount() - rawData.length;
                while (deleteCount > 0) {
                    infoDefaultModel.removeRow(infoDefaultModel.getRowCount() - 1);
                    deleteCount--;
                }
                for (int row = 0; row < infoJTable.getRowCount(); row++) {
                    for (int column = 0; column < columnNames.length; column++) {
                        infoDefaultModel.setValueAt("", row, column);
                    }
                }
                if (infoDefaultModel != null) {
                    for (int row = 0; row < rawData.length; row++) {
                        for (int column = 0; column < columnNames.length; column++) {
                            if (row >= infoDefaultModel.getRowCount()) {
                                infoDefaultModel.addRow(new Object[]{});
                            }
                            infoDefaultModel.setValueAt(rawData[row][column], row, column);
                        }
                    }
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } else {
            JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                    "未登陆", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginJButton) {
                JFrame loginJFrame = new JFrame("欢迎登陆");
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                loginJFrame.setLayout(new GridBagLayout());
                loginJFrame.setLocationRelativeTo(null);
                loginJFrame.setSize(300, 400);
                loginJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                JLabel userNameLabel = new JLabel("用户名");
                JLabel passWordLabel = new JLabel("密码");
                JTextField userNameJTextField = new JTextField(20);
                JPasswordField passwordJPasswordField = new JPasswordField(20);
                JButton loginJButton = new JButton("登陆");
                loginJButton.addActionListener(e12 -> {
                    String userName = userNameJTextField.getText();
                    String password = String.valueOf(passwordJPasswordField.getPassword());
                    qaSimulator = new QaSimulator(userName, password);
                    try {
                        qaSimulator.login();
                        if (qaSimulator.isIfLogin()) {
                            JOptionPane.showMessageDialog(null, "登陆成功", "INFORMATION_MESSAGE", JOptionPane.INFORMATION_MESSAGE);
                            loginJFrame.dispose();
                            refreshJTable();
                        } else {
                            JOptionPane.showMessageDialog(null, "用户名或密码错误", "标题", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });

                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridwidth = 1;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 0;
                gridBagConstraints.weighty = 0;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new Insets(20, 5, 10, 5);
                loginJFrame.add(userNameLabel, gridBagConstraints);

                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridx = 4;
                gridBagConstraints.gridwidth = 4;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 0;
                gridBagConstraints.insets = new Insets(20, 5, 10, 5);
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(userNameJTextField, gridBagConstraints);

                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridwidth = 1;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 0;
                gridBagConstraints.weighty = 0;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new Insets(20, 5, 10, 5);
                loginJFrame.add(passWordLabel, gridBagConstraints);

                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridx = 4;
                gridBagConstraints.gridwidth = 4;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 0;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new Insets(20, 5, 10, 5);
                loginJFrame.add(passwordJPasswordField, gridBagConstraints);

                gridBagConstraints.gridy = 6;
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridwidth = 6;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 0;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new Insets(20, 5, 10, 5);
                loginJFrame.add(loginJButton, gridBagConstraints);
                loginJFrame.setVisible(true);
            }
            if (e.getSource() == addJButton) {
                if (qaSimulator != null && qaSimulator.isIfLogin()) {
                    String title = titleJTextField.getText();
                    String content = contentJTextArea.getText();
                    String date = datePickerField.getText();
                    try {
                        if (checkDateLegal()) {
                            if (qaSimulator.addEvent(title, date, content)) {
                                refreshJTable();
                                JOptionPane.showMessageDialog(null, "添加成功",
                                        "添加成功", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "添加失败，检查日期是否正确，以及title 和content是否为空",
                                        "未登陆", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (IOException | ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                            "未登陆", JOptionPane.ERROR_MESSAGE);
                }
            }
            if (e.getSource() == showJButton) {
                refreshJTable();
            }

            if (e.getSource() == deleteJButton) {
                if (qaSimulator != null && qaSimulator.isIfLogin()) {
                    JOptionPane.showMessageDialog(null, "由于JTable对于Java的单击事件，" +
                                    "获取行标不准，请直接对下表用鼠标操作",
                            "提示信息", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                            "未登陆", JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }
}

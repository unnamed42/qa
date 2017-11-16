import com.eltima.components.ui.DatePicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * create with PACKAGE_NAME
 * USER: husterfox
 *
 * @author husterfox
 */
public class QaUserInterface {
    final static Object[] COLUMN_NAMES = {"eventId", "title", "content", "date"};
    final static int EVENT_ID_COLUMN_POS = 0;
    final static int TITLE_COLUMN_POS = 1;
    final static int CONTENT_COLUMN_POS = 2;
    final static int DATE_COLUMN_POS = 3;
    private final String filename = ".login.properties";
    private final String username = "UserName";
    private final String userpasswd = "UserPassWd";
    private String preDateStr = "";
    private JFrame mainFrame;
    private DatePicker datePickerField;
    private JTextField titleJTextField;
    private JTextArea contentJTextArea;
    private JButton loginJButton;
    private JButton addJButton;
    private JButton showJButton;
    private JButton queryJButton;
    private JPanel leftSideJpanel;
    private JPanel rightSideJpanel;
    private JScrollPane jInfoJTableScrollPane;
    private JTable infoJTable;
    private JTable queryJTable;
    private JPopupMenu jPopupMenu;
    private QaSimulator qaSimulator;
    private DefaultTableModel infoDefaultModel;
    private DefaultTableModel queryDefaultModel;
    private Object[][] originData = {
    };

    public static void main(String[] args) {
        QaUserInterface qaUserInterface = new QaUserInterface();
        qaUserInterface.createMainFrame();
    }

    private void addListener() {
        loginJButton.addActionListener(new ButtonListener());
        addJButton.addActionListener(new ButtonListener());
        showJButton.addActionListener(new ButtonListener());
        queryJButton.addActionListener(new ButtonListener());
    }

    private void createMainFrame() {
        mainFrame = new JFrame("QA-beta"+" 未登陆");
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
        try {
            autoLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GridBagConstraints createJscrollPanel() {

        infoDefaultModel = new DefaultTableModel(originData, COLUMN_NAMES) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != EVENT_ID_COLUMN_POS && column != DATE_COLUMN_POS;
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
        setGridBagConstraints(gridBagConstraints, 0, 6, 4, 4,
                1, 1, GridBagConstraints.BOTH);
        return gridBagConstraints;
    }

    private void operateOnJTable() {
        jPopupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("删除");
        JMenuItem updateMenuItem = new JMenuItem("更新");
        jPopupMenu.add(deleteMenuItem);
        jPopupMenu.add(updateMenuItem);
        deleteMenuItem.addActionListener(new DeleteItemListener());
        updateMenuItem.addActionListener(new UpdateItemListener());
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


    private GridBagConstraints createLeftSideJPanel() {
        leftSideJpanel = new JPanel();
        leftSideJpanel.setLayout(new GridLayout(5, 1));
        JPanel dateLabelAndTextFieldJPanel = new JPanel(new FlowLayout());
        JLabel dateLabel = new JLabel("日期");
        datePickerField = new DatePicker(new Date(), "yyyy-MM-dd",
                new Font("Times New Roman", Font.BOLD, 14), new Dimension(200, 30));
        Document document = datePickerField.getInnerTextField().getDocument();
        document.addDocumentListener(new DocumentListenerAdapter());
        dateLabelAndTextFieldJPanel.add(dateLabel);
        dateLabelAndTextFieldJPanel.add(datePickerField);
        loginJButton = new JButton("登陆");
        addJButton = new JButton("添加");
        showJButton = new JButton("显示");
        queryJButton = new JButton("查询");
        leftSideJpanel.add(dateLabelAndTextFieldJPanel);
        leftSideJpanel.add(loginJButton);
        leftSideJpanel.add(addJButton);
        leftSideJpanel.add(showJButton);
        leftSideJpanel.add(queryJButton);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setGridBagConstraints(gridBagConstraints, 0, 0, 1,
                5, 0.5, 1, GridBagConstraints.BOTH);
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

        setGridBagConstraints(rightJPanelGridBagConstraints, 0, 0, 5,
                1, 1, 0.5, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10));
        rightSideJpanel.add(titleJTextField, rightJPanelGridBagConstraints);

        setGridBagConstraints(rightJPanelGridBagConstraints, 2, 1, 5,
                4, 1, 1.5, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10));
        rightSideJpanel.add(jScrollPane, rightJPanelGridBagConstraints);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setGridBagConstraints(gridBagConstraints, 1, 0, 3,
                5, 1, 1, GridBagConstraints.BOTH);

        return gridBagConstraints;

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

    private void clearSource() throws IOException {
        System.out.println("清理资源");
        if (qaSimulator != null) {
            qaSimulator.closeHttpClient();
        }
    }

    private void fillJTable(String startDate, String endDate, DefaultTableModel infoDefaultModel, JTable infoJTable) {
        try {
            Object[][] rawData = qaSimulator.getDateContent(startDate, endDate);
            if (rawData.length == 0) {
                System.out.println("startDate： "+startDate+" endDate: "+endDate+" 表格数据为空，或者获取失败");
            }
            int deleteCount = infoDefaultModel.getRowCount() - rawData.length;
            while (deleteCount > 0) {
                infoDefaultModel.removeRow(infoDefaultModel.getRowCount() - 1);
                deleteCount--;
            }
            for (int row = 0; row < infoJTable.getRowCount(); row++) {
                for (int column = 0; column < COLUMN_NAMES.length; column++) {
                    infoDefaultModel.setValueAt("", row, column);
                }
            }
            for (int row = 0; row < rawData.length; row++) {
                for (int column = 0; column < COLUMN_NAMES.length; column++) {
                    if (row >= infoDefaultModel.getRowCount()) {
                        infoDefaultModel.addRow(new Object[]{});
                    }
                    infoDefaultModel.setValueAt(rawData[row][column], row, column);
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void setGridBagConstraints(GridBagConstraints gridBagConstraints, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty) {
        if (gridBagConstraints != null) {
            gridBagConstraints.gridx = gridx;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.gridwidth = gridwidth;
            gridBagConstraints.gridheight = gridheight;
            gridBagConstraints.weightx = weightx;
            gridBagConstraints.weighty = weighty;
        }
    }

    private void setGridBagConstraints(GridBagConstraints gridBagConstraints, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill) {
        if (gridBagConstraints != null) {
            setGridBagConstraints(gridBagConstraints, gridx, gridy, gridwidth, gridheight, weightx, weighty);
            gridBagConstraints.fill = fill;
        }
    }

    private void setGridBagConstraints(GridBagConstraints gridBagConstraints, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill, Insets insets) {
        if (gridBagConstraints != null) {
            setGridBagConstraints(gridBagConstraints, gridx, gridy, gridwidth, gridheight, weightx, weighty, fill);
            gridBagConstraints.insets = insets;
        }
    }

    private void loginHandle() {
        JFrame loginJFrame = new JFrame("欢迎登陆");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        loginJFrame.setLayout(new GridBagLayout());
        loginJFrame.setLocationRelativeTo(null);
        loginJFrame.setSize(300, 400);
        loginJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        loginJFrame.setLocationRelativeTo(null);
        JLabel userNameLabel = new JLabel("用户名");
        JLabel passWordLabel = new JLabel("密码");
        JTextField userNameJTextField = new JTextField(20);

        userNameJTextField.requestFocusInWindow();
        JPasswordField passwordJPasswordField = new JPasswordField(20);
        passwordJPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER){
                    loginOperate(loginJFrame, userNameJTextField, passwordJPasswordField);
                }
            }
        });
        loginJFrame.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                if(aComponent == userNameJTextField){
                    return passwordJPasswordField;
                }else{
                    return userNameJTextField;
                }
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                if(aComponent == userNameJTextField){
                    return passwordJPasswordField;
                }else{
                    return userNameJTextField;
                }
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return userNameJTextField;
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return passwordJPasswordField;
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return userNameJTextField;
            }
        });
        JButton loginJButton = new JButton("登陆");
        loginJButton.addActionListener(e12 -> loginOperate(loginJFrame, userNameJTextField, passwordJPasswordField));

        setGridBagConstraints(gridBagConstraints, 2, 1, 1,
                1, 0, 0, GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 10, 5));
        loginJFrame.add(userNameLabel, gridBagConstraints);

        setGridBagConstraints(gridBagConstraints, 4, 1, 4,
                1, 3, 0, GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 10, 5));
        loginJFrame.add(userNameJTextField, gridBagConstraints);

        setGridBagConstraints(gridBagConstraints, 2, 3, 1,
                1, 0, 0, GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 10, 5));
        loginJFrame.add(passWordLabel, gridBagConstraints);

        setGridBagConstraints(gridBagConstraints, 4, 3, 4,
                1, 3, 0, GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 10, 5));
        loginJFrame.add(passwordJPasswordField, gridBagConstraints);

        setGridBagConstraints(gridBagConstraints, 2, 6, 6,
                2, 3, 0, GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 10, 5));
        loginJFrame.add(loginJButton, gridBagConstraints);

        loginJFrame.setVisible(true);
    }

    private void loginOperate(JFrame loginJFrame, JTextField userNameJTextField, JPasswordField passwordJPasswordField) {
        String userName = userNameJTextField.getText();
        String password = String.valueOf(passwordJPasswordField.getPassword());
        qaSimulator = new QaSimulator(userName, password);
        try {
            qaSimulator.login();
            if (qaSimulator.isIfLogin()) {
                int operate = JOptionPane.showConfirmDialog(null, "登陆成功.\n是否保存登陆信息到当前目录中，以便下次登陆",
                        "登陆成功", JOptionPane.YES_NO_OPTION);
                mainFrame.setTitle("QA-beta: 用户 "+userName);
                if (operate == JOptionPane.YES_OPTION) {
                    Properties loginProperties = new Properties();
                    FileOutputStream fileOutputStream = new FileOutputStream(filename, false);
                    loginProperties.setProperty(username, userName);
                    loginProperties.setProperty(userpasswd, password);
                    loginProperties.store(new OutputStreamWriter(fileOutputStream, "utf-8"),
                            "login information");
                    fileOutputStream.close();
                }
                loginJFrame.dispose();
                showHandle();
            } else {
                JOptionPane.showMessageDialog(null, "用户名或密码错误", "登陆失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private void autoLogin() throws IOException {
        File loginFile = new File(filename);
        if (loginFile.exists()) {
            Properties loginProperties = new Properties();
            FileInputStream in = new FileInputStream(filename);
            loginProperties.load(new InputStreamReader(in, "utf-8"));
            String userName = loginProperties.getProperty(username);
            String userPassWd = loginProperties.getProperty(userpasswd);
            qaSimulator = new QaSimulator(userName, userPassWd);
            qaSimulator.login();
            if (qaSimulator.isIfLogin()) {
                JOptionPane.showMessageDialog(null, "从配置信息中,登陆成功,若想更换用户,请点击登陆按钮登陆",
                        "INFORMATION_MESSAGE",
                        JOptionPane.INFORMATION_MESSAGE);

                showHandle();
                mainFrame.setTitle("QA-beta: "+ " 用户: " + userName);
            } else {
                JOptionPane.showMessageDialog(null, "login.properties中用户名或密码错误",
                        "登陆失败", JOptionPane.ERROR_MESSAGE);
            }
            in.close();
        } else {
            JOptionPane.showMessageDialog(null, "程序所在根目录中未找到 login.properties 文件,请使用登陆按钮登陆",
                    "文件未找到", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addHandle() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            String title = titleJTextField.getText();
            String content = contentJTextArea.getText();
            String date = datePickerField.getText();
            try {
                if (checkDateLegal()) {
                    if (qaSimulator.addEvent(title, date, content)) {
                        titleJTextField.setText("");
                        contentJTextArea.setText("");
                        showHandle();
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

    private void showHandle() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            String date = datePickerField.getText();
            fillJTable(date, date, infoDefaultModel, infoJTable);
        } else {
            JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                    "未登陆", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void queryHandle() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            JFrame queryJFrame = new JFrame("欢迎查询");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            queryJFrame.setLayout(new GridBagLayout());
            queryJFrame.setLocationRelativeTo(null);
            queryJFrame.setSize(700, 400);
            queryJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            queryJFrame.setLocationRelativeTo(null);

            JLabel startDateLabel = new JLabel("开始日期");
            JLabel endDateLabel = new JLabel("结束日期");
            DatePicker startDatePicker = new DatePicker(new Date(), "yyyy-MM-dd",
                    new Font("Times New Roman", Font.BOLD, 14), new Dimension(100, 30));
            DatePicker endDatePicker = new DatePicker(new Date(), "yyyy-MM-dd",
                    new Font("Times New Roman", Font.BOLD, 14), new Dimension(100, 30));
            JButton queryConfirmJButton = new JButton("查询");
            queryDefaultModel = new DefaultTableModel(originData, COLUMN_NAMES);
            queryJTable = new JTable(queryDefaultModel);
            queryJTable.setEnabled(false);
            queryJTable.setShowHorizontalLines(true);
            queryJTable.setShowVerticalLines(true);
            queryJTable.setGridColor(Color.black);
            queryJTable.setRowHeight(30);
            queryJTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
            JScrollPane queryJTableScrollPane = new JScrollPane(queryJTable);
            queryConfirmJButton.addActionListener(e -> {
                String startDate = startDatePicker.getText();
                String endDate = endDatePicker.getText();
                if (startDate.compareTo(endDate) > 0) {
                    JOptionPane.showMessageDialog(null, "开始日期大于结束日期", "非法日期", JOptionPane.ERROR_MESSAGE);
                } else {
                    fillJTable(startDate, endDate, queryDefaultModel, queryJTable);
                }
            });

            setGridBagConstraints(gridBagConstraints, 2, 1, 1,
                    1, 0, 0, GridBagConstraints.HORIZONTAL,
                    new Insets(20, 5, 10, 5));
            queryJFrame.add(startDateLabel, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 4, 1, 4,
                    1, 3, 0, GridBagConstraints.HORIZONTAL,
                    new Insets(20, 5, 10, 5));
            queryJFrame.add(startDatePicker, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 2, 3, 1,
                    1, 0, 0, GridBagConstraints.HORIZONTAL,
                    new Insets(20, 5, 10, 5));
            queryJFrame.add(endDateLabel, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 4, 3, 4,
                    1, 3, 0, GridBagConstraints.HORIZONTAL,
                    new Insets(20, 5, 10, 5));
            queryJFrame.add(endDatePicker, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 2, 6, 6,
                    2, 3, 0, GridBagConstraints.HORIZONTAL,
                    new Insets(20, 5, 10, 5));
            queryJFrame.add(queryConfirmJButton, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 0, 8, 8,
                    10, 5, 5, GridBagConstraints.BOTH);
            queryJFrame.add(queryJTableScrollPane, gridBagConstraints);

            queryJFrame.setVisible(true);

        } else {
            JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                    "未登陆", JOptionPane.ERROR_MESSAGE);
        }


    }

    private String getEventId(DefaultTableModel infoDefaultModel, int row) {
        return infoDefaultModel.getValueAt(row, EVENT_ID_COLUMN_POS).toString();
    }

    private String getTitle(DefaultTableModel infoDefaultModel, int row) {
        return infoDefaultModel.getValueAt(row, TITLE_COLUMN_POS).toString();
    }

    private String getContent(DefaultTableModel infoDefaultModel, int row) {
        return infoDefaultModel.getValueAt(row, CONTENT_COLUMN_POS).toString();
    }

    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == loginJButton) {
                loginHandle();
            }
            if (e.getSource() == addJButton) {
                addHandle();
            }
            if (e.getSource() == showJButton) {
                showHandle();
            }
            if (e.getSource() == queryJButton) {
                queryHandle();
            }
        }
    }

    class DocumentListenerAdapter implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            boolean isLogin = qaSimulator == null || !qaSimulator.isIfLogin();
            if (isLogin) {
                System.out.println("登陆前赋值" + (preDateStr = datePickerField.getText()));
                preDateStr = datePickerField.getText();
            } else {
                String nextDateStr = datePickerField.getText();
                if (preDateStr.equals(nextDateStr)) {
                    System.out.println("登陆后: " + nextDateStr);
                } else {
                    preDateStr = nextDateStr;
                    System.out.println("刷新！");
                    showHandle();
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {

        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }
    }

    class DeleteItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = infoJTable.getSelectedRows();
            StringBuilder deleteFailedInfo = new StringBuilder();
            StringBuilder deleteSuccessInfo = new StringBuilder();
            for (int selectedRow : selectedRows) {
                System.out.println("选中的行数：" + selectedRow);
                try {
                    String eventId = getEventId(infoDefaultModel, selectedRow);
                    if (!qaSimulator.deleteEvent(eventId)) {
                        deleteFailedInfo.append(" eventId: ").append(eventId).append(" 删除失败; ");
                    } else {
                        deleteSuccessInfo.append(" eventId: ").append(eventId).append(" 删除成功; ");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            showHandle();
            JOptionPane.showMessageDialog(null,
                    deleteSuccessInfo.toString() + deleteFailedInfo.toString(),
                    "删除信息", JOptionPane.INFORMATION_MESSAGE);

        }
    }

    class UpdateItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (checkDateLegal()) {
                    int[] selectedRows = infoJTable.getSelectedRows();
                    StringBuilder updateFailedStr = new StringBuilder();
                    StringBuilder updateSuccessStr = new StringBuilder();
                    for (int selectedRow : selectedRows) {
                        System.out.println("选中的行数：" + selectedRow);
                        try {
                            String eventId = getEventId(infoDefaultModel, selectedRow);
                            String title = getTitle(infoDefaultModel, selectedRow);
                            String content = getContent(infoDefaultModel, selectedRow);
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
                    showHandle();
                    JOptionPane.showMessageDialog(null,
                            updateSuccessStr.toString() + updateFailedStr.toString(),
                            "更新信息", JOptionPane.INFORMATION_MESSAGE);

                }
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }
}

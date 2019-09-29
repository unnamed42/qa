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
import java.util.*;
import java.util.List;

public class QaUserInterface {
    /**
     * set table information here
     * COLUMN_NAMES: contain the table field name
     * EVENT_ID_COLUMN_POS: index of eventid
     * TITLE_COLUMN_POS: index of title
     * CONTENT_COLUMN_POS: index of content
     * DATE_COLUMN_POS: index of date
     * you can use "Find Usage" to find the way to use it.
     **/
    final static Object[] COLUMN_NAMES = {"eventId", "title", "content", "date"};
    final static int EVENT_ID_COLUMN_POS = 0;
    final static int TITLE_COLUMN_POS = 1;
    final static int CONTENT_COLUMN_POS = 2;
    final static int DATE_COLUMN_POS = 3;
    /**
     * filename: the file name to store login info
     * username: represent the field name for account in login info
     * userpasswd: represent the field name for password in login info
     **/
    private final String filename = ".login.properties";
    private final String username = "UserName";
    private final String userpasswd = "UserPassWd";
    /**
     * preDateStr: to avoid Repeated inquiries the qa info while user has selected the date from the datepicker
     **/
    private String preDateStr = "";
    /**
     * these fields is used to create the user interface
     **/
    private JFrame mainFrame;
    private DatePicker datePickerField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private JTextField titleJTextField;
    private JTextArea contentJTextArea;
    private JButton loginJButton;
    private JButton addJButton;
    private JButton showJButton;
    private JButton queryJButton;
    private JButton batchAddJButton;
    private JPanel leftSideJpanel;
    private JPanel rightSideJpanel;
    private JScrollPane jInfoJTableScrollPane;
    private JTable infoJTable;
    private JMenuItem infoDeleteMenuItem;
    private JMenuItem infoUpdateMenuItem;
    private JTable queryJTable;
    private JMenuItem queryDeleteMenuItem;
    private JMenuItem queryUpdateMenuItem;
    private DefaultTableModel infoDefaultModel;
    private DefaultTableModel queryDefaultModel;
    private Object[][] originData = {
    };
    /**
     * accept a qaSimulator
     **/
    private QaSimulator qaSimulator;

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
        mainFrame = new JFrame("QA-beta" + " 未登陆");
        mainFrame.setSize(800, 500);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    clearResource();
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

    /**
     * create the bottom table
     */
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
        infoUpdateMenuItem = new JMenuItem("更新");
        infoDeleteMenuItem = new JMenuItem("删除");
        operateOnJTable(infoJTable, infoDeleteMenuItem, infoUpdateMenuItem);
        jInfoJTableScrollPane = new JScrollPane(infoJTable);
        //必须在构造方法构建JScrollPane才能显示Table

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setGridBagConstraints(gridBagConstraints, 0, 6, 4, 4,
                1, 1, GridBagConstraints.BOTH);
        return gridBagConstraints;
    }

    /**
     * add menu item to table
     */
    private void operateOnJTable(JTable infoJTable, JMenuItem deleteMenuItem, JMenuItem updateMenuItem) {
        JPopupMenu jPopupMenu = new JPopupMenu();
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

    /**
     * create the left side of user interface
     * include the functional button and date picker
     */
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

    /**
     * create the right side of user interface
     * include the title textfield and content textfield
     */
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

    /**
     * check if the input date is legal
     *
     * @return true:legal false: illegal
     */
    private boolean checkDateLegal(String date) throws ParseException {
        if (date == null || "".equals(date)) {
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
                || date.equals(currentDay)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "不是周六周末也不是当前日期，不能操作的亲！",
                    "非法日期", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * clean the resource
     */
    private void clearResource() throws IOException {
        System.out.println("清理资源");
        if (qaSimulator != null) {
            qaSimulator.closeHttpClient();
        }
    }

    /**
     * refresh the table upon the start date and the end date
     *
     * @param startDate:        start date
     * @param endDate:          end date
     * @param infoDefaultModel: table model to be filled
     * @param infoJTable:       table to be filled
     */
    private void fillJTable(String startDate, String endDate, DefaultTableModel infoDefaultModel, JTable infoJTable) {
        try {
            Object[][] rawData = qaSimulator.getDateContent(startDate, endDate);
            if (rawData.length == 0) {
                System.out.println("startDate： " + startDate + " endDate: " + endDate + " 表格数据为空，或者获取失败");
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
            if (rawData.length == 0) {
                infoDefaultModel.addRow(new Object[]{});
                for (int column = 0; column < COLUMN_NAMES.length; column++) {
                    if (column == DATE_COLUMN_POS) {
                        infoDefaultModel.setValueAt(endDate, 0, column);
                    } else {
                        infoDefaultModel.setValueAt("", 0, column);
                    }
                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void fillQueryJTable(String startDate, String endDate, DefaultTableModel infoDefaultModel) throws IOException, ParseException {
        Object[][] rawData = qaSimulator.getDateContent(startDate, endDate);
        if (rawData.length == 0) {
            System.out.println("startDate： " + startDate + " endDate: " + endDate + " 表格数据为空，或者获取失败");
        }
        int deleteCount = infoDefaultModel.getRowCount() - rawData.length;
        while (deleteCount > 0) {
            infoDefaultModel.removeRow(infoDefaultModel.getRowCount() - 1);
            deleteCount--;
        }
        for (int row = 0; row < infoDefaultModel.getRowCount(); row++) {
            for (int column = 0; column < COLUMN_NAMES.length; column++) {
                infoDefaultModel.setValueAt("", row, column);
            }
        }
        List<String> allQueryDay = BuildDateTable.buildDataTable(startDate, endDate);  //记录所有日期的列表
        HashMap<String, ArrayList<Object[]>> rawDataMap = new HashMap<>(allQueryDay.size());//记录对象日期的内容，一个日期可能有几条内容
        for (Object[] content : rawData) {
            if (rawDataMap.get(content[DATE_COLUMN_POS].toString()) != null) {
                rawDataMap.get(content[DATE_COLUMN_POS].toString()).add(content);
            } else {
                ArrayList<Object[]> tmpContent = new ArrayList<>(2);
                tmpContent.add(content);
                rawDataMap.put(content[DATE_COLUMN_POS].toString(), tmpContent);
            }
        }

        List<Boolean> result = new ArrayList<>(allQueryDay.size()); //记录每一天是否有内容
        for (String date : allQueryDay) {
            if (rawDataMap.get(date) != null) {
                result.add(true);
            } else {
                result.add(false);
            }
        }
        int realRow = 0;
        for (int row = 0; row < allQueryDay.size(); row++) {
            for (int column = 0; column < COLUMN_NAMES.length; column++) {//按行写，每一列的每一列循环一次
                if (realRow >= infoDefaultModel.getRowCount()) {
                    infoDefaultModel.addRow(new Object[]{});
                }
                if (result.get(row) && rawDataMap.get(allQueryDay.get(row)).size() > 0) {//判断该天是否有内容且内容是否已经全部写完
                    System.out.printf("allQueryDay.get(row) is %s\n", allQueryDay.get(row));
                    System.out.printf("rawMap.get size is %d\n", rawDataMap.get(allQueryDay.get(row)).size());
                    System.out.printf("row is %d column is %d value is %s\n", row, column, rawDataMap.get(allQueryDay.get(row)).get(0)[column]);
                    infoDefaultModel.setValueAt(rawDataMap.get(allQueryDay.get(row)).get(0)[column], realRow, column);
                } else {
                    if (column == DATE_COLUMN_POS) {
                        infoDefaultModel.setValueAt(allQueryDay.get(row), realRow, column);//如果是日期列，写上日期
                    } else {
                        infoDefaultModel.setValueAt("", realRow, column);
                    }
                }
            }
            if (result.get(row) && rawDataMap.get(allQueryDay.get(row)).size() > 0) {
                rawDataMap.get(allQueryDay.get(row)).remove(0);
                if (rawDataMap.get(allQueryDay.get(row)).size() > 0) {
                    row--;
                }
            }
            realRow++;
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

    /**
     * deal with the login event
     */
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
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    loginOperate(loginJFrame, userNameJTextField, passwordJPasswordField);
                }
            }
        });
        loginJFrame.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                if (aComponent == userNameJTextField) {
                    return passwordJPasswordField;
                } else {
                    return userNameJTextField;
                }
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                if (aComponent == userNameJTextField) {
                    return passwordJPasswordField;
                } else {
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
                mainFrame.setTitle("QA-beta: 用户 " + userName);
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
                String date = datePickerField.getText();
                showHandle(date, date, infoDefaultModel, infoJTable);
            } else {
                JOptionPane.showMessageDialog(null, "用户名或密码错误", "登陆失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * auto login
     */
    private void autoLogin() throws IOException {
        File loginFile = new File(filename);
        if (loginFile.exists()) {
            Properties loginProperties = new Properties();
            FileInputStream in = new FileInputStream(filename);
            loginProperties.load(new InputStreamReader(in, "utf-8"));
            String userName = loginProperties.getProperty(username);
            String userPassWd = loginProperties.getProperty(userpasswd);
            mainFrame.setTitle("正在登陆，请等待。。。。。");
            qaSimulator = new QaSimulator(userName, userPassWd);
            qaSimulator.login();
            if (qaSimulator.isIfLogin()) {
                JOptionPane.showMessageDialog(null, "从配置信息中,登陆成功,若想更换用户,请点击登陆按钮登陆",
                        "INFORMATION_MESSAGE",
                        JOptionPane.INFORMATION_MESSAGE);
                String date = datePickerField.getText();
                showHandle(date, date, infoDefaultModel, infoJTable);
                mainFrame.setTitle("QA-beta: " + " 用户: " + userName);
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

    /**
     * deal with the add event
     */
    private void addHandle() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            String title = titleJTextField.getText();
            String content = contentJTextArea.getText();
            String date = datePickerField.getText();
            try {
                if (checkDateLegal(datePickerField.getText())) {
                    if (qaSimulator.addEvent(title, date, content)) {
                        titleJTextField.setText("");
                        contentJTextArea.setText("");
                        showHandle(date, date, infoDefaultModel, infoJTable);
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

    /**
     * deal with the show event
     */
    private void showHandle(String startDate, String endDate, DefaultTableModel defaultTableModel, JTable jTable) {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            fillJTable(startDate, endDate, defaultTableModel, jTable);
        } else {
            JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                    "未登陆", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQueryTableHandle(String startDate, String endDate, DefaultTableModel defaultTableModel) throws IOException, ParseException {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            fillQueryJTable(startDate, endDate, defaultTableModel);
        } else {
            JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                    "未登陆", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void queryHandle() {
        if (qaSimulator != null && qaSimulator.isIfLogin()) {
            final String[] preStartDay = {""};
            final String[] preEndDay = {""};
            JFrame queryJFrame = new JFrame("欢迎查询");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            queryJFrame.setLayout(new GridBagLayout());
            queryJFrame.setLocationRelativeTo(null);
            queryJFrame.setSize(900, 600);
            queryJFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            queryJFrame.setLocationRelativeTo(null);

            JLabel startDateLabel = new JLabel("开始日期");
            JLabel endDateLabel = new JLabel("结束日期");
            startDatePicker = new DatePicker(new Date(), "yyyy-MM-dd",
                    new Font("Times New Roman", Font.BOLD, 14), new Dimension(100, 30));
            endDatePicker = new DatePicker(new Date(), "yyyy-MM-dd",
                    new Font("Times New Roman", Font.BOLD, 14), new Dimension(100, 30));
            JButton queryConfirmJButton = new JButton("查询");
            JTextField title = new JTextField(10);
            title.setToolTipText("title");
            JTextField content = new JTextField(20);
            content.setToolTipText("content");
            batchAddJButton = new JButton("批量添加");
            queryDefaultModel = new DefaultTableModel(originData, COLUMN_NAMES) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != EVENT_ID_COLUMN_POS && column != DATE_COLUMN_POS;
                }
            };
            queryJTable = new JTable(queryDefaultModel);
            queryJTable.setShowHorizontalLines(true);
            queryJTable.setShowVerticalLines(true);
            queryJTable.setGridColor(Color.black);
            queryJTable.setRowHeight(30);
            queryJTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
            queryDeleteMenuItem = new JMenuItem("删除");
            queryUpdateMenuItem = new JMenuItem("更新");
            operateOnJTable(queryJTable, queryDeleteMenuItem, queryUpdateMenuItem);
            JScrollPane queryJTableScrollPane = new JScrollPane(queryJTable);
            queryConfirmJButton.addActionListener(e -> {
                String startDate = startDatePicker.getText();
                String endDate = endDatePicker.getText();
                if (startDate.compareTo(endDate) > 0) {
                    JOptionPane.showMessageDialog(null, "开始日期大于结束日期", "非法日期", JOptionPane.ERROR_MESSAGE);
                } else {
                    preStartDay[0] = startDate;
                    preEndDay[0] = endDate;
                    try {
                        fillQueryJTable(startDate, endDate, queryDefaultModel);
                    } catch (IOException | ParseException e1) {
                        preStartDay[0] = "";
                        preEndDay[0] = "";
                        e1.printStackTrace();
                    }
                }
            });

            batchAddJButton.addActionListener(e -> {
                String startDate = startDatePicker.getText();
                String endDate = endDatePicker.getText();
                String titleStr = title.getText();
                String contentStr = content.getText();
                if ("".equals(titleStr) || "".equals(contentStr)) {
                    JOptionPane.showMessageDialog(null, "title 和 content不能为空",
                            "输入不合法", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    List<String> dateList = BuildDateTable.buildDataTable(startDate, endDate);
                    for (String date : dateList) {
                        if (checkDateLegal(date)) {
                            if (!BuildDateTable.checkWeekends(date)) {
                                qaSimulator.addEvent(titleStr, date, contentStr);
                            }
                        }
                    }
                    if ("".equals(preEndDay[0]) && "".equals(preStartDay[0])) {
                        fillQueryJTable(startDate, endDate, queryDefaultModel);
                    } else {
                        fillQueryJTable(preStartDay[0], preEndDay[0], queryDefaultModel);
                    }
                } catch (ParseException | IOException e1) {
                    e1.printStackTrace();
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
                    new Insets(20, 5, 0, 5));
            queryJFrame.add(endDatePicker, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 2, 6, 6,
                    2, 3, 0, GridBagConstraints.HORIZONTAL);
            queryJFrame.add(queryConfirmJButton, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 2, 8, 4,
                    2, 2, 0, GridBagConstraints.HORIZONTAL);
            queryJFrame.add(title, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 6, 8, 6,
                    2, 6, 0, GridBagConstraints.HORIZONTAL);
            queryJFrame.add(content, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 2, 10, 6,
                    2, 3, 0, GridBagConstraints.HORIZONTAL);
            queryJFrame.add(batchAddJButton, gridBagConstraints);

            setGridBagConstraints(gridBagConstraints, 0, 12, 8,
                    15, 5, 15, GridBagConstraints.BOTH);
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

    private String getDate(DefaultTableModel infoDefaultModel, int row) {
        return infoDefaultModel.getValueAt(row, DATE_COLUMN_POS).toString();
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
                String date = datePickerField.getText();
                showHandle(date, date, infoDefaultModel, infoJTable);
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
                    String date = datePickerField.getText();
                    showHandle(date, date, infoDefaultModel, infoJTable);
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
            if (e.getSource() == infoDeleteMenuItem) {
                int[] selectedRows = infoJTable.getSelectedRows();
                StringBuilder deleteFailedInfo = new StringBuilder();
                StringBuilder deleteSuccessInfo = new StringBuilder();
                if (selectedRows.length == 0) {
                    JOptionPane.showMessageDialog(null,
                            "您还未选中任意行",
                            "未选中", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
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
                String date = datePickerField.getText();
                showHandle(date, date, infoDefaultModel, infoJTable);
                if (!"".equals(deleteSuccessInfo.toString() + deleteFailedInfo.toString())) {
                    JOptionPane.showMessageDialog(null,
                            deleteSuccessInfo.toString() + deleteFailedInfo.toString(),
                            "删除信息", JOptionPane.INFORMATION_MESSAGE);
                }

            } else {
                if (e.getSource() == queryDeleteMenuItem) {
                    int[] selectedRows = queryJTable.getSelectedRows();
                    StringBuilder deleteFailedInfo = new StringBuilder();
                    StringBuilder deleteSuccessInfo = new StringBuilder();
                    if (selectedRows.length == 0) {
                        JOptionPane.showMessageDialog(null,
                                "您还未选中任意行",
                                "未选中", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    for (int selectedRow : selectedRows) {
                        System.out.println("选中的行数：" + selectedRow);
                        try {
                            String eventId = getEventId(queryDefaultModel, selectedRow);
                            if (qaSimulator.deleteEvent(eventId)) {
                                deleteSuccessInfo.append(" eventId: ").append(eventId).append(" 删除成功; ");
                            } else {
                                deleteFailedInfo.append(" eventId: ").append(eventId).append(" 删除失败; ");
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    String startDate = startDatePicker.getText();
                    String endDate = endDatePicker.getText();
                    try {
                        showQueryTableHandle(startDate, endDate, queryDefaultModel);
                    } catch (IOException | ParseException e1) {
                        e1.printStackTrace();
                    }
                    if (!"".equals(deleteSuccessInfo.toString() + deleteFailedInfo.toString())) {
                        JOptionPane.showMessageDialog(null,
                                deleteSuccessInfo.toString() + deleteFailedInfo.toString(),
                                "删除信息", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }

    class UpdateItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == infoUpdateMenuItem) {
                try {
                    if (checkDateLegal(datePickerField.getText())) {
                        int[] selectedRows = infoJTable.getSelectedRows();
                        StringBuilder updateFailedStr = new StringBuilder();
                        StringBuilder updateSuccessStr = new StringBuilder();
                        String date = datePickerField.getText();
                        if (selectedRows.length == 0) {
                            JOptionPane.showMessageDialog(null,
                                    "您还未选中任意行",
                                    "未选中", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        for (int selectedRow : selectedRows) {
                            System.out.println("选中的行数：" + selectedRow);
                            try {
                                String eventId = getEventId(infoDefaultModel, selectedRow);
                                String title = getTitle(infoDefaultModel, selectedRow);
                                String content = getContent(infoDefaultModel, selectedRow);
                                if ("".equals(eventId)) {
                                    if (!qaSimulator.addEvent(title, date, content)) {
                                        System.out.println("title : " + title + "添加失败");
                                    } else {
                                        System.out.printf("title : " + title + "添加成功");
                                    }
                                } else {
                                    if (!qaSimulator.updateEvent(title, date, content, eventId)) {
                                        updateFailedStr.append(" eventId ").append(eventId).append(" update failed ");
                                    } else {
                                        updateSuccessStr.append(" eventId ").append(eventId).append(" update success; ");
                                    }
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        showHandle(date, date, infoDefaultModel, infoJTable);
                        if (!"".equals(updateSuccessStr.toString() + updateFailedStr.toString())) {
                            JOptionPane.showMessageDialog(null,
                                    updateSuccessStr.toString() + updateFailedStr.toString(),
                                    "更新信息", JOptionPane.INFORMATION_MESSAGE);
                        }

                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            } else {
                if (e.getSource() == queryUpdateMenuItem) {
                    int[] selectedRows = queryJTable.getSelectedRows();
                    StringBuilder updateFailedStr = new StringBuilder();
                    StringBuilder updateSuccessStr = new StringBuilder();
                    String startDate = startDatePicker.getText();
                    String endDate = endDatePicker.getText();
                    if (selectedRows.length == 0) {
                        JOptionPane.showMessageDialog(null,
                                "您还未选中任意行",
                                "未选中", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    for (int selectedRow : selectedRows) {
                        System.out.println("选中的行数：" + selectedRow);
                        try {
                            String eventId = getEventId(queryDefaultModel, selectedRow);
                            String title = getTitle(queryDefaultModel, selectedRow);
                            String content = getContent(queryDefaultModel, selectedRow);
                            String date = getDate(queryDefaultModel, selectedRow);
                            if ("".equals(eventId)) {
                                if (qaSimulator.addEvent(title, date, content)) {
                                    System.out.println("title : " + title + "添加成功");
                                } else {
                                    System.out.printf("title : " + title + "添加失败");
                                }
                            } else {
                                if (!qaSimulator.updateEvent(title, date, content, eventId)) {
                                    updateFailedStr.append(" eventId ").append(eventId).append(" update failed ");
                                } else {
                                    updateSuccessStr.append(" eventId ").append(eventId).append(" update success; ");
                                }
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    try {
                        showQueryTableHandle(startDate, endDate, queryDefaultModel);
                    } catch (IOException | ParseException e1) {
                        e1.printStackTrace();
                    }
                    if (!"".equals(updateFailedStr.toString() + updateSuccessStr.toString())) {
                        JOptionPane.showMessageDialog(null,
                                updateSuccessStr.toString() + updateFailedStr.toString(),
                                "更新信息", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }
}

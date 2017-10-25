import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * create with PACKAGE_NAME
 * USER: husterfox
 *
 * @author husterfox
 */
public class QaUserInterface {
    private final Object[] columnNames = {"eventId", "title", "content"};
    private JTextField dateJTextField;
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
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
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

    private void addPopMenu2JTable() {
        jPopupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("删除");
        JMenuItem updateMenuItem = new JMenuItem("更新");
        jPopupMenu.add(deleteMenuItem);
        jPopupMenu.add(updateMenuItem);
        deleteMenuItem.addActionListener(e -> {
            int[] selectedRows = infoJTable.getSelectedRows();
            boolean[] deleteCount = new boolean[selectedRows.length];
            for (int i = 0; i < selectedRows.length; i++) {
                System.out.println("选中的行数：" + selectedRows[i]);
                try {
                    deleteCount[i] = qaSimulator.deleteEvent(infoDefaultModel.getValueAt(selectedRows[i], 0)
                            .toString());
                    if (!deleteCount[i]) {
                        JOptionPane.showMessageDialog(null, "eventId:" +
                                        infoDefaultModel.getValueAt(selectedRows[i], 0).toString() + "删除失败",
                                "删除失败", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            for (int i = 0; i < deleteCount.length; i++) {
                if (deleteCount[i]) {
                    infoDefaultModel.removeRow(selectedRows[i]);
                }
            }
        });
        updateMenuItem.addActionListener(e -> {
            int[] selectedRows = infoJTable.getSelectedRows();
            boolean[] updateCount = new boolean[selectedRows.length];
            for (int i = 0; i < selectedRows.length; i++) {
                System.out.println("选中的行数：" + selectedRows[i]);
                try {
                    String eventId = infoDefaultModel.getValueAt(selectedRows[i], 0).toString();
                    String title = infoDefaultModel.getValueAt(selectedRows[i], 1).toString();
                    String content = infoDefaultModel.getValueAt(selectedRows[i], 2).toString();
                    String date = dateJTextField.getText();
                    updateCount[i] = qaSimulator.updateEvent(title, date, content, eventId);
                    if (!updateCount[i]) {
                        JOptionPane.showMessageDialog(null, "eventId:" +
                                        infoDefaultModel.getValueAt(selectedRows[i], 0).toString() + "更新失败",
                                "更新失败", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private GridBagConstraints createLeftSideJPanel() {
        leftSideJpanel = new JPanel();
        leftSideJpanel.setLayout(new GridLayout(5, 1));
        JPanel dateLabelAndTextFieldJPanel = new JPanel(new FlowLayout());
        JLabel dateLabel = new JLabel("日期");
        dateJTextField = new JTextField(15);
        dateJTextField.setToolTipText("格式：2017-10-24");
        dateLabelAndTextFieldJPanel.add(dateLabel);
        dateLabelAndTextFieldJPanel.add(dateJTextField);
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
                JLabel userNameLabel = new JLabel("userName");
                JLabel passWordLabel = new JLabel("passWord");
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
                            Date dateToday = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String date = simpleDateFormat.format(dateToday);
                            dateJTextField.setText(date);
                        } else {
                            JOptionPane.showMessageDialog(null, "用户名或密码错误", "标题", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });

                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 1;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 1;
                gridBagConstraints.weighty = 1;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(userNameLabel, gridBagConstraints);

                gridBagConstraints.gridy = 1;
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 1;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(userNameJTextField, gridBagConstraints);

                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 1;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 1;
                gridBagConstraints.weighty = 1;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(passWordLabel, gridBagConstraints);

                gridBagConstraints.gridy = 3;
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.gridheight = 1;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 1;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(passwordJPasswordField, gridBagConstraints);

                gridBagConstraints.gridy = 6;
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridwidth = 6;
                gridBagConstraints.gridheight = 2;
                gridBagConstraints.weightx = 3;
                gridBagConstraints.weighty = 1;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                loginJFrame.add(loginJButton, gridBagConstraints);
                loginJFrame.setVisible(true);
            }
            if (e.getSource() == addJButton) {
                if (qaSimulator != null && qaSimulator.isIfLogin()) {
                    String title = titleJTextField.getText();
                    String content = contentJTextArea.getText();
                    System.out.println("content 中的内容" + content);
                    String date = dateJTextField.getText();
                    try {
                        if (qaSimulator.addEvent(title, date, content)) {
                            JOptionPane.showMessageDialog(null, "添加成功",
                                    "添加成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "添加失败，检查日期是否正确，以及title 和content是否为空",
                                    "未登陆", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                            "未登陆", JOptionPane.ERROR_MESSAGE);
                }
            }
            if (e.getSource() == showJButton) {
                if (qaSimulator != null && qaSimulator.isIfLogin()) {
                    String date = dateJTextField.getText();
                    try {
                        Object[][] rawData = qaSimulator.getEventIdList(date, date);
                        if (rawData != null) {
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
                        } else {
                            JOptionPane.showMessageDialog(null, "sad 没能获取event，或者event为空",
                                    "获取event失败", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "您还未登陆,请先登陆",
                            "未登陆", JOptionPane.ERROR_MESSAGE);
                }
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

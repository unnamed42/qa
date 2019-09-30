package qa.mvc;

import lombok.val;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Locale;
import java.util.function.Consumer;

import org.jdatepicker.JDatePicker;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.impl.UtilDateModel;

import qa.util.GBCBuilder;

public class MainFrame extends JFrame {

    private JDatePicker datepicker;
    private JTextField titleInput;
    private JTextArea contentInput;
    private Object[][] dataSource = {};
    private final MainFrameController controller = new MainFrameController();

    public MainFrame() {
        // main window properties
        super("QA-beta");
        JFrame.setDefaultLookAndFeelDecorated(true);
        this.setSize(800, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // register destructor
        this.addWindowListener(closed(e -> controller.close()));
        // ui setup
        val mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);
        this.setupLeftPanel(mainLayout);
        this.setupRightPanel(mainLayout);
        this.setupBottomPanel(mainLayout);
        // finish setup, show up
        this.pack();
        this.setVisible(true);
    }

    private void setupLeftPanel(GridBagLayout mainLayout) {
        val panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        // datepicker
        val datepickerFactory = new JDateComponentFactory(UtilDateModel.class,
                new DateFormatter("yyyy-MM-dd"), Locale.getDefault());
        this.datepicker = datepickerFactory.createJDatePicker();
        // buttons
        val loginButton = new JButton("登录");
        val addButton = new JButton("添加");
        val showButton = new JButton("显示");
        val queryButton = new JButton("查询");
        // build ui
        panel.add((Component) this.datepicker);
        panel.add(loginButton);
        panel.add(addButton);
        panel.add(showButton);
        panel.add(queryButton);

        val constraints = new GBCBuilder().position(0, 0, 1, 1)
                .weight(0.5, 1).fill(GridBagConstraints.BOTH).build();
        mainLayout.setConstraints(panel, constraints);
        this.add(panel);
    }

    private void setupRightPanel(GridBagLayout mainLayout) {
        val panel = new JPanel();
        val layout = new GridBagLayout();
        panel.setLayout(layout);
        this.titleInput = new JTextField(30);
        this.titleInput.setToolTipText("输入标题");
        this.contentInput = new JTextArea(5, 30);
        this.contentInput.setToolTipText("输入内容");
        val scrollPane = new JScrollPane(this.contentInput);

        val titleConstraints = new GBCBuilder().position(0, 0, 5, 1).weight(1, 0.5)
                .fill(GridBagConstraints.BOTH).insets(new Insets(10, 10, 10, 10)).build();
        layout.setConstraints(this.titleInput, titleConstraints);
        panel.add(this.titleInput);

        val contentConstraints = new GBCBuilder().position(2, 1, 5, 4).weight(1, 0.5)
                .fill(GridBagConstraints.BOTH).insets(new Insets(10, 10, 10, 10)).build();
        layout.setConstraints(scrollPane, contentConstraints);
        panel.add(scrollPane);

        val constraints = new GBCBuilder().position(1, 0, 3, 5).weight(1, 1)
                .fill(GridBagConstraints.BOTH).build();
        mainLayout.setConstraints(panel, constraints);
        this.add(panel);
    }

    private void setupBottomPanel(GridBagLayout mainLayout) {
        val table = new JTable(new QATableModel(dataSource));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(Color.black);
        table.setRowHeight(30);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));

        val popupMenu = new JPopupMenu();
        val updateMenuItem = new JMenuItem("更新");
        val deleteMenuItem = new JMenuItem("删除");
        popupMenu.add(updateMenuItem);
        popupMenu.add(deleteMenuItem);
        table.addMouseListener(clicked(e -> {}));

        val scrollPane = new JScrollPane(table);
        val constraints = new GBCBuilder().position(0, 6, 4, 4).weight(1, 1)
                .fill(GridBagConstraints.BOTH).build();
        mainLayout.setConstraints(scrollPane, constraints);
        this.add(scrollPane);
    }

    private static WindowListener closed(Consumer<WindowEvent> consumer) {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                consumer.accept(e);
            }
        };
    }

    private static MouseListener clicked(Consumer<MouseEvent> consumer) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                consumer.accept(e);
            }
        };
    }

}

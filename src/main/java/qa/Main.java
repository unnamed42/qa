package qa;

import qa.mvc.MainFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }

}

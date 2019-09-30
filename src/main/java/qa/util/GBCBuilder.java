package qa.util;

import java.awt.*;

public class GBCBuilder {

    private final GridBagConstraints constraints = new GridBagConstraints();

    public GBCBuilder w(int width) {
        constraints.gridwidth = width;
        return this;
    }

    public GBCBuilder h(int height) {
        constraints.gridheight = height;
        return this;
    }

    public GBCBuilder x(int x) {
        constraints.gridx = x;
        return this;
    }

    public GBCBuilder y(int y) {
        constraints.gridy = y;
        return this;
    }

    public GBCBuilder weightx(double wx) {
        constraints.weightx = wx;
        return this;
    }

    public GBCBuilder weighty(double wy) {
        constraints.weightx = wy;
        return this;
    }

    public GBCBuilder position(int x, int y, int width, int height) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        return this;
    }

    public GBCBuilder weight(double x, double y) {
        constraints.weightx = x;
        constraints.weighty = y;
        return this;
    }

    public GBCBuilder anchor(int anchor) {
        constraints.anchor = anchor;
        return this;
    }

    public GBCBuilder fill(int fill) {
        constraints.fill = fill;
        return this;
    }

    public GBCBuilder insets(Insets insets) {
        constraints.insets = insets;
        return this;
    }

    public GridBagConstraints build() {
        return constraints;
    }

}

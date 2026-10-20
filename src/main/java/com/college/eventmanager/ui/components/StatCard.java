package com.college.eventmanager.ui.components;

import com.college.eventmanager.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Custom KPI Card widget displaying a metric title, numeric value, and color accent.
 */
public class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subLabel;
    private final Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 6));
        setBackground(Theme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        subLabel = new JLabel(subtitle);
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(accentColor);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subLabel);

        add(textPanel, BorderLayout.CENTER);

        // Visual color bar on the left
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 40));
        bar.setBackground(accentColor);
        add(bar, BorderLayout.WEST);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subLabel.setText(subtitle);
    }
}

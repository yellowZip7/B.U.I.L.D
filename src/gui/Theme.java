package gui;

import java.awt.*;

public class Theme {

    public static final Color PRIMARY    = new Color(10,  25, 47);
    public static final Color SECONDARY  = new Color(17,  34, 64);
    public static final Color GOLD       = new Color(212, 175, 55);
    public static final Color GOLD_HOVER = new Color(230, 193, 90);
    public static final Color WHITE      = new Color(245, 247, 250);
    public static final Color LIGHT_TEXT = new Color(184, 193, 204);

    public static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font BODY_FONT   = new Font("Segoe UI", Font.PLAIN, 14);

    // Alias kept so existing modules that reference Theme.HEADER still compile
    public static final Font HEADER = HEADER_FONT;
}

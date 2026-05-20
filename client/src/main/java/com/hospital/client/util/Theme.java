package com.hospital.client.util;

import java.awt.*;

public final class Theme {
    // Core palette
    public static final Color NAVY        = new Color(0x1B2A4A);
    public static final Color BLUE        = new Color(0x2F80ED);
    public static final Color BLUE_DARK   = new Color(0x1A6BC8);
    public static final Color GREEN       = new Color(0x27AE60);
    public static final Color RED         = new Color(0xE74C3C);
    public static final Color ORANGE      = new Color(0xF39C12);
    public static final Color PURPLE      = new Color(0x8E44AD);
    public static final Color TEAL        = new Color(0x16A085);

    // Backgrounds
    public static final Color BG          = new Color(0xF4F6F9);
    public static final Color CARD        = new Color(0xFFFFFF);
    public static final Color SIDEBAR_BG  = new Color(0x1B2A4A);
    public static final Color SIDEBAR_HOV = new Color(0x2C3E6A);
    public static final Color SIDEBAR_ACT = new Color(0x2F80ED);

    // Text
    public static final Color TEXT        = new Color(0x212121);
    public static final Color TEXT_SEC    = new Color(0x757575);
    public static final Color TEXT_HINT   = new Color(0xBDBDBD);
    public static final Color SIDEBAR_TXT = new Color(0xCDD9F0);
    public static final Color WHITE       = Color.WHITE;

    // Borders
    public static final Color BORDER      = new Color(0xE0E0E0);
    public static final Color DIVIDER     = new Color(0xEEEEEE);

    // Status
    public static final Color OK          = new Color(0x27AE60);
    public static final Color WARN        = new Color(0xF39C12);
    public static final Color ERR         = new Color(0xE74C3C);
    public static final Color INFO        = new Color(0x2F80ED);

    // Fonts
    public static final Font F_TITLE      = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_SECTION    = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font F_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_MONO       = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font F_SIDEBAR    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SIDEBAR_SM = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font F_BADGE      = new Font("Segoe UI", Font.BOLD,  10);
    public static final Font F_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font F_TABLE      = new Font("Segoe UI", Font.PLAIN, 12);

    // Dimensions
    public static final int SIDEBAR_W = 220;
    public static final int PAD       = 16;
    public static final int PAD_SM    = 8;
    public static final int RADIUS    = 10;

    private Theme() {}
}

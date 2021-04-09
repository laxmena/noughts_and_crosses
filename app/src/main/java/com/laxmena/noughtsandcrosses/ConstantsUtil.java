package com.laxmena.noughtsandcrosses;

import android.graphics.Color;

import java.util.ArrayList;

public class ConstantsUtil {
    public static int ROW = 3;
    public static int COLUMN = 3;
    public static int[] BOARD_UI_POSITIONS = {R.id.cell00, R.id.cell01, R.id.cell02,
                                              R.id.cell10, R.id.cell11, R.id.cell12,
                                              R.id.cell20, R.id.cell21, R.id.cell22};

    public static String CROSS_STRING = "X";
    public static String NOUGHT_STRING = "O";
    public static String EMPTY_STRING = "";

    public static int CROSS_COLOR = Color.parseColor("#000000");
    public static int NOUGHT_COLOR = Color.parseColor("#000000");
    public static int DEFAULT_COLOR = Color.parseColor("#000000");

    public static final int EMPTY_VAL = 0;
    public static final int CROSS_VAL = 1;
    public static final int NOUGHT_VAL = 2;

    public static int UNFINISHED = -1;
    public static int DRAW = 0;
    public static int CROSS_WINS = 1;
    public static int NOUGHT_WINS = 2;

}

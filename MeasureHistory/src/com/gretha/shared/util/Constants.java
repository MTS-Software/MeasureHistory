package com.gretha.shared.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Constants {

	public final static String STYLESHEET = "/com/gretha/shared/resource/css/JMetroLightTheme.css";
	public final static String STYLESHEET_CHART = "/com/gretha/shared/resource/css/Chart.css";

	public final static String LICENSE_FILENAME = "application.license";

	public final static String APP_ICON_PROCESSMANAGER = "com/gretha/shared/resource/icons/processmanager_48.png";
	public final static String APP_ICON_PLCMANAGER = "com/gretha/shared/resource/icons/plcmanager_48.png";

	public static final String SPLASHSCREEN_IMAGE_PLCMANAGER = "com/gretha/shared/resource/icons/splashPLCManager_775x200.png";
	public static final String SPLASHSCREEN_IMAGE_PROCESSMANAGER = "com/gretha/shared/resource/icons/splashProcessManager_775x200.png";

	public final static String ICON_USER_LOGIN = "com/gretha/shared/resource/icons/user_login_64.png";
	public final static String ICON_LICENSE = "com/gretha/shared/resource/icons/license_24.png";
	public final static String ICON_ABOUT = "com/gretha/shared/resource/icons/info_24.png";
	public final static String ICON_HELP = "com/gretha/shared/resource/icons/help_24.png";
	public final static String ICON_SPC_INFO = "com/gretha/shared/resource/icons/spc_information_chart_810x563.png";
	public final static String ICON_SETTINGS = "com/gretha/shared/resource/icons/settings_256.png";
	public final static String ICON_RESTART = "com/gretha/shared/resource/icons/restart_24.png";
	public final static String ICON_OK = "com/gretha/shared/resource/icons/ok_48.png";
	public final static String ICON_SMILEY_GREEN = "com/gretha/shared/resource/icons/smiley_green_225.png";
	public final static String ICON_SMILEY_RED = "com/gretha/shared/resource/icons/smiley_red_200.png";
	public final static String ICON_SMILEY_YELLOW = "com/gretha/shared/resource/icons/smiley_yellow_225.png";
	public final static String ICON_PLAY = "com/gretha/shared/resource/icons/play_24.png";
	public final static String ICON_PAUSE = "com/gretha/shared/resource/icons/pause_24.png";
	public final static String ICON_STOP = "com/gretha/shared/resource/icons/stop_24.png";
	public final static String ICON_WEB_BROWSER = "com/gretha/shared/resource/icons/webbrowser48.png";

	public final static int LIMIT_RESULTS_STANDARD_SHOWN = 1000;
	public final static int LIMIT_RESULTS_FILTER_SHOWN = 100000;
	public final static int LIMIT_LAST_RESULT_TABLE_VIEW_SHOWN = 1000;
	public final static int DEFAULT_LAST_RESULT_TABLE_VIEW_SHOWN = 10;

	public final static double DEFAULT_PROCESS_ROOT_PANE_WIDTH = 1250;
	public final static double DEFAULT_PROCESS_ROOT_PANE_HEIGTH = 790;

	public final static double DEFAULT_ROOT_LAYOUT_WIDTH = 1250;
	public final static double DEFAULT_ROOT_LAYOUT_HEIGTH = 790;

	public final static int SPLASH_WIDTH = 795;
	public final static int SPLASH_HEIGHT = 220;
	public final static int THREAD_SPLASH_SLEEP_TIME = 500;
	public final static double FADE_TRANSITIONS_TIME = 4.0;
	public final static boolean SHOW_SPLASH_SCREEN = true;

	public final static ObservableList<String> PLC_TYPES = FXCollections.observableArrayList("S7_300_400_compatibel",
			"S7_200_compatibel", "S7_1200_compatibel", "S7_1500_compatibel", "WinAC_RTX_2010_compatibel2",
			"Logo_compatibel", "Logo0BA7_compatibel", "Logo0BA8_compatibel");

}

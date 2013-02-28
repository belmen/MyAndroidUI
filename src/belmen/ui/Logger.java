/*
 *	Copyright (c) 2012, Yulong Information Technologies
 *	All rights reserved.
 *  
 *  @Project: LibYulong
 *  @author: Robot	
 */
package belmen.ui;

import android.util.Log;

/**
 * 日志类
 * @author Robot
 * @weibo http://weibo.com/feng88724
 * @date Dec 14, 2012	
 */
public class Logger {
	
	private static boolean SHOW_FLAG = false;
	
	public static void e(String tag, String msg) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.e(tag, msg);
	}
	
	public static void d(String tag, String msg) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.d(tag, msg);
	}
	
	public static void i(String tag, String msg) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.i(tag, msg);
	}
	
	public static void v(String tag, String msg) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.v(tag, msg);
	}
	
	public static void w(String tag, String msg) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.w(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable e) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.e(tag, msg, e);
	}
	
	public static void d(String tag, String msg, Throwable e) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.d(tag, msg, e);
	}
	
	public static void i(String tag, String msg, Throwable e) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.d(tag, msg, e);
	}
	
	public static void v(String tag, String msg, Throwable e) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.v(tag, msg, e);
	}
	
	public static void w(String tag, String msg, Throwable e) {
		if(!SHOW_FLAG) {
			return;
		}
		Log.w(tag, msg, e);
	}
	
	//日志开关
	public static void setDebug(boolean debug) {
		SHOW_FLAG = debug;
	}
}

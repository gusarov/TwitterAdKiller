package ru.xkip.noad.twitter;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import java.io.File;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Xposed implements IXposedHookLoadPackage {
	private static final String tag = "NOAD";
	private static final String COM_TWITTER = "com.twitter.android";
	private static final String TAG = "TwitterAdfree: ";

	private static Context context;
	private static ClassLoader classLoader;
	private static String moduleVersionCode;
	private static String versionCode;

	static View.OnLayoutChangeListener OnLayoutChangeListener;

	static  {
		classLoader = null;
		context = null;

		OnLayoutChangeListener = new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View vw, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				HandleView(vw, false);
			}
		};
	}

	static void HandleView(final View view, boolean beenDelayed){
		ViewGroup vg = (ViewGroup) view;
		boolean isAd = false;
		int m = vg.getChildCount();
		for (int i = 0; i < m; i++) {
			View v = vg.getChildAt(i);
			// Log.i(tag, "vg1["+i+"]="+v);
			if (v.toString().contains("promoted") && v.getVisibility() == View.VISIBLE) {
				// Log.i(tag, "promoted=" + v.ge);
				isAd = true;
				// v.on
				break;
			}
		}

		/* FADE
		Log.i(tag, "is ad " + isAd);
		for (int i = 0; i < m; i++) {
			View v = vg.getChildAt(i);
			Log.i(tag, "vg["+i+"]="+v);
			if (isAd) {
				// v.setBackgroundColor(Color.argb(128, 255, 0, 0));
				v.setAlpha(0.02f);
			} else { // restore because of controls reuse
				// v.setBackgroundColor(Color.argb(255, 255, 255, 255));
				v.setAlpha(1.0f);
			}

		}
		*/

		if (beenDelayed) {
			view.setVisibility(isAd ? View.GONE : View.VISIBLE);
		} else {
			final boolean isItAd = isAd;
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					view.setVisibility(isItAd ? View.GONE : View.VISIBLE);
				}
			}, 1);
		}
	}

	public void log(String text)
	{
		// XposedBridge.log(text);
		// debug("_NOAD_ " + text);
		Log.i("NOAD", text);

		/*
		File dc = Environment.getDataDirectory();
		File logFile = new File(dc, "noadlog.txt");
		if (!logFile.exists())
		{
			try
			{
				logFile.createNewFile();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(text);
			buf.newLine();
			buf.flush();
			buf.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	// @Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		try {

			if (lpparam.packageName.contains("ru.xkip.noad.twitter")) {
				XposedHelpers.findAndHookMethod("ru.xkip.noad.twitter.XChecker", lpparam.classLoader, "isEnabled", XC_MethodReplacement.returnConstant(Boolean.TRUE));
			}


			if (lpparam.packageName.equals("com.twitter.android")){
				log("Loaded app: !!! " + lpparam.packageName);

				if (context == null) {
					Object activityThread = XposedHelpers.callStaticMethod(
							XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
					context = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
					log("Context set!");
				}

				String versionCode = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionName;
				String moduleVersionCode = context.getPackageManager().getPackageInfo("ru.xkip.noad.twitter", 0).versionName;

				log("Twitter: " + lpparam.packageName + " " + versionCode + " loaded with module version " + moduleVersionCode);

				hookViews(lpparam);
				// new BFAsync().execute(lpparam.classLoader);
			}

		} catch (Throwable t) {
			Log.e(tag, "hook self", t);
		}


	}


	private void hookViews(final LoadPackageParam lpparam) {
		final Class<?> mViewGroup = XposedHelpers.findClass("android.view.ViewGroup", lpparam.classLoader);
		XposedBridge.hookAllMethods(mViewGroup, "addView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// log("afterHookedMethod " + param.method.getName());

				checkAndHideAdViewCards(param, lpparam);
			}
		});
	}

	private void checkAndHideAdViewCards(XC_MethodHook.MethodHookParam param, LoadPackageParam lpparam) {
		try {
			final View view = (View) param.args[0];
			// Log.i(tag, "After addView " + view + " " + view.getClass().getName());


			if (view.toString().contains("promoted")) {
				// always mark promo label
				view.setBackgroundColor(Color.argb(128, 0, 255, 255));
			}

			if (view instanceof ViewGroup) {
				if (view.toString().contains("app:id/row")) {
					// Log.i(tag, "row " + view);
					// subscribe once to handle reusable rows
					view.removeOnLayoutChangeListener(OnLayoutChangeListener);
					view.addOnLayoutChangeListener(OnLayoutChangeListener);

					// handle row on first creation
					final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							HandleView(view, true);
						}
					}, 1);
				}
			}

		} catch (Throwable ignored) {
			Log.e(tag, "err", ignored);
		}
	}


}
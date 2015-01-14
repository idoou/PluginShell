package oms.nzg.shell.pluginshell;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class ShellActivity extends Activity {
	
	Context mContext;
	
	public static DexClassLoader mDexClassLoader;
	public static String mExternalPath;
	
	public ShellActivity() {

		mExternalPath = getExternalApkPath();
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_shell);
		
		
	}
	
	public void loadApk(View v) {
		mContext = getApplicationContext();
		try {
			loadExternalApk(mExternalPath);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private String getExternalApkPath() {
		File dir = Environment.getExternalStorageDirectory();
		File f = new File(dir, "PluginA.apk");
		if (f.exists()) {
			return f.getAbsolutePath();
			
		}
		return "";
	}
	
	private void loadExternalApk(String path) throws Exception {
		mDexClassLoader = new DexClassLoader(path,
				mContext.getCacheDir().getAbsolutePath(),
				null, mContext.getClassLoader());
		Field mMainThread = Activity.class.getDeclaredField("mMainThread");
		mMainThread.setAccessible(true);
		Object mainThread = mMainThread.get(this);
		Class threadClass = mainThread.getClass();
		Field mPackages = threadClass.getDeclaredField("mPackages");
		mPackages.setAccessible(true);
		Map<String, ?> map = (Map<String, ?>) mPackages.get(mainThread);
		WeakReference<?> ref= (WeakReference<?>) map.get(mContext.getPackageName());
		Object apk = ref.get();
		Class apkClass = apk.getClass();
		Field mClassLoader = apkClass.getDeclaredField("mClassLoader");
		mClassLoader.setAccessible(true);
		mClassLoader.set(apk, mDexClassLoader);
		
		Toast.makeText(this, "Load Apk is OK.", Toast.LENGTH_SHORT).show();
	}

	public void startApkActivity(View v) {
		startActivity(new Intent(this, ProxyActivity.class));
	}
}

package oms.nzg.shell.pluginshell;

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
import android.widget.TextView;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class ProxyActivity extends Activity {
	
	public DexClassLoader mDexClassLoader;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		setContentView(tv);
		
		init();
	}
	
	@SuppressLint("NewApi")
	private void init() {

		try {
			loadExternalApk(ShellActivity.mExternalPath);
			loadApkRes(ShellActivity.mExternalPath);
			
			Class cls = mDexClassLoader
					.loadClass("com.example.plugina.MainActivity");

			 startActivity(new Intent(getApplicationContext(), cls));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadExternalApk(String path) throws Exception {
		Context context = getApplicationContext();
		mDexClassLoader = new DexClassLoader(path,
				context.getCacheDir().getAbsolutePath(),
				null, context.getClassLoader());
		Field mMainThread = Activity.class.getDeclaredField("mMainThread");
		mMainThread.setAccessible(true);
		Object mainThread = mMainThread.get(this);
		Class threadClass = mainThread.getClass();
		Field mPackages = threadClass.getDeclaredField("mPackages");
		mPackages.setAccessible(true);
		Map<String, ?> map = (Map<String, ?>) mPackages.get(mainThread);
		WeakReference<?> ref= (WeakReference<?>) map.get(getPackageName());
		Object apk = ref.get();
		Class apkClass = apk.getClass();
		Field mClassLoader = apkClass.getDeclaredField("mClassLoader");
		mClassLoader.setAccessible(true);
		mClassLoader.set(apk, mDexClassLoader);
		
		Toast.makeText(this, "Load Apk is OK.", Toast.LENGTH_SHORT).show();
	}

	AssetManager assetManager;
	Resources resources;
	Theme mTheme;

	public void loadApkRes(String dexPath) {
		System.out.println("dexPath:" + dexPath);
		assetManager = createAssetManager(dexPath);
		resources = createResources(assetManager);
		
		String text = resources.getString(0x7f050003);
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private AssetManager createAssetManager(String dexPath) {
		try {
			AssetManager assetManager = AssetManager.class.newInstance();
			Method addAssetPath = assetManager.getClass().getMethod(
					"addAssetPath", String.class);
			addAssetPath.invoke(assetManager, dexPath);
			return assetManager;
		} catch (Exception e) {
			System.out.println("createAssetManager error:");
			e.printStackTrace();
			return null;
		}

	}

	private Resources createResources(AssetManager assetManager) {
		Resources superRes = getResources();
		Resources resources = new Resources(assetManager,
				superRes.getDisplayMetrics(), superRes.getConfiguration());

		Theme superTheme = getTheme();
		mTheme = resources.newTheme();
		mTheme.setTo(superTheme);

		return resources;
	}
}

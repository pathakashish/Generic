package com.aviras.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;

public class ImageLoader {

	private static final String TAG = "ImageLoader";
	private Context mContext;
	private static File cacheFilePath = null;
	private static boolean writablePathExists = true;
	private static final int DEFAULT_RADIUS = 9;
	private static final int MAX_CACHE_ITEMS_COUNT = 20;
	private static final int DEFAULT_CACHE_TIME_IN_MILLIS = 86400000 * 2; // Two
																			// days
	private long cacheTimiLimit = DEFAULT_CACHE_TIME_IN_MILLIS;
	private HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
	private ArrayList<String> displayedImages = new ArrayList<String>();
	private int loadingResId = R.drawable.loading;
	protected File cacheDir;
	private int requiredImageSize = 100;
	private OnImageLoadedListener onImageLoadedListener = null;
	private int noImageResId = R.drawable.no_image;
	private static Bitmap noImageBitmap = Bitmap.createBitmap(1, 1,
			Config.ALPHA_8);
	private PhotosQueue photosQueue = new PhotosQueue();
	private PhotosLoader photoLoaderThread = new PhotosLoader();
	private boolean roundTopLeftCorner = true;
	private boolean roundTopRightCorner = true;
	private boolean roundBottomLeftCorner = true;
	private boolean roundBottomRightCorner = true;
	private boolean haveRoundedCorders = false;
	private float densityMultiplier;
	private Handler handler;
	private float cornerRadius = DEFAULT_RADIUS;

	public ImageLoader(Context applicationContext) {
		this(applicationContext, null);
	}

	public ImageLoader(Context applicationContext, File cacheDir) {
		mContext = applicationContext.getApplicationContext();
		if (cacheDir == null) {
			this.cacheDir = cacheFilePath == null ? getCacheImagesPath(
					mContext, false) : cacheFilePath;
		} else {
			this.cacheDir = cacheDir;
		}
		if (!this.cacheDir.exists()) {
			if (!this.cacheDir.mkdirs()) {
				this.cacheDir.mkdir();
			}
		}
		handler = new Handler();
		densityMultiplier = mContext.getResources().getDisplayMetrics().density;
		photoLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		SharedPreferences prefs = mContext.getSharedPreferences(
				"pref_imageloader_cache", Context.MODE_PRIVATE);
		cacheTimiLimit = prefs.getLong("time", DEFAULT_CACHE_TIME_IN_MILLIS);
	}

	public void displayImage(int resId, final ImageView imageView) {
		try {
			imageView.setImageResource(resId);
		} catch (Exception e) {
			imageView.setImageResource(noImageResId);
		}
	}

	public void displayImage(String url, final ImageView imageView) {
		displayImage(url, imageView, loadingResId);
	}

	public void displayImage(final String url, final ImageView imageView,
			final int stubId) {
		this.loadingResId = stubId;
		imageView.setTag(url);
		if (cache.containsKey(url)) {
			Bitmap b = cache.get(url);
			imageView.setBackgroundResource(0);
			if (b == noImageBitmap) {
				Log.v(TAG, "imageDimensions: " + imageView.getWidth() + ", "
						+ imageView.getHeight());
				imageView.setImageResource(noImageResId);
			} else {
				imageView.setImageBitmap(b);
				displayedImages.add(url);
			}
			if (onImageLoadedListener != null) {
				onImageLoadedListener.onImageLoaded(imageView, url,
						b != noImageBitmap);
			}
		} else {
			imageView.setBackgroundResource(loadingResId);
			imageView.setImageBitmap(null);
			queuePhoto(url, imageView);

			imageView.post(new Runnable() {

				@Override
				public void run() {
					try {
						AnimationDrawable frameAnimation = (AnimationDrawable) imageView
								.getBackground();
						frameAnimation.start();
					} catch (ClassCastException e) {
						Log.w(TAG, "stub is not AnimationDrawable");
					} catch (NullPointerException e) {
						Log.w(TAG, "Cannot load AnimationDrawable");
					}
				}
			});
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	protected Bitmap getBitmap(String url) {
		Bitmap b = null;
		if (url.startsWith("res://")) {
			try {
				b = decodeResource(url.substring(6));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (url.startsWith("file:///android_asset/")) {
			try {
				b = decodeAsset(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (url.startsWith("content://")) {
			b = decodeContentUri(url);
		} else {
			String filename = String.valueOf(url.hashCode());
			File f = new File(cacheDir, filename);
			b = decodeFile(f);
			if (b == null) {
				try {
					if (url.startsWith("http")) {
						File fTemp = new File(f.getAbsolutePath() + "_tmp");
						fTemp.createNewFile();
						InputStream is = readFromNetwork(new URL(url));
						OutputStream os = new FileOutputStream(f);
						copyStream(is, os);
						os.close();
						fTemp.renameTo(f);
						b = decodeFile(f);
					} else {
						b = decodeFile(url);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if (b == null) {
			b = noImageBitmap;
		}
		return b;
	}

	private Bitmap decodeContentUri(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		Bitmap b = decodeFile(f);
		if (b == null) {
			InputStream is = null;
			try {
				is = mContext.getContentResolver().openInputStream(
						Uri.parse(url));
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, o);
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true) {
					if (width_tmp / 2 < requiredImageSize
							|| height_tmp / 2 < requiredImageSize)
						break;
					width_tmp /= 2;
					height_tmp /= 2;
					scale /= 2;
				}
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				try {
					is.close();
				} catch (Exception e) {
				}
				is = mContext.getContentResolver().openInputStream(
						Uri.parse(url));
				b = BitmapFactory.decodeStream(is, null, o2);
				b = roundBitmapCornersIfRequired(b);
				FileOutputStream os = new FileOutputStream(f);
				b.compress(CompressFormat.PNG, 100, os);
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return b;
	}

	private Bitmap decodeAsset(String assetUrl) throws IOException {
		// Log.v(TAG, "assetUrl: " + assetUrl);
		// Log.v(TAG,
		// "assetName: "
		// + assetUrl.substring(assetUrl.lastIndexOf("/") + 1));
		// AssetManager am = mContext.getAssets();
		// InputStream is = am.open(
		// assetUrl.substring(assetUrl.lastIndexOf("/") + 1),
		// AssetManager.ACCESS_STREAMING);
		// Bitmap bitmap = BitmapFactory.decodeStream(is);
		// return roundBitmapCornersIfRequired(bitmap);
		String filename = String.valueOf(assetUrl.hashCode());
		File f = new File(cacheDir, filename);
		Bitmap b = decodeFile(f);
		if (b == null) {
			InputStream is = null;
			try {
				String temp = assetUrl.substring(0, assetUrl.lastIndexOf("/"));
				int index = temp.lastIndexOf("/") + 1;
				AssetManager am = mContext.getAssets();
				is = am.open(assetUrl.substring(index),
						AssetManager.ACCESS_STREAMING);
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, o);

				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true) {
					if (width_tmp / 2 < requiredImageSize
							|| height_tmp / 2 < requiredImageSize)
						break;
					width_tmp /= 2;
					height_tmp /= 2;
					scale /= 2;
				}
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				try {
					is.close();
				} catch (Exception e) {
				}
				is = am.open(assetUrl.substring(index),
						AssetManager.ACCESS_STREAMING);
				b = BitmapFactory.decodeStream(is, null, o2);
				b = roundBitmapCornersIfRequired(b);
				FileOutputStream os = new FileOutputStream(f);
				b.compress(CompressFormat.PNG, 100, os);
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return b;
	}

	private Drawable decodeResource(int resId) {
		return resId == 0 ? null : mContext.getResources().getDrawable(resId);
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeResource(String f) {
		Bitmap bitmap = ((BitmapDrawable) decodeResource(getResId(f,
				R.drawable.class))).getBitmap();
		return roundBitmapCornersIfRequired(bitmap);
	}

	private Bitmap roundBitmapCornersIfRequired(Bitmap bitmap) {
		if (bitmap != null && haveRoundedCorders) {
			Bitmap temp = ImageHelper.getRoundedCornerBitmap(bitmap,
					(int) (densityMultiplier * cornerRadius),
					roundTopLeftCorner, roundTopRightCorner,
					roundBottomLeftCorner, roundBottomRightCorner);
			bitmap.recycle();
			bitmap = temp;
		}
		return bitmap;
	}

	public int getResId(String variableName, Class<?> c) {

		Field field = null;
		int resId = 0;
		try {
			field = c.getField(variableName);
			try {
				resId = field.getInt(null);
			} catch (Exception e) {
				Log.e(TAG, "No such a field exception");
			}
		} catch (Exception e) {
			Log.e(TAG, "No such a field exception");
		}
		return resId;
	}

	protected Bitmap decodeFile(String f) {
		String filePath = f;
		if (f.startsWith("file://")) {
			filePath = f.substring(6);
		}
		return decodeFile(new File(filePath));
	}

	protected Bitmap decodeFile(File f) {
		if (f.exists()) {
			long lastModified = f.lastModified();
			if (Calendar.getInstance().getTimeInMillis() - lastModified > cacheTimiLimit) {
				f.delete();
				return null;
			}
		}
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < requiredImageSize
						|| height_tmp / 2 < requiredImageSize)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;

			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
					null, o2);
			return roundBitmapCornersIfRequired(bitmap);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public File getCacheImagesPath(Context applicationContext,
			boolean preferSdCard) {
		if (cacheFilePath == null && writablePathExists) {
			if (!preferSdCard) {
				cacheFilePath = getCacheImagesPathPreferLocalStorage(applicationContext);
			} else {
				cacheFilePath = getCacheImagesPathPreferSdCard(applicationContext);
			}
		}
		if (cacheFilePath == null) {
			writablePathExists = false;
		}
		return cacheFilePath;
	}

	private File getCacheImagesPathPreferSdCard(Context applicationContext) {
		File cacheFilePath = null;

		File temp = new File(Environment.getExternalStorageDirectory(),
				applicationContext.getPackageName());
		cacheFilePath = new File(temp, "imagecache");

		try {
			if (!cacheFilePath.mkdirs()) {
				if (!cacheFilePath.mkdir()) {
					cacheFilePath = null;
				}
			}
		} catch (Exception e) {
			cacheFilePath = null;
		}
		if (cacheFilePath == null) {
			temp = new File(applicationContext.getFilesDir(),
					applicationContext.getPackageName());
			cacheFilePath = new File(temp, "imagecache");
			if (!cacheFilePath.mkdirs()) {
				if (!cacheFilePath.mkdir()) {
					cacheFilePath = null;
				}
			}
		}
		return cacheFilePath;
	}

	private File getCacheImagesPathPreferLocalStorage(Context applicationContext) {
		File cacheFilePath = null;
		File temp = new File(applicationContext.getFilesDir(),
				applicationContext.getPackageName());
		cacheFilePath = new File(temp, "imagecache");
		if (!cacheFilePath.exists()) {
			if (!cacheFilePath.exists() && !cacheFilePath.mkdirs()) {
				cacheFilePath = null;
			}
		}
		if (cacheFilePath == null) {
			try {
				temp = new File(Environment.getExternalStorageDirectory(),
						applicationContext.getPackageName());
				cacheFilePath = new File(temp, "imagecache");

				if (!cacheFilePath.mkdirs()) {
					if (!cacheFilePath.mkdir()) {
						cacheFilePath = null;
					}
				}
			} catch (Exception e) {
				cacheFilePath = null;
			}
		}
		return cacheFilePath;
	}

	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		String url;

		public BitmapDisplayer(Bitmap b, ImageView i, String url) {
			bitmap = b;
			imageView = i;
			this.url = url;
		}

		public void run() {
			if (bitmap != null) {
				imageView.setBackgroundResource(0);
				if (bitmap == noImageBitmap) {
					imageView.setImageResource(noImageResId);
					Log.v(TAG, "imageDimensions: " + imageView.getWidth()
							+ ", " + imageView.getHeight());
				} else {
					imageView.setImageBitmap(bitmap);
					displayedImages.add(url);
				}
				if (onImageLoadedListener != null) {
					onImageLoadedListener.onImageLoaded(imageView, url,
							bitmap != noImageBitmap);
				}
			} else {
				imageView.setBackgroundResource(loadingResId);
				imageView.setImageBitmap(null);
				imageView.post(new Runnable() {

					@Override
					public void run() {
						try {
							AnimationDrawable frameAnimation = (AnimationDrawable) imageView
									.getBackground();
							frameAnimation.start();
						} catch (ClassCastException e) {
							Log.w(TAG, "stub is not AnimationDrawable");
						} catch (NullPointerException e) {
							Log.w(TAG, "Cannot load AnimationDrawable");
						}
					}
				});
			}
		}
	}

	class PhotosLoader extends Thread {

		public void run() {
			try {
				while (true) {
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						Bitmap bmp = getBitmap(photoToLoad.url);
						if (cache.size() > getMaxAllowedItems()) {
							clearInMemoryCache();
						}
						if (!cache.containsKey(photoToLoad.url)) {
							cache.put(photoToLoad.url, bmp);
						}
						Object tag = photoToLoad.imageView.getTag();
						if (tag != null
								&& ((String) tag).equals(photoToLoad.url)) {
							BitmapDisplayer bd = new BitmapDisplayer(bmp,
									photoToLoad.imageView, photoToLoad.url);
							handler.post(bd);
							// mActivity.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public void clearInMemoryCache() {
		synchronized (cache) {

			if (cache != null && displayedImages != null
					&& displayedImages.size() > getMaxAllowedItems()) {
				ArrayList<String> removeUrls = new ArrayList<String>();
				for (int i = 0; i < 10; i++) {
					String str = displayedImages.get(i);
					Bitmap bt = cache.get(str);
					if (bt != noImageBitmap) {
						removeUrls.add(str);
						if (bt != null) {
							cache.get(str).recycle();
							cache.remove(str);
						}
					}
				}
				for (String s : removeUrls) {
					displayedImages.remove(s);
				}
				Log.i(TAG, "Cache cleared.");
			}
		}

	}

	private int getMaxAllowedItems() {
		return MAX_CACHE_ITEMS_COUNT;
	}

	public void setRequiredImageSize(int size) {
		this.requiredImageSize = size;
	}

	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		public void Clean(ImageView image) {
			synchronized (photosToLoad) {
				for (int j = 0; j < photosToLoad.size();) {
					if (photosToLoad.get(j).imageView == image)
						photosToLoad.remove(j);
					else
						++j;
				}
			}
		}
	}

	public void clearCache() {
		for (java.util.Map.Entry<String, Bitmap> e : cache.entrySet()) {
			if (e.getValue() != null) {
				e.getValue().recycle();
			}
		}
		cache.clear();
		File[] files = cacheDir.listFiles();
		if (files != null) {
			for (File f : files) {
				try {
					f = new File(cacheDir.getAbsolutePath(), f.getName());
					f.delete();
				} catch (Exception e) {
					Log.e(TAG,
							"Error while deleting the file: "
									+ f.getAbsolutePath());
				}
			}
		}
		cacheDir.delete();
	}

	public synchronized InputStream readFromNetwork(URL url) {
		InputStream is = null;
		try {

			url = new URL(url.toString().replace("+", "%20")
					.replace("https://", "http://"));
			Log.v(TAG, "Downloading >>" + url.toString() + "<<");
			HttpGet httpRequest = new HttpGet(url.toString());
			httpRequest.setHeader("Accept-Encoding", "gzip");
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient
					.execute(httpRequest);
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(
					entity);
			is = bufferedHttpEntity.getContent();
			Header contentEncoding = response
					.getFirstHeader("Content-Encoding");
			if (contentEncoding != null
					&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				is = new GZIPInputStream(is);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Bad ad URL", e);
		} catch (IOException e) {
			Log.e(TAG, "Could not get remote ad image", e);
		} catch (Exception e) {
			Log.e(TAG, "Unknown exception", e);
		}
		return is;
	}

	protected void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 2048;
		try {
			byte[] buffer = new byte[buffer_size];
			int bytesRead = is.read(buffer, 0, buffer_size);
			while (bytesRead > 0) {
				os.write(buffer, 0, bytesRead);
				bytesRead = is.read(buffer, 0, buffer_size);
			}
			os.flush();
		} catch (Exception ex) {
		}
	}

	public void setOnImageLoadedListener(
			OnImageLoadedListener onImageLoadedListener) {
		this.onImageLoadedListener = onImageLoadedListener;
	}

	public interface OnImageLoadedListener {
		void onImageLoaded(ImageView iv, String url,
				boolean dowloadWasSuccessful);
	}

	public long getChacheTimeInMillis() {
		SharedPreferences prefs = mContext.getSharedPreferences(
				"pref_imageloader_cache", Context.MODE_PRIVATE);
		cacheTimiLimit = prefs.getLong("time", DEFAULT_CACHE_TIME_IN_MILLIS);
		return cacheTimiLimit;
	}

	public void setChacheTimeInMillis(long time) {
		if (time <= 1000) {
			throw new RuntimeException(
					"Invalid cache time value. Cache time must at least be 1000.");
		}
		cacheTimiLimit = time;
		SharedPreferences prefs = mContext.getSharedPreferences(
				"pref_imageloader_cache", Context.MODE_PRIVATE);
		prefs.edit().putLong("time", time);
	}

	public static class ImageHelper {
		public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Log.v(TAG, "in getRoundedCornerBitmap");
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);

			return output;
		}

		public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels,
				boolean roundTopLeftCorner, boolean roundTopRightCorner,
				boolean roundBottomLeftCorner, boolean roundBottomRightCorner) {

			Bitmap output = null;
			if (bitmap != null) {
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();
				output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				Canvas canvas = new Canvas(output);
				final int color = 0xff424242;
				final Paint paint = new Paint();
				final Rect rect = new Rect(0, 0, w, h);
				final RectF rectF = new RectF(rect);
				final float roundPx = pixels;
				paint.setAntiAlias(true);
				canvas.drawARGB(0, 0, 0, 0);
				paint.setColor(color);
				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
				if (!roundTopLeftCorner) {
					canvas.drawRect(0, 0, w / 2, h / 2, paint);
				}
				if (!roundTopRightCorner) {
					canvas.drawRect(w / 2, 0, w, h / 2, paint);
				}
				if (!roundBottomLeftCorner) {
					canvas.drawRect(0, h / 2, w / 2, h, paint);
				}
				if (!roundBottomRightCorner) {
					canvas.drawRect(w / 2, h / 2, w, h, paint);
				}
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
				canvas.drawBitmap(bitmap, 0, 0, paint);
			}
			return output;
		}
	}

	public void haveRoundedCorners(boolean b) {
		haveRoundedCorders = b;
		this.roundTopLeftCorner = b;
		this.roundTopRightCorner = b;
		this.roundBottomLeftCorner = b;
		this.roundBottomRightCorner = b;
	}

	public void haveRoundedCorners(boolean roundTopLeftCorner,
			boolean roundTopRightCorner, boolean roundBottomLeftCorner,
			boolean roundBottomRightCorner) {
		haveRoundedCorders = roundTopLeftCorner | roundTopRightCorner
				| roundBottomLeftCorner | roundBottomRightCorner;
		this.roundTopLeftCorner = roundTopLeftCorner;
		this.roundTopRightCorner = roundTopRightCorner;
		this.roundBottomLeftCorner = roundBottomLeftCorner;
		this.roundBottomRightCorner = roundBottomRightCorner;
	}

	public void removeFromCache(String url) {
		synchronized (cache) {

			if (cache != null) {
				ArrayList<String> removeUrls = new ArrayList<String>();
				Bitmap bt = cache.get(url);
				if (bt != noImageBitmap) {
					removeUrls.add(url);
					if (bt != null) {
						cache.get(url).recycle();
						cache.remove(url);
					}
				}
				for (String s : removeUrls) {
					displayedImages.remove(s);
				}
				Log.i(TAG, "Cache cleared.");
			}
		}
	}
}

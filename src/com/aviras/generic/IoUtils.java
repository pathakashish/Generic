package com.aviras.generic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

public class IoUtils {

	private static final String TAG = "Utils";

	private static final int CONNECTION_TIMEOUT_IN_MILLIS = 10000; // 1 seconds
	private static final int REQUEST_TIMEOUT_IN_MILLIS = 300000; // 5 minutes
	public static final int CHUNK_SIZE = 4096;
	private static org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = null;
	private static HttpClient httpClient = null;
	private static ThreadSafeClientConnManager cm;

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 4096;
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

	/**
	 * Returns MD5 hash for given string.
	 * 
	 * @param s
	 *            String for which we want MD5 hash to be generated
	 * @return MD5 hash for s
	 */
	public static String getMd5For(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hexValue = Integer.toHexString(0xFF & messageDigest[i]);
				if (hexValue.length() == 1) {
					hexValue = "0" + hexValue;
				}
				hexString.append(hexValue);
			}
			Log.v(TAG, "MD5 for '" + s + "'  is: " + hexString);
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (hasHttpConnectionBug()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/**
	 * Check if OS version has a http URLConnection bug. See here for more
	 * information:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 * 
	 * @return true if this OS version is affected, false otherwise
	 */
	public static boolean hasHttpConnectionBug() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
	}

	/**
	 * Execute get on given url. It automatically uses the gzip encoding if
	 * server supports it.
	 * 
	 * @param applicationContext
	 * @param url
	 * @return String response we get on executing get with given url.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String get(Context applicationContext, String url)
			throws ClientProtocolException, IOException {
		InputStream is = getStream(applicationContext, url);
		String response = slurp(is);
		if (is != null) {
			is.close();
		}
		Log.v(TAG, "Response:\n" + response);
		return response;
	}

	/**
	 * Execute get on given url. It automatically uses the gzip encoding if
	 * server supports it.
	 * 
	 * @param applicationContext
	 * @param url
	 * @return {@link InputStream} we get on executing get with given url.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static InputStream getStream(Context applicationContext, String url)
			throws ClientProtocolException, IOException {
		Log.v(TAG, "Querying: " + url);
		HttpClient httpClient = getHttpClient(applicationContext);
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept-Encoding", "gzip");
		HttpResponse response = httpClient.execute(httpGet, localContext);
		InputStream is = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null
				&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			is = new GZIPInputStream(is);
		}
		return is;
	}

	/**
	 * Execute post on given url with post parameters specified by
	 * nameValuePairs
	 * 
	 * @param applicationCntext
	 * @param url
	 * @param nameValuePairs
	 *            {@link ArrayList<NameValuePair>} which hold post parameters
	 * @return {@link String} response we get by executing post request with
	 *         given post parameters.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String post(Context applicationCntext, String url,
			List<NameValuePair> nameValuePairs) throws ClientProtocolException,
			IOException {
		return post(applicationCntext, url, nameValuePairs, new String[0]);
	}

	/**
	 * Execute post on given url with post parameters specified by
	 * nameValuePairs
	 * 
	 * @param applicationCntext
	 * @param url
	 * @param nameValuePairs
	 *            {@link ArrayList<NameValuePair>} which hold post parameters
	 * @param fileParameterNames
	 *            {@link String[]} of parameter names in nameValuePairs which
	 *            should be posted as {@link FileBody}
	 * @return {@link String} we get by executing post request with given post
	 *         parameters.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String post(Context applicationCntext, String url,
			List<NameValuePair> nameValuePairs, String[] fileParameterNames)
			throws ClientProtocolException, IOException {
		InputStream is = getStream(applicationCntext, url, nameValuePairs,
				fileParameterNames);
		String response = slurp(is);
		Log.v(TAG, "Response:\n" + response);
		if (is != null) {
			is.close();
		}
		return response;
	}

	/**
	 * Execute post on given url with post parameters specified by
	 * nameValuePairs
	 * 
	 * @param applicationCntext
	 * @param url
	 * @param nameValuePairs
	 *            {@link ArrayList<NameValuePair>} which hold post parameters
	 * @param fileParameterNames
	 *            {@link String[]} of parameter names in nameValuePairs which
	 *            should be posted as {@link FileBody}
	 * @return {@link InputStream} we get by executing post request with given
	 *         post parameters.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static InputStream getStream(Context applicationCntext, String url,
			List<NameValuePair> nameValuePairs) throws ClientProtocolException,
			IOException {
		return getStream(applicationCntext, url, nameValuePairs, new String[0]);
	}

	/**
	 * Execute post on given url with post parameters specified by
	 * nameValuePairs
	 * 
	 * @param applicationCntext
	 * @param url
	 * @param nameValuePairs
	 *            {@link ArrayList<NameValuePair>} which hold post parameters
	 * @param fileParameterNames
	 *            {@link String[]} of parameter names in nameValuePairs which
	 *            should be posted as {@link FileBody}
	 * @return {@link InputStream} we get by executing post request with given
	 *         post parameters.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static InputStream getStream(Context applicationCntext, String url,
			List<NameValuePair> nameValuePairs, String[] fileParameterNames)
			throws ClientProtocolException, IOException {
		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		Log.v(TAG, "Querying: " + url + " with following parameters");
		for (int index = 0; index < nameValuePairs.size(); index++) {
			Log.v(TAG, nameValuePairs.get(index).getName()
					+ "="
					+ (nameValuePairs.get(index).getValue() == null ? ""
							: nameValuePairs.get(index).getValue()));
			boolean isImage = false;
			if (fileParameterNames != null && fileParameterNames.length > 0) {
				for (String p : fileParameterNames) {
					if (nameValuePairs.get(index).getName().equalsIgnoreCase(p)) {
						isImage = true;
						break;
					}
				}
			}
			if (isImage) {
				// We use FileBody to transfer the file data
				entity.addPart(nameValuePairs.get(index).getName(),
						new FileBody(new File(nameValuePairs.get(index)
								.getValue())));
			} else {
				// Normal string data
				entity.addPart(
						nameValuePairs.get(index).getName(),
						new StringBody(
								nameValuePairs.get(index).getValue() == null ? ""
										: nameValuePairs.get(index).getValue()));
			}
		}

		HttpClient httpClient = getHttpClient(applicationCntext);
		HttpContext localContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept-Encoding", "gzip");
		HttpResponse response = httpClient.execute(httpPost, localContext);
		InputStream is = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null
				&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			is = new GZIPInputStream(is);
		}
		return is;
	}

	/**
	 * Check if internet connection is available.
	 * 
	 * @param applicationContext
	 * @return true if internet connection is available false otherwise.
	 */
	public static boolean isOnline(Context applicationContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	/**
	 * Read data in {@link InputStream} and return as string.
	 * 
	 * @param in
	 * @return {@link String} read from {@link InputStream}
	 * @throws IOException
	 */
	public static String slurp(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Forms {@link HttpClient} object and sets connection timeout and response
	 * timeout on it. This {@link HttpClient} object will also support https
	 * connections.
	 * 
	 * @param applicationContext
	 * @return {@link HttpClient}
	 */
	public static HttpClient getHttpClient(Context applicationContext) {
		disableConnectionReuseIfNecessary();
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = CONNECTION_TIMEOUT_IN_MILLIS;
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = REQUEST_TIMEOUT_IN_MILLIS;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		final SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		if (sslSocketFactory == null) {
			sslSocketFactory = createAdditionalCertsSSLSocketFactory(applicationContext);
		}
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		try {

			if (cm == null) {
				cm = new ThreadSafeClientConnManager(httpParameters,
						schemeRegistry);
				cm.closeExpiredConnections();
			}
			if (httpClient == null) {
				httpClient = new DefaultHttpClient(cm, httpParameters);
			} else {
				httpClient.getConnectionManager().closeExpiredConnections();
			}
		} catch (Exception e) {
			e.printStackTrace();
			cm = new ThreadSafeClientConnManager(httpParameters, schemeRegistry);
			cm.closeExpiredConnections();
			httpClient = new DefaultHttpClient(cm, httpParameters);
		}
		return httpClient;
	}

	public static void closeHttpConnection() {
		try {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		} catch (Exception e) {
		}
	}

	public static org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory(
			Context context) {
		// final StringBuilder passwd = new StringBuilder("$ph1nx123");
		try {
			// if (ks == null) {
			// ks = KeyStore.getInstance("BKS");
			//
			// // the bks file we generated above
			// final InputStream in = context.getApplicationContext()
			// .getResources().openRawResource(R.raw.mrassistance);
			// try {
			// ks.load(in, passwd.toString().toCharArray());
			// } finally {
			// in.close();
			// }
			// }
			// return new AdditionalKeyStoresSSLSocketFactory(ks);
			return new AdditionalKeyStoresSSLSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// convert the image URI to the direct file system path of the image file
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };

		// can post image
		Cursor cursor = context.getContentResolver().query(contentUri, proj, // Which
				// columns
				// to
				// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

	public static String getRealPathFromURIWorkAround(Context context,
			Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA,
				MediaColumns.DISPLAY_NAME, "data",
				MediaStore.Images.Media.PICASA_ID };

		// can post image
		Cursor cursor = context.getContentResolver().query(contentUri, proj, // Which
				// columns
				// to
				// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		DatabaseHelper.explainCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);
		cursor.moveToFirst();
		for (String s : proj) {
			if (cursor.getColumnIndexOrThrow(s) <= 0) {
				Log.v(TAG, s + ": NA");
			} else {
				Log.v(TAG, s + " index: " + cursor.getColumnIndexOrThrow(s));
				Log.v(TAG,
						s
								+ ": "
								+ cursor.getString(cursor
										.getColumnIndexOrThrow(s)));
			}
		}
		String path = cursor.getString(column_index);
		cursor.close();
		return "/emmc/DCIM/" + path;
	}
}

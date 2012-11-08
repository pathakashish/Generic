package com.aviras.generic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesHelper {

	private static final String TAG = "AesHelper";
	private static final String DEFAULT_IV = "372adfc77ec8913b";

	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private Cipher cipher;
	private String secretKey;

	public AesHelper(String key) {
		this(key, DEFAULT_IV);
	}

	public AesHelper(String key, String iv) {
		this.secretKey = key;
		ivspec = new IvParameterSpec(iv.getBytes());
		keyspec = new SecretKeySpec(getMd5For(secretKey).getBytes(), "AES");
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public String encrypt(String text) {
		if (text == null || text.length() == 0) {
			return null;
		}

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			encrypted = cipher.doFinal(padString(text).getBytes());
		} catch (Exception e) {
			return null;
		}

		return bytesToHex(encrypted);
	}

	public String decrypt(String code) {
		if (code == null || code.length() == 0) {
			return null;
		}

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(hexToBytes(code));
		} catch (Exception e) {
			return null;
		}
		return new String(decrypted);
	}

	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}

		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16)
				str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
			else
				str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(
						str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}

	private static String padString(String source) {
		char paddingChar = ' ';
		int size = 16;
		int x = source.length() % size;
		int padLength = size - x;

		for (int i = 0; i < padLength; i++) {
			source += paddingChar;
		}
		return source;
	}

	public String getMd5For(String s) {
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
}

package net;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.codec.binary.*;

import walker.Info;

public class Crypto {
	private static final String BaseSecretKey = "uH9JF2cHf6OppaC1";
	//private static final String UnusedSecretKey = "A1dPUcrvur2CRQyl";	
	
	
	
	private static String GetSecretKey() {
		String pw = BaseSecretKey;
		while(pw.length() < 32) pw += "0";
		return pw;
	}
	
	private static String GetSecretKey(Info info) {
		String pw = BaseSecretKey;
		pw += info.LoginId;
		while(pw.length() < 32) pw += "0";
		return pw;
	}
	
	private static String encrypt2Base64(String toEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey().getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, keyspec);
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes()));
	}
	
	private static String encrypt2Base64(String toEncrypt,Info info) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(info).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, keyspec);
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes()));
	}

	public static String Encrypt2Base64NoKey(String toEncrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		return encrypt2Base64(toEncrypt);
	}
	
	public static String Encrypt2Base64WithKey(String toEncrypt, Info info) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return encrypt2Base64(toEncrypt, info);
	}
	
	public static String DecryptBase64NoKey2Str(String cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey().getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return new String(c.doFinal(Base64.decodeBase64(cyphertext)));
	}
	public static String DecryptBase64WithKey2Str(String cyphertext, Info info) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(info).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return new String(c.doFinal(Base64.decodeBase64(cyphertext)));
	}

	private static byte[] decrypt2Bytes(byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey().getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return c.doFinal(ciphertext);
	}
	
	private static byte[] decrypt2Bytes(byte[] ciphertext, Info info) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(info).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return c.doFinal(ciphertext);
	}
	
	public static byte[] DecryptNoKey(byte[] Ciphertext) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		return decrypt2Bytes(Ciphertext);
	}

	public static byte[] DecryptWithKey(byte[] ciphertext, Info info) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return decrypt2Bytes(ciphertext, info);
	}
}

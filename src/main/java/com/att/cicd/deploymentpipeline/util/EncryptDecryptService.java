/*******************************************************************************
 *  * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved. 
 *******************************************************************************/
package com.att.cicd.deploymentpipeline.util;

import java.util.Arrays;

import org.jasypt.util.text.BasicTextEncryptor;

public class EncryptDecryptService {
	
	private static String secret = "pX5R7Ct6EAiZWOknlPeAHlrjAXSjLFCl";

	public static void main(String[] args) throws Exception {
		EncryptDecryptService c = new EncryptDecryptService();
		System.out.println(decrypt("nhWB6fAeL2ukhUfrOcbs0+hK+3BA0cM6"));
//		String cipher = c.encrypt("This is my text");
//		String plainText = c.decrypt(cipher);
//		System.out.println(cipher);
//		System.out.println(plainText);
	}
	
	public static String encrypt(String plainText) throws Exception {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		String cipher = textEncryptor.encrypt(plainText);
		return cipher;
	}
	
	public static String decrypt(String cipherText) throws Exception {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(secret);
		String plainText = textEncryptor.decrypt(cipherText);
		return plainText;
	}
}

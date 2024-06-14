package com.example.dbclpm.utils;

import java.util.Random;

public class RandomCode {
	public static String randomCode() {
		Random random = new Random();
		String code = "";
		for (int i = 0; i < 6; i++) {
			code = code + random.nextInt(10);
		}
		return code;
	}
}

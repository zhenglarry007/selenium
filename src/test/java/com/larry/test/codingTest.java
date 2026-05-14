package com.larry.test;

public class codingTest {
    
    /**
     * 字符串倒序遍历拼接
     */
    public static void testReverseString(String str) {
        String res = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            res += str.charAt(i);
        }
        System.out.println("Input: " + str + " -> Output: " + res);
    }
    
    public static void main(String[] args) {
        System.out.println("--- testReverseString ---");
        testReverseString("abc123");
    }
}

package org.example.tests;
import org.example.utils.PasswordUtil;

public class TestHash {
    public static void main(String[] args) {
        System.out.println(
                org.example.utils.PasswordUtil.hashPassword("15254550")
        );
    }
}
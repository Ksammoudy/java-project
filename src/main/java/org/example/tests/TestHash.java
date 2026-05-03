package org.example.tests;
import org.example.utils.user.PasswordUtil;

public class TestHash {
    public static void main(String[] args) {
        System.out.println(
                org.example.utils.user.PasswordUtil.hashPassword("15254550")
        );
    }
}
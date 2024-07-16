package com.swiftsync.review.util;

import java.awt.*;

public class AdvancedOutput {

    static String currentColor = ConsoleColors.BLACK;

    public static void print(String s){
        System.out.print(s);
    }

    public static void println(String s){
        System.out.println(s);
    }

    public static void print(String s, String color){
        System.out.print(color);
        System.out.print(s);
        System.out.print(currentColor);
    }

    public static void println(String s, String color){
        System.out.print(color);
        System.out.println(s);
        System.out.print(currentColor);
    }

    public static void setColor(String color){
        currentColor = color;
        System.out.print(currentColor);
    }

}

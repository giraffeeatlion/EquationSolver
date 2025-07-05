package com.example;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import Control.GUI_init;

//Controls: zoom using scrollwheel. You can pan only if you hold ctrl and then click and drag.
//functions: sin,cos,tan,sec,csc,cot,floor and the usual polynomial. you can do wtv you want like cos(sin(x*floor(x)))
public class Main {
    public static void main(String[] args) {
        try {
            // Set FlatLaf Look and Feel
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        new GUI_init();
    }
}
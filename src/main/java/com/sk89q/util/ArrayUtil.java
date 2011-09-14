package com.sk89q.util;

public class ArrayUtil {
    
    public static String[] removePortionOfArray(String[] array, int from, int to, String replace) {
        String[] newArray = new String[from + array.length - to - (replace == null ? 1 : 0)];
        System.arraycopy(array, 0, newArray, 0, from);
        if (replace != null) newArray[from] = replace;
        System.arraycopy(array, to + 1, newArray, from + (replace == null ? 0 : 1),
                    array.length - to - 1);
        return newArray;
    }

    public static char[] removePortionOfArray(char[] array, int from, int to, Character replace) {
        char[] newArray = new char[from + array.length - to - (replace == null ? 1 : 0)];
        System.arraycopy(array, 0, newArray, 0, from);
        if (replace != null) newArray[from] = replace;
        System.arraycopy(array, to + 1, newArray, from + (replace == null ? 0 : 1),
                    array.length - to - 1);
        return newArray;
    }
}

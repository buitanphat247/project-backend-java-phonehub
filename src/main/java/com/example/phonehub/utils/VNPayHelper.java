package com.example.phonehub.utils;

public final class VNPayHelper {

    private VNPayHelper() {}

    public static long toVNPAmount(long vnd) { return vnd * 100; }

    public static long fromVNPAmount(long vnpAmount) { return vnpAmount / 100; }
}


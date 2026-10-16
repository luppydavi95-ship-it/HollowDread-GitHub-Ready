package com.hollowdread.client;

/** Estado de sanidade no cliente (sem referenciar classes exclusivas de cliente). */
public final class ClientState {
    public static float sanity = 100f;
    public static float smoothSanity = 100f;

    private ClientState() {}

    public static void setSanity(float s) { sanity = s; }

    public static void reset() {
        sanity = 100f;
        smoothSanity = 100f;
    }
}

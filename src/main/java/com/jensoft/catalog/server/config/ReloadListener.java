package com.jensoft.catalog.server.config;

public interface ReloadListener {
    /**
     * Called when reloading of the container is requested.
     */
    void onReload();
}


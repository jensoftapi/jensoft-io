package com.jensoft.catalog.server.config;


public interface Scanner {
    public void scan(ScannerListener sl) throws ScannerException;
}


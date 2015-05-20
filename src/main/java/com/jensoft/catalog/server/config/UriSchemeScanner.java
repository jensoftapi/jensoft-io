package com.jensoft.catalog.server.config;


import java.net.URI;
import java.util.Set;


public interface UriSchemeScanner {

    /**
     * Get the set of supported URI schemes.
     *
     * @return the supported URI schemes.
     */
    Set<String> getSchemes();

    /**
     * Perform a scan and report resources to a scanning listener.
     *
     * @param u the URI to scan for resources.
     * @param sl the scanning listener to report entries.
     * @throws ScannerException if an error occured while scanning.
     */
    void scan(URI u, ScannerListener sl) throws ScannerException;
}

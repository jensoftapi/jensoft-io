package com.jensoft.catalog.server.config;


import java.io.IOException;
import java.io.InputStream;


public interface ScannerListener {

    /**
     * Accept a scanned resource.
     * <p>
     * This method will be invoked by a {@link Scanner} to ascertain if the
     * listener accepts the resource for processing. If acceptable then
     * the {@link Scanner} will then invoke the
     * {@link #onProcess(java.lang.String, java.io.InputStream) } method.
     *
     * @param name the resource name.
     * @return true if the resource is accepted for processing, otherwise false.
     */
    boolean onAccept(String name);

    /**
     * Process a scanned resource.
     * <p>
     * This method will be invoked after the listener has accepted the
     * resource.
     *
     * @param name the resource name.
     * @param in the input stream of the resource
     * @throws IOException if an error occurs when processing the resource.
     */
    void onProcess(String name, InputStream in) throws IOException;
}
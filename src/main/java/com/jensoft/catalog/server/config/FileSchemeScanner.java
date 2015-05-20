package com.jensoft.catalog.server.config;



import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Set;


public class FileSchemeScanner implements UriSchemeScanner {

    public Set<String> getSchemes() {
        return Collections.singleton("file");
    }

    // UriSchemeScanner
    
    public void scan(final URI u, final ScannerListener cfl) {
        final File f = new File(u.getPath());
        if (f.isDirectory()) {
            scanDirectory(f, cfl);
        } else {
            // TODO log
        }
    }

    private void scanDirectory(final File root, final ScannerListener cfl) {
        for (final File child : root.listFiles()) {
            if (child.isDirectory()) {
                scanDirectory(child, cfl);
            } else if (cfl.onAccept(child.getName())) {
                try {
                    new Closing(new BufferedInputStream(new FileInputStream(child))).f(new Closing.Closure() {

                        public void f(final InputStream in) throws IOException {
                            cfl.onProcess(child.getName(), in);
                        }
                    });
                } catch (IOException ex) {
                    throw new ScannerException("IO error when scanning jar file " + child, ex);
                }
            }
        }
    }
}

package com.jensoft.catalog.server.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


/**
 * A utility class that scans entries in jar files.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class JarFileScanner {

    /**
     * Scan entries in a jar file.
     * <p>
     * An entry will be reported to the scanning listener if the entry is a
     * child of the parent path.
     *
     * @param f the jar file.
     * @param parent the parent path.
     * @param sl the scanning lister to report jar entries.
     * @throws IOException if an error occurred scanning the jar entries
     */
    public static void scan(final File f, final String parent, final ScannerListener sl) throws IOException {
        new Closing(new FileInputStream(f)).f(new Closing.Closure() {

            public void f(final InputStream in) throws IOException {
                scan(in, parent, sl);
            }
        });
    }

    /**
     * Scan entries in a jar file.
     * <p>
     * An entry will be reported to the scanning listener if the entry is a
     * child of the parent path.
     * 
     * @param in the jar file as an input stream.
     * @param parent the parent path.
     * @param sl the scanning lister to report jar entries.
     * @throws IOException if an error occurred scanning the jar entries
     */
    public static void scan(final InputStream in, final String parent, final ScannerListener sl) throws IOException {
        JarInputStream jarIn = null;
        try {
            jarIn = new JarInputStream(in);
            JarEntry e = jarIn.getNextJarEntry();
            while (e != null) {
            	//System.out.println("entry : "+e.getName());
                if (!e.isDirectory() && e.getName().startsWith(parent) && sl.onAccept(e.getName())) {
                    //System.out.println("process :"+e.getName());
                	sl.onProcess(e.getName(), jarIn);
                }
                jarIn.closeEntry();
                e = jarIn.getNextJarEntry();
            }
        } finally {
            if (jarIn != null) {
                jarIn.close();
            }
        }
    }
}

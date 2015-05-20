package com.jensoft.catalog.server.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A "jar" and "zip" scheme URI scanner that recursively jar files.
 * Jar entries are reported to a {@link ScannerListener}.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author gerard.davison@oracle.com
 */
public class JarZipSchemeScanner implements UriSchemeScanner {

    public Set<String> getSchemes() {
        return new HashSet<String>(Arrays.asList("jar", "zip"));
    }

    public void scan(final URI u, final ScannerListener cfl) {
        final String ssp = u.getRawSchemeSpecificPart();
        final String jarUrlString = ssp.substring(0, ssp.lastIndexOf('!'));
        final String parent = ssp.substring(ssp.lastIndexOf('!') + 2);
        try {
            closing(jarUrlString).f(new Closing.Closure() {

                public void f(final InputStream in) throws IOException {
                    JarFileScanner.scan(in, parent, cfl);
                }
            });
        } catch (IOException ex) {
            throw new ScannerException("IO error when scanning jar " + u, ex);
        }
    }

    /**
     * Obtain a {@link Closing} of the jar file.
     * <p>
     * For most platforms the format for the zip or jar follows the form of
     * the <a href="http://docs.sun.com/source/819-0913/author/jar.html#jarprotocol"jar protcol.</a></p>
     * <ul>
     *   <li><code>jar:file:///tmp/fishfingers.zip!/example.txt</code></li>
     *   <li><code>zip:http://www.example.com/fishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p>
     * On versions of the WebLogic application server a proprietary format is
     * supported of the following form, which assumes a zip file located on
     * the local file system:
     * </p>
     * <ul>
     *   <li><code>zip:/tmp/fishfingers.zip!/example.txt</code></li>
     *   <li><code>zip:d:/tempfishfingers.zip!/example.txt</code></li>
     * </ul>
     * <p>
     * This method will first attempt to create a {@link Closing} as follows:
     * <pre>
     *   new Closing(new URL(jarUrlString).openStream());
     * </pre>
     * if that fails with a {@link MalformedURLException} then the method will
     * attempt to create a {@link Closing} instance as follows:
     * <pre>
     *  return new Closing(new FileInputStream(
     *      UriComponent.decode(jarUrlString, UriComponent.Type.PATH)));
     * </pre>
     *
     * @param jarUrlString the raw scheme specific part of a URI minus the jar
     *        entry
     * @return a {@link Closing}.
     * @throws IOException if there is an error opening the stream.
     */
    protected Closing closing(String jarUrlString) throws IOException {
        try {
            return new Closing(new URL(jarUrlString).openStream());
        } catch (MalformedURLException ex) {
           // return new Closing(new FileInputStream(
             //       UriComponent.decode(jarUrlString, UriComponent.Type.PATH)));
        	return null;
        }
    }
}


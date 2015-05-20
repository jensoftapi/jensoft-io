package com.jensoft.catalog.server.config;



import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class PackageNamesScanner implements Scanner {

	
    private final String[] packages;
    private final ClassLoader classloader;
    private final Map<String, UriSchemeScanner> scanners;

    /**
     * Scan from a set of packages using the context class loader.
     *
     * @param packages an array of package names.
     */
    public PackageNamesScanner(final String[] packages) {
        this(ReflectionHelper.getContextClassLoader(), packages);
    }

    /**
     * Scan from a set of packages using declared class loader.
     *
     * @param classloader the class loader to load classes from.
     * @param packages an array of package names.
     */
    public PackageNamesScanner(final ClassLoader classloader, final String[] packages) {
        this.packages = packages;
        this.classloader = classloader;

        this.scanners = new HashMap<String, UriSchemeScanner>();
        add(new JarZipSchemeScanner());
        add(new FileSchemeScanner());
    }

    private void add(final UriSchemeScanner ss) {
        for (final String s : ss.getSchemes()) {
            scanners.put(s.toLowerCase(), ss);
        }
    }

    @Override
    public void scan(final ScannerListener cfl) {
        for (final String p : packages) {
            try {
                final Enumeration<URL> urls = ResourcesProvider.getInstance(). getResources(p.replace('.', '/'), classloader);
                       
                while (urls.hasMoreElements()) {
                    try {
                    	URI uri = toURI(urls.nextElement());
                    	System.out.println("scan URI : "+uri);
                        scan(uri, cfl);
                    } catch (URISyntaxException ex) {
                        throw new ScannerException("Error when converting a URL to a URI", ex);
                    }
                }
            } catch (IOException ex) {
                throw new ScannerException("IO error when package scanning jar", ex);
            }
        }
    }

    /**
     * Find resources with a given name and class loader.
     */
    public static abstract class ResourcesProvider {

        private static volatile ResourcesProvider provider;

        private static ResourcesProvider getInstance() {
            // Double-check idiom for lazy initialization
            ResourcesProvider result = provider;

            if (result == null) { // first check without locking
                synchronized (ResourcesProvider.class) {
                    result = provider;
                    if (result == null) { // second check with locking
                        provider = result = new ResourcesProvider() {

                            @Override
                            public Enumeration<URL> getResources(String name, ClassLoader cl)
                                    throws IOException {
                                return cl.getResources(name);
                            }
                        };

                    }
                }

            }
            return result;
        }

        private static void setInstance(ResourcesProvider provider) throws SecurityException {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                ReflectPermission rp = new ReflectPermission("suppressAccessChecks");
                security.checkPermission(rp);
            }
            synchronized (ResourcesProvider.class) {
                ResourcesProvider.provider = provider;
            }
        }

        /**
         * Find all resources with the given name using a class loader.
         *
         * @param cl the class loader use to find the resources
         * @param name the resource name
         * @return An enumeration of URL objects for the resource.
         *         If no resources could be found, the enumeration will be empty.
         *         Resources that the class loader doesn't have access to will
         *         not be in the enumeration.
         * @throws IOException if I/O errors occur
         */
        public abstract Enumeration<URL> getResources(String name, ClassLoader cl) throws IOException;
    }

    /**
     * Set the {@link ResourcesProvider} implementation to find resources.
     * <p>
     * This method should be invoked before any package scanning is performed
     * otherwise the functionality method will be utilized.
     *
     * @param provider the resources provider.
     * @throws SecurityException if the resources provider cannot be set.
     */
    public static void setResourcesProvider(ResourcesProvider provider) throws SecurityException {
        ResourcesProvider.setInstance(provider);
    }


    private void scan(final URI u, final ScannerListener cfl) {
        final UriSchemeScanner ss = scanners.get(u.getScheme().toLowerCase());
        if (ss != null) {
            ss.scan(u, cfl);
        } else {
            throw new ScannerException("The URI scheme " + u.getScheme() +
                    " of the URI " + u +
                    " is not supported. Package scanning deployment is not" +
                    " supported for such URIs." +
                    "\nTry using a different deployment mechanism such as" +
                    " explicitly declaring root resource and provider classes" +
                    " using an extension of javax.ws.rs.core.Application");
        }
    }

    private URI toURI(URL url) throws URISyntaxException {
       
            return url.toURI();
       
    }

}


package com.jensoft.catalog.server.config;



import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jensoft.core.catalog.nature.JenSoftView;
import org.jensoft.core.x2d.binding.X2DBinding;



public class ScanningCatalogConfig extends DefaultCatalogConfig implements ReloadListener {
    private static final Logger LOGGER = 
            Logger.getLogger(ScanningCatalogConfig.class.getName());

    private Scanner scanner;

    private final Set<Class<?>> cachedClasses = new HashSet<Class<?>>();

    /**
     * Initialize and scan for root resource and provider classes
     * using a scanner.
     *
     * @param scanner the scanner.
     */
    public void init(final Scanner scanner) {
        this.scanner = scanner;

        final AnnotationScannerListener asl = new PathProviderScannerListener();
        scanner.scan(asl);

        getClasses().addAll(asl.getAnnotatedClasses());
        
        if (!getClasses().isEmpty()) {
            final Set<Class> inflaterClasses = get(X2DBinding.class);
            if (inflaterClasses.isEmpty()) {
                LOGGER.info("No Inflater classes found.");
            } else {
                logClasses("Inflater classes found:", inflaterClasses);
            }

          
            
            final Set<Class> viewClasses = get(JenSoftView.class);
            if (viewClasses.isEmpty()) {
                LOGGER.log(Level.INFO, "No View classes found.");
            } else {
                logClasses("View2D classes found:", viewClasses);
            }

        }
        
  

        cachedClasses.clear();
        cachedClasses.addAll(getClasses());
    }

    /**
     * Perform a new search for resource classes and provider classes.
     * <p/>
     * Deprecated, use onReload instead.
     */
    @Deprecated
    public void reload() {
        onReload();
    }
    

    /**
     * Perform a new search for resource classes and provider classes.
     */
    @Override
    public void onReload() {
        Set<Class<?>> classesToRemove = new HashSet<Class<?>>();
        Set<Class<?>> classesToAdd = new HashSet<Class<?>>();

        for(Class c : getClasses())
            if(!cachedClasses.contains(c))
                classesToAdd.add(c);

        for(Class c : cachedClasses)
            if(!getClasses().contains(c))
                classesToRemove.add(c);

        getClasses().clear();

        init(scanner);

        getClasses().addAll(classesToAdd);
        getClasses().removeAll(classesToRemove);
    }

    private Set<Class> get(Class<? extends Annotation> ac) {
        Set<Class> s = new HashSet<Class>();
        for (Class c : getClasses())
            if (c.isAnnotationPresent(ac))
                s.add(c);
        return s;
    }

    private void logClasses(String s, Set<Class> classes) {
        final StringBuilder b = new StringBuilder();
        b.append(s);
        for (Class c : classes)
            b.append('\n').append("  ").append(c);

        LOGGER.log(Level.INFO, b.toString());
    }
}
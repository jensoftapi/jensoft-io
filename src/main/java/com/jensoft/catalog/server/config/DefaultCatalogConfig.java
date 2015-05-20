package com.jensoft.catalog.server.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


public class DefaultCatalogConfig extends CatalogApplication {

	private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

	private final Set<Object> singletons = new LinkedHashSet<Object>(1);

	/**
     */
	public DefaultCatalogConfig() {
		this((Set<Class<?>>) null);
	}

	/**
	 * @param classes
	 *            the initial set of root resource classes and provider classes
	 */
	public DefaultCatalogConfig(Class<?>... classes) {
		this(new LinkedHashSet<Class<?>>(Arrays.asList(classes)));
	}

	/**
	 * @param classes
	 *            the initial set of root resource classes and provider classes
	 */
	public DefaultCatalogConfig(Set<Class<?>> classes) {
		if (null != classes) {
			this.classes.addAll(classes);
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}

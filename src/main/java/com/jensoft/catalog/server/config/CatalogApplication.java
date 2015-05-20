package com.jensoft.catalog.server.config;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;

import org.jensoft.core.catalog.nature.Captcha;
import org.jensoft.core.catalog.nature.Catalog;
import org.jensoft.core.catalog.nature.Intro;
import org.jensoft.core.catalog.nature.JenSoftDashboard;
import org.jensoft.core.catalog.nature.JenSoftView;
import org.jensoft.core.catalog.nature.New;
import org.jensoft.core.catalog.nature.UIUnit;
import org.jensoft.core.catalog.nature.X2DView;
import org.jensoft.core.x2d.binding.AbstractX2DPluginInflater;
import org.jensoft.core.x2d.binding.X2DBinding;

public abstract class CatalogApplication extends Application {
	private static final Logger logger = Logger.getLogger(CatalogApplication.class.getName());

	public static final String COMMON_DELIMITERS = " ,;\n";

	public Set<Class<?>> getX2DInflaterClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();

		for (Class<?> c : getClasses()) {
			if (isX2DInflaterClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Set<Class<?>> getX2DDeflaterClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();

		for (Class<?> c : getClasses()) {
			if (isX2DInflaterClass(c))
				s.add(c);
		}
		return s;
	}

	public Set<Class<?>> getView2DClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {
			if (isView2DClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Set<Class<?>> getNewClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {
			if (isNewClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Class<?> getIntro() {
		for (Class<?> c : getClasses()) {
			if (isIntro(c))
				return c;
		}
		return null;
	}
	
	public Class<?> getCatalog() {
		for (Class<?> c : getClasses()) {
			if (isCatalog(c))
				return c;
		}
		return null;
	}
	
	public Set<Class<?>> getDashboardClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {
			if (isDashboardClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Set<Class<?>> getUnitClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {
			
			if (isUnitClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Set<Class<?>> getCaptchaClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {			
			if (isCaptchaClass(c))
				s.add(c);
		}
		return s;
	}
	
	public Set<Class<?>> getX2DClasses() {
		Set<Class<?>> s = new LinkedHashSet<Class<?>>();
		for (Class<?> c : getClasses()) {			
			if (isX2DClass(c))
				s.add(c);
		}
		return s;
	}

	public Set<Object> getX2DInflaterSingletons() {
		Set<Object> s = new LinkedHashSet<Object>();
		for (Object o : getSingletons()) {
			if (isX2DInflaterClass(o.getClass()))
				s.add(o);
		}
		return s;
	}

	public Set<Object> getView2DSingletons() {
		Set<Object> s = new LinkedHashSet<Object>();
		for (Object o : getSingletons()) {
			if (isView2DClass(o.getClass()))
				s.add(o);
		}
		return s;
	}

	public static boolean isX2DInflaterClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(X2DBinding.class) && c.isAssignableFrom(AbstractX2DPluginInflater.class);
	}

	public static boolean isX2DDeflaterClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(X2DBinding.class) && c.isAssignableFrom(AbstractX2DPluginInflater.class);
	}

	public static boolean isView2DClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(JenSoftView.class);
	}
	
	public static boolean isNewClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(New.class);
	}
	public static boolean isCaptchaClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(Captcha.class);
	}
	public static boolean isX2DClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(X2DView.class);
	}
	public static boolean isIntro(Class<?> c) {
		return c != null && c.isAnnotationPresent(Intro.class);
	}
	
	public static boolean isCatalog(Class<?> c) {
		return c != null && c.isAnnotationPresent(Catalog.class);
	}
	
	public static boolean isDashboardClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(JenSoftDashboard.class);
	}
	
	public static boolean isUnitClass(Class<?> c) {
		return c != null && c.isAnnotationPresent(UIUnit.class);
	}

	public static String[] getElements(String[] elements) {
		return getElements(elements, ";");
	}

	public static String[] getElements(String[] elements, String delimiters) {
		List<String> es = new LinkedList<String>();
		for (String element : elements) {
			if (element == null)
				continue;
			element = element.trim();
			if (element.length() == 0)
				continue;
			for (String subElement : getElements(element, delimiters)) {
				if (subElement == null || subElement.length() == 0)
					continue;
				es.add(subElement);
			}
		}
		return es.toArray(new String[es.size()]);
	}

	private static String[] getElements(String elements, String delimiters) {
		String regex = "[";
		for (char c : delimiters.toCharArray())
			regex += Pattern.quote(String.valueOf(c));
		regex += "]";

		String[] es = elements.split(regex);
		for (int i = 0; i < es.length; i++) {
			es[i] = es[i].trim();
		}
		return es;
	}
}

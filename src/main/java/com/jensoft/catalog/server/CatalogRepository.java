package com.jensoft.catalog.server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.jensoft.catalog.server.config.PackagesCatalogConfig;
import org.jensoft.core.catalog.nature.Captcha;
import org.jensoft.core.catalog.nature.Catalog;
import org.jensoft.core.catalog.nature.JenSoftView;
import org.jensoft.core.catalog.nature.UIUnit;
import org.jensoft.core.plugin.AbstractPlugin;
import org.jensoft.core.plugin.copyright.CopyrightPlugin;
import org.jensoft.core.projection.Projection;
import org.jensoft.core.view.View;

public class CatalogRepository implements ServletContextListener {

	static Logger logger = Logger.getLogger(CatalogRepository.class);

	private static PackagesCatalogConfig resourceConfig;
	
	private static String webStartTemplate;
	private static File webStartDir;	
	private static String contextPath;
	private static String server;
	private static String jnlpContextPath;
	private static String resourcesContextPath;
	private static String imageContextPath;
	
	private static ConcurrentHashMap<Integer, Class<?>> viewsCache = new ConcurrentHashMap<Integer, Class<?>>();
	private static ConcurrentHashMap<Integer, Class<?>> dashboardsCache = new ConcurrentHashMap<Integer, Class<?>>();
	private static ConcurrentHashMap<Integer, Class<?>> unitsCache = new ConcurrentHashMap<Integer, Class<?>>();
	private static ConcurrentHashMap<Integer, Class<?>> newCache = new ConcurrentHashMap<Integer, Class<?>>();
	private static ConcurrentHashMap<Integer, Class<?>> captchaCache = new ConcurrentHashMap<Integer, Class<?>>();
	private static ConcurrentHashMap<Integer, Class<?>> x2dCache = new ConcurrentHashMap<Integer, Class<?>>();

	// map className - class
	private static ConcurrentHashMap<String, Class<?>> classByClassName = new ConcurrentHashMap<String, Class<?>>();

	// map unit - views 'list like' in map
	private static ConcurrentHashMap<UIUnit, ConcurrentHashMap<Integer, Class<?>>> viewsByUnit = new ConcurrentHashMap<UIUnit, ConcurrentHashMap<Integer, Class<?>>>();

	// map view class - unit
	private static ConcurrentHashMap<Class<?>, UIUnit> unitByClass = new ConcurrentHashMap<Class<?>, UIUnit>();

	// default unit
	private static UIUnit defaultUnit;
	
	
	//CAPTCHA
	private static ConcurrentHashMap<Integer, CaptchaItem> captchaItems = new ConcurrentHashMap<Integer, CaptchaItem>();
	private static ConcurrentHashMap<Integer, BufferedImage> captchItemsImage = new ConcurrentHashMap<Integer, BufferedImage>();
	private static int captchaItemsSize;

	
	public static PackagesCatalogConfig getResourceConfig() {
		return resourceConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("Jensoft Catalog BOOT : JenSoft Catalog Closing");
	}

	/**
	 * generate the given stream as a generic source file
	 * 
	 * @param content
	 *            source content
	 * @param file
	 *            source file
	 * @throws BuildException
	 */
	private void generateSource(String content, File file) throws Exception {
		FileOutputStream fop = null;
		try {

			fop = new FileOutputStream(file);

			if (!file.exists()) {
				file.createNewFile();
			}

			byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (IOException e) {
			throw new Exception("generate jnlp failed with error :" + e.getMessage());
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * create frame jnlp
	 * 
	 */
	private void createWebStartViewFrameUI(Class demoClass) throws Exception {

		String jnlpDemoFileName = webStartDir.getPath() + File.separator + demoClass.getSimpleName() + ".jnlp";
		// log("generate JNLP : " + jnlpDemoFileName);

		logger.debug("Generate FRAME UI JNLP: " + jnlpDemoFileName);
		String frameUIJNLP = new String(webStartTemplate);
		frameUIJNLP = frameUIJNLP.replace("${jnlp-codebase}", jnlpContextPath);
		frameUIJNLP = frameUIJNLP.replace("${jnlp-href}", demoClass.getSimpleName() + ".jnlp");
		//frameUIJNLP = frameUIJNLP.replace("${frame-ui-class}", demoClass.getPackage().getName() + ".ui.frame." + demoClass.getSimpleName() + "FrameUI");
		
		frameUIJNLP = frameUIJNLP.replace("${demo-splash}", imageContextPath+"/catalog.1.0.png");
		frameUIJNLP = frameUIJNLP.replace("${frame-ui}", "org.jensoft.core.catalog.ui.ViewFrameUI");
		frameUIJNLP = frameUIJNLP.replace("${ui-class}", demoClass.getName());

		StringBuffer jarsRefBuffer = new StringBuffer();
		
		//add core
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + "jensoft-core-"+getCatalogCoreVersion()+".jar" + "\"" + "/>" + "\n");
		//if core source is need for web start?!
		//jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + "jensoft-core-"+getCatalogCoreVersion()+".jar" + "\"" + "/>" + "\n");
		
		//add catalog
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + getCatalogArtifact()+"-"+getCatalogVersion()+".jar" + "\"" + "/>" + "\n");
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + getCatalogArtifact()+"-"+getCatalogVersion()+"-sources.jar" + "\"" + "/>" + "\n");
		
		

		frameUIJNLP = frameUIJNLP.replace("${ui-jar-resources}", jarsRefBuffer.toString());

		generateSource(frameUIJNLP, new File(jnlpDemoFileName));

	}
	
	/**
	 * create frame jnlp
	 * 
	 */
	private void createWebStartDashboardFrameUI(Class demoClass) throws Exception {

		String jnlpDemoFileName = webStartDir.getPath() + File.separator + demoClass.getSimpleName() + ".jnlp";
		// log("generate JNLP : " + jnlpDemoFileName);

		logger.debug("Generate FRAME UI JNLP: " + jnlpDemoFileName);
		String frameUIJNLP = new String(webStartTemplate);
		frameUIJNLP = frameUIJNLP.replace("${jnlp-codebase}", jnlpContextPath);
		frameUIJNLP = frameUIJNLP.replace("${jnlp-href}", demoClass.getSimpleName() + ".jnlp");
		
		frameUIJNLP = frameUIJNLP.replace("${frame-ui}", "org.jensoft.core.catalog.ui.DashboardFrameUI");
		frameUIJNLP = frameUIJNLP.replace("${ui-class}", demoClass.getName());

		StringBuffer jarsRefBuffer = new StringBuffer();
		
		//add core
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + "jensoft-core-"+getCatalogCoreVersion()+".jar" + "\"" + "/>" + "\n");
		//if core source is need for web start?!
		//jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + "jensoft-core-"+getCatalogCoreVersion()+".jar" + "\"" + "/>" + "\n");
		
		//add catalog
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + getCatalogArtifact()+"-"+getCatalogVersion()+".jar" + "\"" + "/>" + "\n");
		jarsRefBuffer.append("\t" + "\t" + "<jar href=\"" + resourcesContextPath + "/" + getCatalogArtifact()+"-"+getCatalogVersion()+"-sources.jar" + "\"" + "/>" + "\n");
				
		frameUIJNLP = frameUIJNLP.replace("${ui-jar-resources}", jarsRefBuffer.toString());

		generateSource(frameUIJNLP, new File(jnlpDemoFileName));

	}

	private void generateWebStart(ServletContextEvent ctx) {
		logger.info("Generate web start files...");
		String pathToWebInf = ctx.getServletContext().getRealPath("/WEB-INF");
		File webInfDir = new File(pathToWebInf);
		if (webInfDir.exists()) {
			String pathToPublicContext = webInfDir.getParent();
			String publicJNLPDir = pathToPublicContext + File.separator + "webstart";
			webStartDir = new File(publicJNLPDir);
			webStartDir.mkdirs();
			InputStream jnlpTemplate = this.getClass().getClassLoader().getResourceAsStream("frame-ui.jnlp");
			ResourceBundle res = ResourceBundle.getBundle("catalog");
			server = res.getString("catalog.server");
			
			//contextPath = ctx.getServletContext().getContextPath();
			contextPath = "";
			
			logger.info("Catalog context path is  : " + contextPath);
			
			
			
			jnlpContextPath = server + contextPath + "/" + "webstart";
			resourcesContextPath = server + contextPath + "/" + "resources";
			imageContextPath = server + contextPath + "/" + "img";
			logger.info("Register web start context path : " + jnlpContextPath);
			logger.info("Register resources context path : " + resourcesContextPath);

			logger.info("server info : " + ctx.getServletContext().getServerInfo());
			URL url = null;
			try {
				url = ctx.getServletContext().getResource("/");
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			logger.info("server resource on / : " + url);
			if (jnlpTemplate != null) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(jnlpTemplate));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();
					webStartTemplate = sb.toString();
					logger.info("web start template successfully loaded : " + "\n" + webStartTemplate);
				} catch (IOException e) {
					logger.info("web start template loading error", e);
				}
			} else {
				logger.error("web start template can not be loaded : frame-ui.jnlp");
			}
			if (webStartDir.exists() && webStartTemplate != null) {
				logger.info("Generate View Web Starts");
				Set<Class<?>> viewClasses = resourceConfig.getView2DClasses();
				try {
					for (Class viewClass : viewClasses) {
						createWebStartViewFrameUI(viewClass);
					}
				} catch (Exception e) {
					logger.error("jnlp generation failed.", e);
				}

				logger.info("Generate Dashboard Web Starts");
				Set<Class<?>> dashboardClasses = resourceConfig.getDashboardClasses();
				try {
					for (Class dashboardClass : dashboardClasses) {
						createWebStartDashboardFrameUI(dashboardClass);
						logger.info("Generate Dashboard Web Start : " + dashboardClass.getName());
					}
				} catch (Exception e) {
					logger.error("jnlp generation failed.", e);
				}
			}

		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent ctx) {
		logger.info("Catalog Server Starting...");
		
		ServletContext sc = ctx.getServletContext();

		Enumeration<String> paramsName = sc.getInitParameterNames();
		logger.info("catalog params names : " + paramsName);

		
		ServletContext c = ctx.getServletContext();
		String catalogPackage = c.getInitParameter("repository-scan");
		
		if(catalogPackage == null){
			logger.error("Catalog Generic server : repository property 'repository-scan' should be supplied");
		}
		
		logger.info("Catalog Generic Server, repository scan from root : "+catalogPackage);
		resourceConfig = new PackagesCatalogConfig(catalogPackage);
		initCatalog();
		
		generateWebStart(ctx);

		createCache();
	}


	public static ConcurrentHashMap<Integer, Class<?>> getViewsCache() {
		return viewsCache;
	}

	public static ConcurrentHashMap<Integer, Class<?>> getDashboardsCache() {
		return dashboardsCache;
	}

	public static ConcurrentHashMap<String, Class<?>> getViewsByClassNameMap() {
		return classByClassName;
	}

	public static ConcurrentHashMap<Integer, Class<?>> getUnitsCache() {
		return unitsCache;
	}
	
	public static ConcurrentHashMap<UIUnit, ConcurrentHashMap<Integer, Class<?>>> getUnitViewsMapCache() {
		return viewsByUnit;
	}

	public static ConcurrentHashMap<Class<?>, UIUnit> getViewUnitMapCache() {
		return unitByClass;
	}

	public static UIUnit getDefaultUnit() {
		return defaultUnit;
	}
	
	
	public static Class catalog;

	private void createCache() {
		
//		logger.info("create View see resources cache");
//		for (Class<?> vc : resourceConfig.getView2DClasses()) {
//			JenSoftView viewAnnot = vc.getAnnotation(JenSoftView.class);
//			Class[] seeResource = viewAnnot.see();
//			if(seeResource != null && seeResource.length > 0){
//				logger.info("Found see resource for view : "+vc.getName());
//				try {
//					Class.forName(vc.getName());
//					logger.error("see resource loaded successfully.");
//				} catch (ClassNotFoundException e) {
//					logger.error("see resource can not be loaded");
//				}
//			}
//		}
		

		// views
		logger.info("create View cache");
		Set<Class<?>> viewClasses = resourceConfig.getView2DClasses();
		int countView = 0;
		for (Class<?> vc : viewClasses) {
			viewsCache.put(new Integer(countView++), vc);
		}

		// dashboards
		logger.info("create Dashboard cache");
		int countDashboard = 0;
		Set<Class<?>> dashboardClasses = resourceConfig.getDashboardClasses();
		for (Class<?> dc : dashboardClasses) {
			dashboardsCache.put(new Integer(countDashboard++), dc);
		}

		// units
		logger.info("create Unit cache");
		Set<Class<?>> unitClasses = resourceConfig.getUnitClasses();
		int countUnit = 0;
		for (Class<?> uc : unitClasses) {
			unitsCache.put(new Integer(countUnit++), uc);
		}
		

		logger.info("create Default Unit instance");
		defaultUnit = new UIUnit() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public String name() {
				return "Default";
			}

			@Override
			public String desc() {
				return "Default unit";
			}
		};

		viewsByUnit.put(defaultUnit, new ConcurrentHashMap<Integer, Class<?>>());

		// mapping one to many : unit - children views or dashboards by package rooting
		for (Map.Entry entry : unitsCache.entrySet()) {
			Class<?> uc = (Class<?>) entry.getValue();
			UIUnit unit = (UIUnit) uc.getPackage().getAnnotation(UIUnit.class);
			logger.info("create cache for unit : " + unit.name());
			ConcurrentHashMap<Integer, Class<?>> unitViewsMap = new ConcurrentHashMap<Integer, Class<?>>();
			viewsByUnit.put(unit, unitViewsMap);

			for (Map.Entry<Integer, Class<?>> viewsClass : viewsCache.entrySet()) {
				Class<?> vc = (Class<?>) viewsClass.getValue();
				if (vc.getPackage().getName().contains(uc.getPackage().getName())) {
					logger.info("attach cache view : " + vc.getName());
					unitViewsMap.put(unitViewsMap.size(), vc);
				}
			}
			for (Map.Entry<Integer, Class<?>> dashboardClass : dashboardsCache.entrySet()) {
				Class<?> dc = (Class<?>) dashboardClass.getValue();
				if (dc.getPackage().getName().contains(uc.getPackage().getName())) {
					logger.info("attach cache dashboard : " + dc.getName());
					unitViewsMap.put(unitViewsMap.size(), dc);
				}
			}
		}

		// mapping one to one : viewClass/Unit and  viewName/viewClass
		for (Map.Entry<Integer, Class<?>> viewEntry : viewsCache.entrySet()) {
			Class<?> viewClass = viewEntry.getValue();
			classByClassName.put(viewClass.getName(), viewClass);
			
			JenSoftView viewAnnot = viewClass.getAnnotation(JenSoftView.class);
			Class[] seeResource = viewAnnot.see();
			if(seeResource != null && seeResource.length > 0){
				logger.info("Found see resource for view : "+viewClass.getName());
				for (int i = 0; i < seeResource.length; i++) {
					Class<?> see = seeResource[i];
					logger.info(" add see resource to reference ");
					classByClassName.put(see.getName(), see);
				}
				
			}else{
				logger.info("Empty see resource for view : "+viewClass.getName());
			}
			
			// each view , check unit
			for (Map.Entry<Integer, Class<?>> unitEntry : unitsCache.entrySet()) {
				Class<?> unitClass = unitEntry.getValue();
				if (viewClass.getPackage().getName().contains(unitClass.getPackage().getName())) {
					UIUnit unit = (UIUnit) unitClass.getPackage().getAnnotation(UIUnit.class);
					unitByClass.put(viewClass, unit);
				}
			}
			//if null->default unit
			if (unitByClass.get(viewClass) == null) {
				unitByClass.put(viewClass, defaultUnit);
				viewsByUnit.get(defaultUnit).put(viewsByUnit.get(defaultUnit).size(), viewClass);
			}
			logger.info("cache view/unit : " + viewClass.getName() + "->" + unitByClass.get(viewClass).name());
		}

		for (Map.Entry<Integer, Class<?>> dashboardEntry : dashboardsCache.entrySet()) {
			Class<?> dashboardClass = dashboardEntry.getValue();
			classByClassName.put(dashboardClass.getName(), dashboardClass);
			// each dashboard , check unit
			for (Map.Entry<Integer, Class<?>> unitEntry : unitsCache.entrySet()) {
				Class<?> unitClass = (Class<?>) unitEntry.getValue();
				if (dashboardClass.getPackage().getName().contains(unitClass.getPackage().getName())) {
					UIUnit unit = (UIUnit) unitClass.getPackage().getAnnotation(UIUnit.class);
					unitByClass.put(dashboardClass, unit);
				}
			}
			if (unitByClass.get(dashboardClass) == null) {
				unitByClass.put(dashboardClass, defaultUnit);
				viewsByUnit.get(defaultUnit).put(viewsByUnit.size(), dashboardClass);
			}
			logger.info("cache dashboard/unit : " + dashboardClass.getName() + "->" + unitByClass.get(dashboardClass).name());
		}

		for (Map.Entry<UIUnit, ConcurrentHashMap<Integer, Class<?>>> entry : viewsByUnit.entrySet()) {
			logger.info("Unit : " + entry.getKey().name() + " contains cache views :" + entry.getValue().size());
			ConcurrentHashMap<Integer, Class<?>> map = viewsByUnit.get(entry.getKey());
			for (Class<?> classDef : map.values()) {
				logger.info(entry.getKey().name()+ " unit contains following class :" + classDef.getName());
			}
		}
		
		catalog = resourceConfig.getCatalog();

	}
	

	
	public static Class<?> getCatalog(){
		return catalog;
	}
	
	private static String catalogName;
	private static String catalogGroup;
	private static String catalogArtifact;
	private static String catalogVersion;
	private static String catalogCoreVersion;
	
	
	

	/**
	 * @return the catalogName
	 */
	public static String getCatalogName() {
		return catalogName;
	}

	/**
	 * @return the catalogGroup
	 */
	public static String getCatalogGroup() {
		return catalogGroup;
	}

	/**
	 * @return the catalogArtifact
	 */
	public static String getCatalogArtifact() {
		return catalogArtifact;
	}

	/**
	 * @return the catalogVersion
	 */
	public static String getCatalogVersion() {
		return catalogVersion;
	}

	/**
	 * @return the catalogCoreVersion
	 */
	public static String getCatalogCoreVersion() {
		return catalogCoreVersion;
	}
	
	
	
	

	private void initCatalog() {
		
		
		Catalog catalog = (Catalog) getResourceConfig().getCatalog().getPackage().getAnnotation(Catalog.class);
		
		catalogName = catalog.name();
		catalogGroup = catalog.group();
		catalogArtifact = catalog.artifact();
		catalogVersion = catalog.version();
		catalogCoreVersion=catalog.core();
		
		logger.info("catalog name : " + catalog.version());
		logger.info("catalog group : " + catalog.group());
		logger.info("catalog artifact : " + catalog.artifact());
		logger.info("catalog version : " + catalog.version());
		logger.info("catalog core version : " + catalog.core());
		
		ResourceBundle res = ResourceBundle.getBundle("catalog");
		logger.info("server :" + res.getString("catalog.server"));

	}

}

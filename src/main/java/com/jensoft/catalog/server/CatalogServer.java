package com.jensoft.catalog.server;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import org.jensoft.core.catalog.nature.Captcha;
import org.jensoft.core.catalog.nature.Catalog;
import org.jensoft.core.catalog.nature.JenSoftDashboard;
import org.jensoft.core.catalog.nature.JenSoftView;
import org.jensoft.core.catalog.nature.New;
import org.jensoft.core.catalog.nature.UIUnit;
import org.jensoft.core.catalog.nature.X2DView;
import org.jensoft.core.plugin.AbstractPlugin;
import org.jensoft.core.plugin.copyright.CopyrightPlugin;
import org.jensoft.core.projection.Projection;
import org.jensoft.core.view.View;
import org.jensoft.core.view.background.ViewBackgroundPainter;
import org.jensoft.core.x2d.X2D;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/")
public class CatalogServer {

	static Logger logger = Logger.getLogger(CatalogServer.class);
	
	
	public CatalogServer () {
		logger.info("Create Catalog server");
	}
	
	@GET
	@Produces(value=MediaType.TEXT_PLAIN)
	@Path("{mode}/size/search/{search: .*}")
	public Response size(@PathParam("mode") String mode,@DefaultValue("") @PathParam("search") String searchSequence){
		if(mode.equals("views")){
			//logger.info("count catalog views with search sequence : "+searchSequence.trim());
			Collection<Class<?>> viewsClasses = lookUpViews(searchSequence.trim());		
			//logger.info("count catalog views :"+viewsClasses.size());
			return Response.status(200).type(MediaType.TEXT_PLAIN).entity(Integer.toString(viewsClasses.size())).build();
		}else if(mode.equals("units")){
			//logger.info("count catalog units with search sequence : "+searchSequence.trim());
			Collection<Class<?>> unitsClasses = lookUpUnits(searchSequence.trim());		
			//logger.info("count catalog units :"+unitsClasses.size());
			return Response.status(200).type(MediaType.TEXT_PLAIN).entity(Integer.toString(unitsClasses.size())).build();
		}
		return null;
	}
	
	@GET
	@Produces(value=MediaType.TEXT_PLAIN)
	@Path("/count/all")
	public Response countAll(){
		return Response.status(200).type(MediaType.TEXT_PLAIN).entity(Integer.toString((CatalogRepository.getViewsCache().size()+CatalogRepository.getDashboardsCache().size()))).build();
	}
	
	
	
	/**
	 * return count of views and dash boards of the given unit that matches given pattern pattern
	 * @param unit
	 * @param searchSequence
	 * @return count views and dash boards filtered
	 */
	@GET
	@Produces(value=MediaType.TEXT_PLAIN)
	@Path("unit/{unit}/size/search/{search: .*}")
	public Response sizeUnit(@PathParam("unit") String unit,@DefaultValue("") @PathParam("search") String searchSequence){
		//logger.info("count catalog unit "+unit+" views with search sequence : "+searchSequence.trim());
		Collection<Class<?>> viewsClasses = lookUpViews(searchSequence.trim());
		int count=0;
		for(Class<?> c : viewsClasses){
			UIUnit uiunit = CatalogRepository.getViewUnitMapCache().get(c);
			if(uiunit != null && uiunit.name().equals(unit)){
				count++;
			}
		}
		//logger.info("count catalog unit "+unit+" views for unit :"+count);
		return Response.status(200).type(MediaType.TEXT_PLAIN).entity(Integer.toString(count)).build();
	}
	 
	private Map<String, ConcurrentHashMap<Dimension,BufferedImage>> viewcache = new ConcurrentHashMap<String, ConcurrentHashMap<Dimension,BufferedImage>>();
	
	/**
	 * get image from cache for the given dimension. if image does not exist, create image, put in cache
	 * @param viewClassName
	 * @param width
	 * @param height
	 * @return cache image
	 */
	private BufferedImage getViewImage(String viewClassName,int width, int height){
		if(viewcache.get(viewClassName) != null){
			BufferedImage image = viewcache.get(viewClassName).get(new Dimension(width,height));
			if(image != null){
				//logger.info("get cache image "+viewClassName);
				return image;
			}else{
				BufferedImage im = createImage(viewClassName, width, height);
				if(im != null){
					viewcache.get(viewClassName).put(new Dimension(width,height), im);
					logger.info("create and cache image for view "+viewClassName);
					return im;
				}else{
					logger.error("ERROR0, NULL IMAGE  : create and cache image for view "+viewClassName);
					return null;
				}
				
			}
		}else{
			ConcurrentHashMap<Dimension, BufferedImage> sizecache = new ConcurrentHashMap<Dimension, BufferedImage>();
			viewcache.put(viewClassName, sizecache);
			
			logger.info("create and cache image for view "+viewClassName);
			BufferedImage im = createImage(viewClassName, width, height);
			if(im != null){
				sizecache.put(new Dimension(width,height), im);
				return im;
			}else{
				logger.error("ERROR1, NULL IMAGE : create and cache image for view "+viewClassName);
				return null;
			}
			
			
			
		}
	}
	
	/**
	 * create image for the given view
	 * @param viewClassName
	 * @param width
	 * @param height
	 * @return buffered image
	 */
	private BufferedImage createImage(String viewClassName,int width,int height){
		
		Class<?> view = CatalogRepository.getViewsByClassNameMap().get(viewClassName);
		if(view != null){
			try {
				JenSoftView viewAnnot = view.getAnnotation(JenSoftView.class);
				View v = (View)view.newInstance();
				for(Projection w : v.getProjections()){
					List<AbstractPlugin> plugins = w.getPluginRegistry();
					List<AbstractPlugin> toremove = new ArrayList<AbstractPlugin>();
					for (AbstractPlugin p : plugins) {
						if(p.getClass().getName().equals(CopyrightPlugin.class.getName())){
							toremove.add(p);
						}
					}
					plugins.removeAll(toremove);
				}
				if(v.getBackgroundPainter() == null){
					v.setBackgroundPainter((ViewBackgroundPainter)viewAnnot.background().newInstance());
				}
					
				BufferedImage viewImage = v.createViewEmitter().emitAsBufferedImage(width,height);
				return viewImage;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}
	
	
	/**
	 * GET view image with default image size
	 * @param viewClass
	 * @return image
	 */
	@GET
	@Path("/icon/48")
	public Response icon48(){
		Class<?> catalogRoot = CatalogRepository.getCatalog();
		ClassLoader cloader = catalogRoot.getClassLoader();
		String packageName = catalogRoot.getPackage().getName();
		//String inputSourceName = packageName.replace('.', '/') + "/" + "icon-48.png";
		//logger.info("try to get icon from name : "+inputSourceName);
		//InputStream is = cloader.getResourceAsStream(inputSourceName);
		
		InputStream is = cloader.getResourceAsStream("icon-48.png");
		Image image;
		try {
			image = ImageIO.read(is);
			return Response.status(200).type("image/png").entity(image).build();
		} catch (Throwable e) {
			logger.warn("error get icon 48 ",e);
			return Response.status(404).build();
		}
	}
	
	/**
	 * GET view image with default image size
	 * @param viewClass
	 * @return image
	 */
	@GET
	@Path("view/{view}/image")
	public Response image(@PathParam("view") String viewClass){
		Class<?> view = CatalogRepository.getViewsByClassNameMap().get(viewClass);
		if(view != null){
			logger.info("image for class : "+view.getName());
			JenSoftView viewAnnot = view.getAnnotation(JenSoftView.class);
			BufferedImage viewImage = getViewImage(viewClass, viewAnnot.dimension().width(), viewAnnot.dimension().height());
			return Response.status(200).type("image/png").entity(viewImage).build();
		}
		return Response.status(404).build();
	}
	
	/**
	 * GET view image for given dimension
	 * @param viewClass
	 * @param width
	 * @param height
	 * @return image
	 */
	@GET
	@Path("view/{view}/image/{width}/{height}")
	public Response sizedImage(@PathParam("view") String viewClass,@PathParam("width") int width,@PathParam("height") int height){
		Class<?> view = CatalogRepository.getViewsByClassNameMap().get(viewClass);
		if(view != null){
			BufferedImage viewImage = getViewImage(viewClass, width, height);
			return Response.status(200).type("image/png").entity(viewImage).build();
		}
		return Response.status(404).build();
	}
	
	
	/**
	 *  look up in repository cache views (views and dash boards) that matches with the pattern search sequence
	 * @param searchSequence
	 * @return views and dashboards
	 */
	private Collection<Class<?>> lookUpViews(String searchSequence){
		Collection<Class<?>> viewsClasses = new ArrayList<Class<?>>();
		
		if(searchSequence != null && searchSequence.trim().length() > 0){
			String[] sequences = searchSequence.split(" ");
			for (int i = 0; i < sequences.length; i++) {
				for(Class<?> c : CatalogRepository.getViewsCache().values()){
					JenSoftView viewAnnot = c.getAnnotation(JenSoftView.class);
					String viewDesc="";
					if(viewAnnot != null && viewAnnot.description() != null){
						viewDesc = viewAnnot.description();
					}					
					UIUnit unit = CatalogRepository.getViewUnitMapCache().get(c);
					String unitName = "";
					String unitDesc = "";
					if(unit != null && unit.name() != null){
						unitName = unit.name();
					}
					if(unit != null && unit.desc() != null){
						unitDesc = unit.desc();
					}
					
					if(viewDesc.contains(sequences[i]) || 
							c.getName().contains(sequences[i]) || 
							c.getName().toLowerCase().contains(sequences[i]) ||
							unitName.contains(sequences[i]) ||
							unitDesc.contains(sequences[i])||
							
							viewDesc.contains(sequences[i].toLowerCase()) || 
							c.getName().contains(sequences[i].toLowerCase()) || 
							c.getName().toLowerCase().contains(sequences[i].toLowerCase()) ||
							unitName.contains(sequences[i].toLowerCase()) ||
							unitDesc.contains(sequences[i].toLowerCase())
							
							)
					{
						if(!viewsClasses.contains(c)){
							viewsClasses.add(c);
						}
					}
				}
				for(Class<?> c : CatalogRepository.getDashboardsCache().values()){
					JenSoftDashboard viewAnnot = c.getAnnotation(JenSoftDashboard.class);
					String viewDesc="";
					if(viewAnnot != null && viewAnnot.description() != null){
						viewDesc = viewAnnot.description();
					}					
					
					if(viewDesc.contains(sequences[i]) || c.getName().contains(sequences[i]) || c.getName().toLowerCase().contains(sequences[i])){
						if(!viewsClasses.contains(c)){
							viewsClasses.add(c);
						}
					}
				}
			}
			
		}else{
			viewsClasses.addAll(CatalogRepository.getViewsCache().values());
			viewsClasses.addAll(CatalogRepository.getDashboardsCache().values());
		}
		
		//filter ignore
		List<Class<?>> remove = new ArrayList<Class<?>>();
		for (Class<?> class1 : viewsClasses) {
			JenSoftView viewAnnot = class1.getAnnotation(JenSoftView.class);
			if(viewAnnot != null && viewAnnot.ignore()==true){
				remove.add(class1);
			}
		}
		
		for (Class<?> class1 : remove) {
			viewsClasses.remove(class1);
		}
		return viewsClasses;
	}
	
	
	/**
	 * look up in repository cache unit that matches with the pattern search sequence
	 * @param searchSequence
	 * @return units
	 */
	private Collection<Class<?>> lookUpUnits(String searchSequence){
		Collection<Class<?>> unitClasses = new ArrayList<Class<?>>();
		if(searchSequence != null && searchSequence.trim().length() > 0){
			String[] sequences = searchSequence.split(" ");
			for (int i = 0; i < sequences.length; i++) {
				for(Class<?> c : CatalogRepository.getUnitsCache().values()){
					UIUnit unit = (UIUnit)c.getPackage().getAnnotation(UIUnit.class);
					String unitName = unit.name();
					String unitDesc = unit.desc();
					if(unitName.contains(sequences[i]) || unitName.toLowerCase().contains(sequences[i]) || unitDesc.contains(sequences[i]) || unitDesc.toLowerCase().contains(sequences[i])){
						if(!unitClasses.contains(c)){
							unitClasses.add(c);
						}
					}
				}
			}
			
		}else{
			unitClasses.addAll(CatalogRepository.getUnitsCache().values());
		}
		return unitClasses;
	}
	
	
	/**
	 * GET view source
	 * @param viewClass
	 * @return
	 */
	@GET
	@Path("view/{view}/source")
	public Response source(@PathParam("view") String viewClass){
		
		Class<?> view = CatalogRepository.getViewsByClassNameMap().get(viewClass);
		logger.info("try to get view for class : "+viewClass);
		ClassLoader cloader = view.getClassLoader();
		String packageName = view.getPackage().getName();
		String inputSourceName = packageName.replace('.', '/') + "/" + view.getSimpleName() + ".java";
		
		logger.info("try to get java source file : "+inputSourceName);
		
		InputStream is = cloader.getResourceAsStream(inputSourceName);
		InputStreamReader isreader = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isreader);
		String line = null;
		StringBuffer buffer = new StringBuffer();
		try {
			while ((line = in.readLine()) != null) {
				//logger.info("read line : "+line);
				buffer.append(line+"\n");
			}
		} catch (IOException e) {
			return Response.ok().build();
		}	
		finally{
			if(isreader != null){
				try {
					isreader.close();
				} catch (IOException e) {
					logger.error("can not close input stream reader",e);
				}
			}
			
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					logger.error("can not close input stream",e);
				}
				
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					logger.error("can not close buffered reader",e);
				}
				
			}
		}
		String src = buffer.toString();
		return Response.ok().type(MediaType.TEXT_PLAIN).entity(src).build();
	}
	
	/**
	 * GET view source
	 * @param viewClass
	 * @return
	 */
	@GET
	@Path("view/{view}/x2d")
	public Response x2dsource(@PathParam("view") String viewClass){
		try {
		Class<?> view = CatalogRepository.getViewsByClassNameMap().get(viewClass);
		
		View v = (View)view.newInstance();
		X2D x2d = new X2D();
		
			x2d.registerView(v);
			
			Document doc = x2d.getX2dDocument();

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();

			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");

			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			
			
			
			StringReader sr = new StringReader(sw.toString());
			BufferedReader in = new BufferedReader(sr);
			
			String line = null;
			StringBuffer buffer = new StringBuffer();
			try {
				while ((line = in.readLine()) != null) {
					//logger.info("read line : "+line);
					buffer.append(line+"\n");
				}
			} catch (IOException e) {
				return Response.status(404).build();
			}	
			finally{
				if(in != null){
					try {
						in.close();
					} catch (IOException e) {
						logger.error("can not close input stream reader",e);
					}
				}
				
				
			}
			String src = buffer.toString();
			return Response.ok().type(MediaType.TEXT_PLAIN).entity(src).build();
			
		} catch (Throwable e) {
			e.printStackTrace();
			return Response.status(404).build();
		}

		
	}
	
	/**
	 * GET view source
	 * @param viewClass
	 * @return
	 */
	@GET
	@Path("/note/release")
	public Response releaseNote(){
		
		Class<?> catalogRoot = CatalogRepository.getCatalog();
		ClassLoader cloader = catalogRoot.getClassLoader();
		String packageName = catalogRoot.getPackage().getName();
		//String inputSourceName = packageName.replace('.', '/') + "/" + "RELEASE-NOTES.txt";
		//InputStream is = cloader.getResourceAsStream(inputSourceName);
		
		InputStream is = cloader.getResourceAsStream("RELEASE-NOTES.txt");
		InputStreamReader isreader = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isreader);
		String line = null;
		StringBuffer buffer = new StringBuffer();
		try {
			while ((line = in.readLine()) != null) {
				//logger.info("read line : "+line);
				buffer.append(line+"\n");
			}
		} catch (IOException e) {
			return Response.ok().build();
		}	
		finally{
			if(isreader != null){
				try {
					isreader.close();
				} catch (IOException e) {
					logger.error("can not close input stream reader",e);
				}
			}
			
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					logger.error("can not close input stream",e);
				}
				
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					logger.error("can not close buffered reader",e);
				}
				
			}
		}
		String src = buffer.toString();
		return Response.ok().type(MediaType.TEXT_PLAIN).entity(src).build();
	}
	
	
	/**
	 * GET view source
	 * @param viewClass
	 * @return
	 */
	@GET
	@Path("/news")
	public Response news(@PathParam("view") String viewClass){
		ArrayList<NewItem> news = new ArrayList<NewItem>();
		for(Class<?> c : CatalogRepository.getNewCache().values()){
			New newFromClass = c.getAnnotation(New.class);
			New newFromClassPackage = c.getPackage().getAnnotation(New.class);
			
			if(newFromClass != null){
				NewItem i = new NewItem();
				i.setComment(newFromClass.comment());
				news.add(i);
			}
//			if(newFromClassPackage != null){
//				NewItem i = new NewItem();
//				i.setComment(newFromClassPackage.comment());
//				news.add(i);
//			}
		}
		
		NewBook book = new NewBook();
		book.setPage(news);
		return Response.ok().entity(book).build();
		
	}
	
	/**
	 * GET view source
	 * @param viewClass
	 * @return
	 */
	@GET
	@Path("/note/license")
	public Response licenseNote(){
		
		Class<?> catalogRoot = CatalogRepository.getCatalog();
		ClassLoader cloader = catalogRoot.getClassLoader();
		String packageName = catalogRoot.getPackage().getName();
		//String inputSourceName = packageName.replace('.', '/') + "/" + "LICENSE.txt";
		//InputStream is = cloader.getResourceAsStream(inputSourceName);
		InputStream is = cloader.getResourceAsStream("LICENSE.txt");
		InputStreamReader isreader = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isreader);
		String line = null;
		StringBuffer buffer = new StringBuffer();
		try {
			while ((line = in.readLine()) != null) {
				//logger.info("read line : "+line);
				buffer.append(line+"\n");
			}
		} catch (IOException e) {
			return Response.ok().build();
		}	
		finally{
			if(isreader != null){
				try {
					isreader.close();
				} catch (IOException e) {
					logger.error("can not close input stream reader",e);
				}
			}
			
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					logger.error("can not close input stream",e);
				}
				
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					logger.error("can not close buffered reader",e);
				}
				
			}
		}
		String src = buffer.toString();
		return Response.ok().type(MediaType.TEXT_PLAIN).entity(src).build();
	}
	
	/**
	 * GET dash board definition with child views
	 * @param dashboardClass
	 * @return dash board def
	 */
	@GET
	@Path("dashboard/{class}")
	public Response dashboard(@PathParam("class") String dashboardClass){
		logger.info("get dashboard def for: "+dashboardClass);
		for(Class<?> c : CatalogRepository.getDashboardsCache().values()){
		
				if(c.getName().equals(dashboardClass)){
					JenSoftDashboard viewAnnot = c.getAnnotation(JenSoftDashboard.class);
					ArrayList<ViewItem> catalogPart = new ArrayList<ViewItem>();
					Class[] dashboardViews = viewAnnot.views();
					for (int i = 0; i < dashboardViews.length; i++) {
						logger.info("get dashboard view part : "+dashboardViews[i]);
						for(Class<?> cv : CatalogRepository.getViewsCache().values()){
							if(cv.getName().equals(dashboardViews[i].getName())){
								ViewItem item = new ViewItem();
								item.setClassName(cv.getSimpleName());
								item.setClassPackage(cv.getPackage().getName());
								JenSoftDashboard dashboardAnnot = cv.getAnnotation(JenSoftDashboard.class);
								
								UIUnit uiunit = CatalogRepository.getViewUnitMapCache().get(cv);
								if(uiunit != null){
									item.setUnit(uiunit.name());
								}else{
									item.setUnit("Default");
								}
								
								
								if(viewAnnot != null && viewAnnot.description()!=null){
									item.setDescription(viewAnnot.description());
									item.setType("view");
								}
								if(dashboardAnnot != null && dashboardAnnot.description()!=null){
									item.setDescription(dashboardAnnot.description());
									item.setType("dashboard");
								}
								catalogPart.add(item);
							}
						}
					}
					
					ViewsPage catalogPage = new ViewsPage();
					catalogPage.setPage(catalogPart);
					return Response.status(200).entity(catalogPage).build();
					
				}
		}
		logger.info("fetch catalog bad request");
		return Response.status(404).build();
	}
	
	
	
	/**
	 * get catalog views/unit page filtered for search pattern 
	 * @param mode
	 * @param page
	 * @param viewsPerPage
	 * @param searchSequence
	 * @return catalog page
	 */
	@GET
	@Path("{mode}/page/{page}/viewsperpage/{viewsperpage}/search/{search: .*}")
	public Response page(@PathParam("mode") String mode,@PathParam("page") int page, @PathParam("viewsperpage") int viewsPerPage,@DefaultValue("") @PathParam("search") String searchSequence){
		logger.info("fetch catalog with mode: "+mode);
		if("views".equals(mode)){
			return getViewPage(page, viewsPerPage, searchSequence);
		}else if("units".equals(mode)){
			return getUnitPage(page, viewsPerPage, searchSequence);
		}
		logger.info("fetch catalog bad request");
		return Response.status(404).build();
	}
	
	
	/**
	 *  get catalog unit views page filtered for search pattern 
	 * @param unit
	 * @param page
	 * @param viewsPerPage
	 * @param searchSequence
	 * @return catalog unit views page
	 */
	@GET
	@Path("unit/{unit}/page/{page}/viewsperpage/{viewsperpage}/search/{search: .*}")
	public Response pageUnitView(@PathParam("unit") String unit,@PathParam("page") int page, @PathParam("viewsperpage") int viewsPerPage,@DefaultValue("") @PathParam("search") String searchSequence){
		logger.info("fetch catalog unit views with unit: "+unit);
		
		return getUnitViewsPage(unit,page, viewsPerPage, searchSequence);		
	}
	
	/**
	 * GET unit views
	 * @param unit
	 * @return
	 */
	@GET
	@Path("intro")
	public Response intro(){
		logger.info("fetch intro ");
		Class<?> intro = CatalogRepository.getIntro();
		if(intro != null){
		ViewItem introItem = new ViewItem();
		introItem.setClassName(intro.getSimpleName());
		introItem.setClassPackage(intro.getPackage().getName());
		return Response.ok(introItem).build();
		}else{
			return Response.status(404).build();
		}
	}
	
	
	/**
	 * GET unit views
	 * @param unit
	 * @return
	 */
	@GET
	@Path("unit/{unit}")
	public Response unit(@PathParam("unit") String unit){
		logger.info("fetch catalog unit : "+unit);
		UIUnit selectedUnit = null;
		if(unit != null && unit.equals("Default")){
			logger.info("fetch Default unit");
			selectedUnit = CatalogRepository.getDefaultUnit();
		}else{
			Collection<Class<?>> units = CatalogRepository.getUnitsCache().values();
			for(Class c : units){
				
					UIUnit u = (UIUnit)c.getPackage().getAnnotation(UIUnit.class);
					if(u.name().equals(unit)){						
						selectedUnit = u;
					}
				
			}
		}
		
		if(selectedUnit != null){
			UnitItem item = new UnitItem();
			
			String unitName = selectedUnit.name();
			String unitDesc = selectedUnit.desc();				
			
			int count= 0;
			ConcurrentHashMap<Integer,Class<?>> unitViews = CatalogRepository.getUnitViewsMapCache().get(selectedUnit);
			if(unitViews != null){				
				count = unitViews.size();
			}
			
			item.setUnitViews(count);
			item.setUnitName(unitName);
			item.setUnitInfo(unitDesc);	
			
			ArrayList<UnitItem> items = new ArrayList<UnitItem>();
			items.add(item);
			
			UnitsPage catalogPage = new UnitsPage();
			catalogPage.setPage(items);
			logger.info("found catalog unit : "+unit+" with count views : "+count);
			return Response.status(200).entity(catalogPage).build();
		}

		logger.info("NOT found catalog unit : "+unit+" send 404 response");
		return Response.status(404).build();	
	}
	
	private Response getUnitViewsPage(String unit,int page,int viewsPerPage,String searchSequence){
		logger.info("fetch catalog unit :"+ unit +" views page : "+page+" with search sequence : "+searchSequence);
		Collection<Class<?>> viewsClasses = lookUpViews(searchSequence);
		List<Class<?>> unitViewsClasses = new ArrayList<Class<?>>();
		for(Class<?> c : viewsClasses){
			UIUnit uiunit = CatalogRepository.getViewUnitMapCache().get(c);
			if(uiunit != null && uiunit.name().equals(unit)){
				unitViewsClasses.add(c);
			}
		}
		logger.info("found views for the  unit : "+unit+" --> "+unitViewsClasses.size());
		int startIndex = page*viewsPerPage;
		if(startIndex < unitViewsClasses.size()){			
			ArrayList<ViewItem> catalogPart = new ArrayList<ViewItem>();
			int count = 0;
			for(Class<?> c : unitViewsClasses){
				if(count >= startIndex && count < startIndex + viewsPerPage){
					ViewItem item = new ViewItem();
					item.setClassName(c.getSimpleName());
					item.setClassPackage(c.getPackage().getName());
					
					JenSoftView viewAnnot = c.getAnnotation(JenSoftView.class);
					JenSoftDashboard dashboardAnnot = c.getAnnotation(JenSoftDashboard.class);
					
					UIUnit uiunit = CatalogRepository.getViewUnitMapCache().get(c);
					if(uiunit != null){
						item.setUnit(uiunit.name());
					}else{
						item.setUnit("no unit");
					}
					

					
					if(uiunit != null){
						item.setUnit(uiunit.name());
					}else{
						item.setUnit("Default");
					}
					
					
					if(viewAnnot != null && viewAnnot.description()!=null){
						item.setDescription(viewAnnot.description());
						item.setType("view");
					}
					if(dashboardAnnot != null && dashboardAnnot.description()!=null){
						item.setDescription(dashboardAnnot.description());
						item.setType("dashboard");
					}
					
					catalogPart.add(item);
				}
				count++;
			}
			ViewsPage catalogPage = new ViewsPage();
			catalogPage.setPage(catalogPart);
			return Response.status(200).entity(catalogPage).build();
			
		}		
		return Response.status(200).build();
	}
	
	
	private Response getViewPage(int page,int viewsPerPage,String searchSequence){
		logger.info("fetch catalog views page with search sequence : "+searchSequence);
		Collection<Class<?>> viewsClasses = lookUpViews(searchSequence);
		int startIndex = page*viewsPerPage;
		if(startIndex < viewsClasses.size()){			
			ArrayList<ViewItem> catalogPart = new ArrayList<ViewItem>();
			int count = 0;
			for(Class<?> c : viewsClasses){
				if(count >= startIndex && count < startIndex + viewsPerPage){
					logger.info("process view : "+c.getSimpleName());
					ViewItem item = new ViewItem();
					item.setClassName(c.getSimpleName());
					item.setClassPackage(c.getPackage().getName());
					JenSoftView viewAnnot = c.getAnnotation(JenSoftView.class);
					JenSoftDashboard dashboardAnnot = c.getAnnotation(JenSoftDashboard.class);
					X2DView x2dAnnot = c.getAnnotation(X2DView.class);
					
					UIUnit uiunit = CatalogRepository.getViewUnitMapCache().get(c);
					if(uiunit != null){
						item.setUnit(uiunit.name());
					}else{
						//??
						item.setUnit(CatalogRepository.getDefaultUnit().name());
					}
					
					
					if(viewAnnot != null && viewAnnot.description()!=null){
						item.setDescription(viewAnnot.description());
						item.setType("view");
					}
					if(dashboardAnnot != null && dashboardAnnot.description()!=null){
						item.setDescription(dashboardAnnot.description());
						item.setType("dashboard");
					}
					if(x2dAnnot != null){
						item.setX2d("true");
					}else{
						item.setX2d("false");
					}
					
					//captchas
					if(viewAnnot != null && viewAnnot.captchas() != null){
						
						Captcha[] captchasResource = viewAnnot.captchas();
						logger.info("has captchas : "+c.getSimpleName() +" count : "+captchasResource.length);
						if(captchasResource != null && captchasResource.length > 0){
							ArrayList<CaptchaItem> captchaItems = new ArrayList<CaptchaItem>();
							item.setCaptchas(captchaItems);
							
							for (int i = 0; i < captchasResource.length; i++) {
								Captcha see = captchasResource[i];
								CaptchaItem captchaItem = new CaptchaItem();
								captchaItem.setQuestion(see.question());
								captchaItem.setAnwser(see.anwser());
								captchaItems.add(captchaItem);
							}
						}
					}else{
						logger.info("has NO captchas : "+c.getSimpleName());
					}
					
					
					//see resource
					if(viewAnnot != null && viewAnnot.see() != null){
						Class<?>[] seeResource = viewAnnot.see();
						if(seeResource != null && seeResource.length > 0){
							ArrayList<SeeItem> seesItems = new ArrayList<SeeItem>();
							item.setSees(seesItems);
							
							for (int i = 0; i < seeResource.length; i++) {
								Class<?> see = seeResource[i];
								SeeItem seeitem = new SeeItem();
								seeitem.setSeeClassName(see.getSimpleName());
								seeitem.setSeeClassPackage(see.getPackage().getName());
								seesItems.add(seeitem);
							}
						}
					}
					if(dashboardAnnot != null && dashboardAnnot.see() != null){
						Class<?>[] seeResource = dashboardAnnot.see();
						if(seeResource != null && seeResource.length > 0){
							ArrayList<SeeItem> seesItems = new ArrayList<SeeItem>();
							item.setSees(seesItems);
							
							for (int i = 0; i < seeResource.length; i++) {
								Class<?> see = seeResource[i];
								SeeItem seeitem = new SeeItem();
								seeitem.setSeeClassName(see.getSimpleName());
								seeitem.setSeeClassPackage(see.getPackage().getName());
								seesItems.add(seeitem);
							}
						}
					}
					
					
					catalogPart.add(item);
				}
				count++;
			}
			ViewsPage catalogPage = new ViewsPage();
			catalogPage.setPage(catalogPart);
			return Response.status(200).entity(catalogPage).build();
			
		}		
		return Response.status(200).build();
	}
	

	private Response getUnitPage(int page,int viewsPerPage,String searchSequence){
		logger.info("fetch catalog views page with search sequence : "+searchSequence);
		Collection<Class<?>> unitClasses = lookUpUnits(searchSequence);
		int startIndex = page*viewsPerPage;
		if(startIndex < unitClasses.size()){			
			ArrayList<UnitItem> catalogPart = new ArrayList<UnitItem>();
			int count = 0;
			for(Class<?> c : unitClasses){
				if(count >= startIndex && count < startIndex + viewsPerPage){
					UnitItem item = new UnitItem();
					UIUnit unit = (UIUnit)c.getPackage().getAnnotation(UIUnit.class);
					String unitName = unit.name();
					String unitDesc = unit.desc();
					
					
					int viewCount= 0;
					ConcurrentHashMap<Integer,Class<?>> unitViews = CatalogRepository.getUnitViewsMapCache().get(unit);
					if(unitViews != null){
						viewCount = unitViews.size();
					}
					item.setUnitViews(viewCount);
					item.setUnitName(unitName);
					item.setUnitInfo(unitDesc);		
					item.setCharts(new ArrayList<ViewItem>());
					ConcurrentHashMap<Integer,Class<?>> unitViewsClass = CatalogRepository.getUnitViewsMapCache().get(unit);
					for (Class<?> class1 : unitViewsClass.values()) {
						ViewItem vItem = new ViewItem();
						item.getCharts().add(vItem);
						
						JenSoftView viewAnnot = class1.getAnnotation(JenSoftView.class);
						JenSoftDashboard dashboardAnnot = class1.getAnnotation(JenSoftDashboard.class);
						
						if(viewAnnot != null && viewAnnot.description()!=null){
							//item.setDescription(viewAnnot.description());
							vItem.setType("view");
						}
						if(dashboardAnnot != null && dashboardAnnot.description()!=null){
							//item.setDescription(dashboardAnnot.description());
							vItem.setType("dashboard");
						}
						
						vItem.setClassName(class1.getSimpleName());
						vItem.setClassPackage(class1.getPackage().getName());
						//vItem.setUnit(unit.name());
						//JenSoftView viewAnnot = class1.getAnnotation(JenSoftView.class);
						if(viewAnnot != null && viewAnnot.description()!=null){
							vItem.setDescription(viewAnnot.description());
						}
					}
					catalogPart.add(item);
				}
				count++;
			}
			UnitsPage catalogPage = new UnitsPage();
			catalogPage.setPage(catalogPart);
			return Response.status(200).entity(catalogPage).build();
			
		}		
		return Response.status(200).build();
	}
	
	
	/**
	 * GET catalog ping 
	 * @return pong
	 */
	@GET
	@Path("/ping")
	public Response ping() {
		logger.info("ping catalog "+CatalogRepository.getCatalogGroup()+":"+CatalogRepository.getCatalogArtifact()+":"+CatalogRepository.getCatalogVersion());
		return Response.status(200).entity("pong "+ CatalogRepository.getCatalogGroup()+":"+CatalogRepository.getCatalogArtifact()+":"+CatalogRepository.getCatalogVersion()).build();
	}
	
	/**
	 * GET catalog def 
	 * @return def
	 */
	@GET
	@Path("/def")
	@Produces(MediaType.APPLICATION_XML)
	public Response def() {
		Class<?> catalog = CatalogRepository.getCatalog();
		Catalog c = catalog.getPackage().getAnnotation(Catalog.class);
		
		CatalogItem catalogItem = new CatalogItem();
		catalogItem.setName(c.name());
		catalogItem.setArtifact(c.artifact());
		catalogItem.setGroup(c.group());
		catalogItem.setVersion(c.version());
		catalogItem.setDescription(c.description());
		catalogItem.setCore(c.core());
		
		catalogItem.setCountCharts(CatalogRepository.getViewsCache().size()+CatalogRepository.getDashboardsCache().size());
		catalogItem.setCountUnits(CatalogRepository.getUnitsCache().size());
		
		
		logger.info("def catalog "+catalogItem.toString());
		return Response.ok().entity(catalogItem).build();
	}
	

}

package com.jensoft.catalog.server;

import java.awt.image.BufferedImage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/captcha")
public class CaptchaServer {

	static Logger logger = Logger.getLogger(CaptchaServer.class);

	public CaptchaServer() {
		//logger.info("Create Captcha server");
	}

	

	@GET
	@Path("/count")
	public Response count() {
		//logger.info("count captcha api " + CaptchaRepository.getCaptchaCount());
		return Response.status(200).entity(Integer.toString(CatalogRepository.getCaptchaCount())).build();
	}


	@GET
	@Path("/def/{type}")
	public Response def(@PathParam("type") String type) {
		CaptchaItem item = CatalogRepository.getCaptcha();
		//logger.info("get captcha :" + item.getQuestion());
		if ("xml".equals(type)) {
			return Response.status(200).type(MediaType.APPLICATION_XML).entity(CatalogRepository.getCaptcha()).build();
		} else if ("json".equals(type)) {
			return Response.status(200).type(MediaType.APPLICATION_JSON).entity(CatalogRepository.getCaptcha()).build();
		}
		return Response.status(200).entity(CatalogRepository.getCaptcha()).build();
	}

	
	@GET
	@Path("/image/{defId}")
	public Response image(@PathParam("defId") int captchaId) {
		//logger.info("get captcha image for captcha class :" + captchaId);
		BufferedImage img = CatalogRepository.getCaptchaImage(captchaId);
		return Response.status(200).type("image/png").entity(img).build();
	}

	@GET
	@Path("/image/{captcha}/{width}/{height}")
	public Response image(@PathParam("captcha") String captcha, @PathParam("width") int width, @PathParam("height") int height) {
		//logger.info("get captcha sized image for captcha class :" + captcha + " with @Dim(width=" + width + ",height" + height + ")");
		return Response.status(200).entity(CatalogRepository.getCaptcha()).build();
	}

}

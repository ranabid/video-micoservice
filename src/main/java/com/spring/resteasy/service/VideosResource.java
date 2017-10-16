package com.spring.resteasy.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.management.monitor.Monitor;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Path(VideosResource.VIDEOS_URL)
@PropertySources({ @PropertySource("classpath:application.properties") })
public class VideosResource {
	public static final String VIDEOS_URL = "/videos";
	public static final Logger LOGGER = LoggerFactory.getLogger(VideosResource.class);

	@Autowired
	VideosService videosService;

	@HEAD
	@Produces("video/mp4")
	public Response header() throws Exception {

		return Response.ok().status(206).header(HttpHeaders.CONTENT_LENGTH, videosService.getVideoContentLength())
				.build();

	}

	@GET
	@Path("/{fileName}")
	@Produces("video/mp4")
	public Response streamVideoContent(@PathParam("fileName") String fileName, @HeaderParam("Range") String range)
			throws Exception {
		return videosService.buildVideoStream(fileName, range);
	}
	
	@GET
	@Path("/image/{fileName}")
	@Produces("image/jpeg")
	public Response getThumbnail(@PathParam("fileName") String fileName) throws Exception {
		
		File file = videosService.getThumbnail(fileName);
		return Response.ok(new FileInputStream(file)).build();
		
	}
}

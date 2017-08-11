package com.video.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Path("/service")
@Component
public class VideoController {
	private File videoFile;
	private final int chunkSize = 1024 * 1024;
	final static Logger logger = LoggerFactory.getLogger(VideoController.class);

	@HEAD
	@Produces("video/mp4")
	public Response header() {
		return Response.ok().status(206).header(HttpHeaders.CONTENT_LENGTH, videoFile.length()).build();
	}

	// @GET
	// @Path("/videos/{fileName}")
	// @Produces({ MediaType.APPLICATION_JSON })
	// public String getData(@PathParam("fileName") String fileName,
	// @HeaderParam("Range") String range) {
	// logger.info("Test succeed: "+fileName+" range: "+range);
	// return "Success";
	// }
	@GET
	@Path("/videos/{fileName}")
	@Produces("video/mp4")
	public Response streamVideoContent(@PathParam("fileName") String fileName, @HeaderParam("Range") String range)
			throws Exception {
		// URL url = this.getClass().getResource(fileName);
		String targetFileName = "/home/rana/eclipse-workspace/video-micro-service/src/main/resources/" + fileName;
		this.videoFile = new File(targetFileName);
		logger.info("File name: " + this.videoFile.getName() + " " + "Range: " + range);

		return buildVideoStream(this.videoFile, range);

	}

	private Response buildVideoStream(final File fileName, final String range) throws Exception {
		if (range == null) {
			StreamingOutput streamer = output -> {
				try (FileChannel inputChannel = new FileInputStream(fileName).getChannel();
						WritableByteChannel outputChannel = Channels.newChannel(output)) {
					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
				}
			};
			return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, fileName.length()).build();
		} else {

			String[] ranges = range.split("=")[1].split("-");
			final int from = Integer.parseInt(ranges[0]);
			int to = chunkSize + from;
			if (to >= fileName.length()) {
				to = (int) (fileName.length() - 1);
			}
			if (ranges.length == 2) {
				to = Integer.parseInt(ranges[1]);
			}

			final String responseRange = String.format("bytes %d-%d/%d", from, to, fileName.length());
			final RandomAccessFile raf = new RandomAccessFile(fileName, "r");
			raf.seek(from);

			final int len = to - from + 1;
			final VideoStreamer streamer = new VideoStreamer(len, raf);
			Response.ResponseBuilder res = Response.ok(streamer).status(Response.Status.PARTIAL_CONTENT)
					.header("Accept-Ranges", "bytes").header("Content-Range", responseRange)
					.header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
					.header(HttpHeaders.LAST_MODIFIED, new Date(fileName.lastModified()));
			return res.build();

		}
	}

}

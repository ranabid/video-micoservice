package com.spring.resteasy.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class VideosService {
	public static final Logger LOGGER = LoggerFactory.getLogger(VideosService.class);
	@Autowired
	Environment env;
	private File videoFile;
	final int CHUNK_SIZE;

	public VideosService() {
		this.CHUNK_SIZE = 1024 * 1024;

	}

	public long getVideoContentLength() {
		return this.videoFile.length();
	}

	public Response buildVideoStream(String fileName, String range) throws Exception {

		String targetFile = env.getProperty("videoRepo") + fileName;
		videoFile = new File(targetFile);

		if (range == null) {
			StreamingOutput streamer = output -> {
				try (FileChannel inputChannel = new FileInputStream(videoFile).getChannel();
						WritableByteChannel outputChannel = Channels.newChannel(output)) {
					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
				}
			};
			return Response.ok(streamer).status(200).header(HttpHeaders.CONTENT_LENGTH, videoFile.length()).build();
		} else {

			String[] ranges = range.split("=")[1].split("-");
			final int from = Integer.parseInt(ranges[0]);
			int to = CHUNK_SIZE + from;
			if (to >= videoFile.length()) {
				to = (int) (videoFile.length() - 1);
			}
			if (ranges.length == 2) {
				to = Integer.parseInt(ranges[1]);
			}

			final String responseRange = String.format("bytes %d-%d/%d", from, to, videoFile.length());
			final RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
			raf.seek(from);

			final int len = to - from + 1;
			final VideoStreamer streamer = new VideoStreamer(len, raf);
			Response.ResponseBuilder res = Response.ok(streamer).status(Response.Status.PARTIAL_CONTENT)
					.header("Accept-Ranges", "bytes").header("Content-Range", responseRange)
					.header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
					.header(HttpHeaders.LAST_MODIFIED, new Date(videoFile.lastModified()));
			return res.build();

		}

	}

	public File getThumbnail(String fileName) throws Exception {
		final String imgFile = env.getProperty("videoRepo") + fileName;

		File file = new File(imgFile);
		System.out.println(file.getParent());
		if (file.exists() && file.isFile()) {
			return file;
		} else {
			LOGGER.info("file"+ imgFile +" does not exists");
			throw new Exception("file does not exists");
		}
		
	}

}

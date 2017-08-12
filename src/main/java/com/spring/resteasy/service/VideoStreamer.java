package com.spring.resteasy.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoStreamer implements StreamingOutput {
	public static final Logger LOGGER = LoggerFactory.getLogger(VideoStreamer.class);
    private int length;
    private RandomAccessFile raf;
    final byte[] buf = new byte[4096];

    public VideoStreamer(int length, RandomAccessFile raf) {
        this.length = length;
        this.raf = raf;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            while( length != 0) {
                int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
                outputStream.write(buf, 0, read);
                length -= read;
            }
        } finally {
            raf.close();
        }
    }

    public int getLenth() {
        return length;
    }
}
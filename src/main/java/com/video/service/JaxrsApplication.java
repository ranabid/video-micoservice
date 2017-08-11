package com.video.service;

import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application
 *
 * Created by facarvalho on 12/7/15.
 */
@Component
@ApplicationPath("/video-micro-service/")
public class JaxrsApplication extends Application {
}
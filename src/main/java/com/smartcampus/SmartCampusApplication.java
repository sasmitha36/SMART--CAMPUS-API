package com.smartcampus;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.smartcampus.filters.ApiLoggingFilter;
import com.smartcampus.mappers.GlobalExceptionMapper;
import com.smartcampus.mappers.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.mappers.RoomNotEmptyExceptionMapper;
import com.smartcampus.mappers.SensorUnavailableExceptionMapper;
import com.smartcampus.resources.DiscoveryResource;
import com.smartcampus.resources.SensorReadingResource;
import com.smartcampus.resources.SensorResource;
import com.smartcampus.resources.SensorRoomResource;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);
        classes.add(SensorReadingResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(ApiLoggingFilter.class);
        classes.add(JacksonFeature.class);
        return classes;
    }
}

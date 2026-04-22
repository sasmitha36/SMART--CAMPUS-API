package com.smartcampus.resources;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getDiscoveryDocument() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Smart Campus Sensor & Room Management API");
        payload.put("version", "v1");
        payload.put("contact", "facilities-api-admin@westminster.example");

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        payload.put("resources", resources);

        return Response.ok(payload).build();
    }
}

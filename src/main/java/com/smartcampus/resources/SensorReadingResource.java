package com.smartcampus.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.SensorReading;
import com.smartcampus.store.CampusRepository;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final CampusRepository repository = CampusRepository.getInstance();
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return repository.getReadings(sensorId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        if (reading == null) {
            throw new WebApplicationException("Reading payload is required.", Response.Status.BAD_REQUEST);
        }
        SensorReading created = repository.addReading(sensorId, reading);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }
}

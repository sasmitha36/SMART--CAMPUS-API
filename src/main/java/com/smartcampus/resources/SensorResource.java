package com.smartcampus.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.CampusRepository;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final CampusRepository repository = CampusRepository.getInstance();

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        return repository.getAllSensors(type);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        validateSensor(sensor);
        Sensor created = repository.createSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null
                || isBlank(sensor.getId())
                || isBlank(sensor.getType())
                || isBlank(sensor.getStatus())
                || isBlank(sensor.getRoomId())) {
            throw new WebApplicationException(
                    "Sensor payload must include id, type, status, and roomId.",
                    Response.Status.BAD_REQUEST);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package com.smartcampus.resources;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.smartcampus.model.Room;
import com.smartcampus.store.CampusRepository;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final CampusRepository repository = CampusRepository.getInstance();

    @GET
    public List<Room> getRooms() {
        return repository.getAllRooms();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        validateRoom(room);
        Room created = repository.createRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(created.getId()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = repository.getRoom(roomId);
        if (room == null) {
            throw new javax.ws.rs.NotFoundException("Room " + roomId + " not found.");
        }
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = repository.getRoom(roomId);
        if (room == null) {
            return Response.noContent().build();
        }

        repository.deleteRoom(roomId);
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("message", "Room " + roomId + " deleted successfully.");
        return Response.ok(body).build();
    }

    private void validateRoom(Room room) {
        if (room == null || isBlank(room.getId()) || isBlank(room.getName()) || room.getCapacity() <= 0) {
            throw new WebApplicationException(
                    "Room payload must include id, name, and positive capacity.",
                    Response.Status.BAD_REQUEST);
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new CopyOnWriteArrayList<>());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

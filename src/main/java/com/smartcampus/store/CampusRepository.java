package com.smartcampus.store;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

public final class CampusRepository {

    private static final CampusRepository INSTANCE = new CampusRepository();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingsBySensorId = new ConcurrentHashMap<>();

    private CampusRepository() {
        seedData();
    }

    public static CampusRepository getInstance() {
        return INSTANCE;
    }

    public List<Room> getAllRooms() {
        return rooms.values().stream()
                .sorted(Comparator.comparing(Room::getId))
                .collect(Collectors.toList());
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Room createRoom(Room room) {
        if (rooms.putIfAbsent(room.getId(), room) != null) {
            throw new IllegalArgumentException("Room with id " + room.getId() + " already exists.");
        }
        return room;
    }

    public void deleteRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still has sensors assigned.");
        }
        rooms.remove(roomId);
    }

    public List<Sensor> getAllSensors(String type) {
        return sensors.values().stream()
                .filter(sensor -> type == null || sensor.getType().equalsIgnoreCase(type))
                .sorted(Comparator.comparing(Sensor::getId))
                .collect(Collectors.toList());
    }

    public Sensor createSensor(Sensor sensor) {
        Room room = rooms.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Referenced room " + sensor.getRoomId() + " does not exist.");
        }
        if (sensors.putIfAbsent(sensor.getId(), sensor) != null) {
            throw new IllegalArgumentException("Sensor with id " + sensor.getId() + " already exists.");
        }

        room.getSensorIds().add(sensor.getId());
        readingsBySensorId.putIfAbsent(sensor.getId(), new CopyOnWriteArrayList<>());
        return sensor;
    }

    public List<SensorReading> getReadings(String sensorId) {
        ensureSensorExists(sensorId);
        return readingsBySensorId.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
    }

    public SensorReading addReading(String sensorId, SensorReading reading) {
        Sensor sensor = ensureSensorExists(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is under maintenance and cannot accept readings.");
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        readingsBySensorId.computeIfAbsent(sensorId, ignored -> new CopyOnWriteArrayList<>()).add(reading);
        sensor.setCurrentValue(reading.getValue());
        return reading;
    }

    private Sensor ensureSensorExists(String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new javax.ws.rs.NotFoundException("Sensor " + sensorId + " does not exist.");
        }
        return sensor;
    }

    private void seedData() {
        Room library = new Room("LIB-301", "Library Quiet Study", 120);
        Room lab = new Room("LAB-201", "IoT Systems Lab", 45);
        rooms.put(library.getId(), library);
        rooms.put(lab.getId(), lab);

        Sensor co2Sensor = new Sensor("CO2-001", "CO2", "ACTIVE", 418.0, library.getId());
        Sensor tempSensor = new Sensor("TEMP-001", "Temperature", "MAINTENANCE", 21.7, lab.getId());

        sensors.put(co2Sensor.getId(), co2Sensor);
        sensors.put(tempSensor.getId(), tempSensor);

        library.getSensorIds().add(co2Sensor.getId());
        lab.getSensorIds().add(tempSensor.getId());

        List<SensorReading> co2Readings = new CopyOnWriteArrayList<>();
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 10_000, 410.0));
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 418.0));
        readingsBySensorId.put(co2Sensor.getId(), co2Readings);
        readingsBySensorId.put(tempSensor.getId(), new CopyOnWriteArrayList<>());
    }
}

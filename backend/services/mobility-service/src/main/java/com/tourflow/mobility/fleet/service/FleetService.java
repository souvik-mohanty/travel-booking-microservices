package com.tourflow.mobility.fleet.service;

import com.tourflow.mobility.fleet.domain.Vehicle;
import com.tourflow.mobility.fleet.domain.VehicleStatus;
import com.tourflow.mobility.fleet.dto.CreateVehicleRequest;
import com.tourflow.mobility.fleet.dto.UpdateVehicleRequest;
import com.tourflow.mobility.fleet.dto.VehicleResponse;
import com.tourflow.mobility.fleet.exception.VehicleAccessDeniedException;
import com.tourflow.mobility.fleet.exception.VehicleAlreadyExistsException;
import com.tourflow.mobility.fleet.exception.VehicleNotFoundException;
import com.tourflow.mobility.fleet.repository.VehicleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FleetService {

    private final VehicleRepository vehicleRepository;

    public FleetService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse createVehicle(
            CreateVehicleRequest request,
            UUID ownerId
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        Vehicle vehicle = new Vehicle(
                UUID.randomUUID(),
                ownerId,
                request.registrationNumber(),
                request.type(),
                request.capacity(),
                VehicleStatus.AVAILABLE,
                now,
                now
        );

        try {
            vehicle = vehicleRepository.saveAndFlush(vehicle);
        } catch (DataIntegrityViolationException ex) {
            throw new VehicleAlreadyExistsException(
                    "A vehicle with this registration number already exists"
            );
        }

        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehicles() {

        return vehicleRepository.findAll()
                .stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles(UUID ownerId) {

        return vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    @Transactional
    public VehicleResponse updateVehicle(
            UUID vehicleId,
            UUID ownerId,
            UpdateVehicleRequest request
    ) {

        Vehicle vehicle = requireOwnedVehicle(vehicleId, ownerId);

        vehicle.update(request.type(), request.capacity());

        return VehicleResponse.fromEntity(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleResponse setStatus(
            UUID vehicleId,
            UUID ownerId,
            VehicleStatus status
    ) {

        Vehicle vehicle = requireOwnedVehicle(vehicleId, ownerId);

        if (vehicle.getStatus() == status) {
            return VehicleResponse.fromEntity(vehicle);
        }

        vehicle.setStatus(status);
        vehicle.setUpdatedAt(OffsetDateTime.now());

        return VehicleResponse.fromEntity(vehicleRepository.save(vehicle));
    }

    private Vehicle requireOwnedVehicle(UUID vehicleId, UUID ownerId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        if (!vehicle.getOwnerId().equals(ownerId)) {
            throw new VehicleAccessDeniedException(
                    "You are not authorized to manage this vehicle"
            );
        }

        return vehicle;
    }
}

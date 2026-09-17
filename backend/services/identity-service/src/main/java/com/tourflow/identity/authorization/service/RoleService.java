package com.tourflow.authorization.service;

import com.tourflow.authorization.domain.Role;
import com.tourflow.authorization.dto.CreateRoleRequest;
import com.tourflow.authorization.dto.RoleResponse;
import com.tourflow.authorization.exception.AlreadyExistsException;
import com.tourflow.authorization.exception.RoleNotFoundException;
import com.tourflow.authorization.repository.RoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Role/permission definitions are platform configuration, not user data --
// there's no ownership model here (and no admin role to gate creation behind
// yet, see identity-service's flat "role" claim), so any authenticated
// caller can define roles/permissions for now.
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {

        OffsetDateTime now = OffsetDateTime.now();

        Role role = new Role(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                now
        );

        try {
            role = roleRepository.saveAndFlush(role);
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExistsException("A role named '" + request.name() + "' already exists");
        }

        return RoleResponse.fromEntity(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        return RoleResponse.fromEntity(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {

        return roleRepository.findAll()
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }
}

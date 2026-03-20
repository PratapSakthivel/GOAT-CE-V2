package com.goatce.controller;

import com.goatce.dto.RoleChangeRequest;
import com.goatce.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/{roomCode}/change")
    public ResponseEntity<String> changeRole(@PathVariable String roomCode,
                                             @Valid @RequestBody RoleChangeRequest request,
                                             Principal principal) {
        return ResponseEntity.ok(roleService.changeRole(principal.getName(), roomCode, request));
    }

    @GetMapping("/{roomCode}/my-role")
    public ResponseEntity<String> getMyRole(@PathVariable String roomCode, Principal principal) {
        return ResponseEntity.ok(roleService.getMyRole(principal.getName(), roomCode));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

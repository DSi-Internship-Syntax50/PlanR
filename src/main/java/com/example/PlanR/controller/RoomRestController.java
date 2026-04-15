package com.example.PlanR.controller;

import com.example.PlanR.dto.RoomDto;
import com.example.PlanR.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomRestController {

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        List<RoomDto> rooms = roomRepository.findAll()
            .stream()
            .map(RoomDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }
}

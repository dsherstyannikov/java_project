package com.todoapp.backend.services;

import com.todoapp.backend.dto.ColorDTO;
import com.todoapp.backend.models.Color;
import com.todoapp.backend.repositories.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    public List<ColorDTO> getAllColors() {
        return colorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ColorDTO mapToDTO(Color color) {
        ColorDTO dto = new ColorDTO();
        dto.setId(color.getId());
        dto.setHashCode(color.getHashCode());
        dto.setName(color.getName());
        return dto;
    }
}
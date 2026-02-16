package com.marina.demo.dto;

import java.math.BigDecimal;

public record JobDTO(
        Long id,
        String title,
        String companyName,
        String location,
        BigDecimal salary,
        String description
) {}
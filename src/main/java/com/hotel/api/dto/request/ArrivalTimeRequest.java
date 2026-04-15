package com.hotel.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArrivalTimeRequest {

    @NotBlank
    private String checkIn;

    private String checkOut;
}

package com.hotel.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HotelCreateRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String brand;

    @Valid
    @NotNull
    private AddressRequest address;

    @Valid
    @NotNull
    private ContactsRequest contacts;

    @Valid
    @NotNull
    private ArrivalTimeRequest arrivalTime;
}

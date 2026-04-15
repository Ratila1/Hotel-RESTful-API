package com.hotel.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactsRequest {

    @NotBlank
    private String phone;

    private String email;
}

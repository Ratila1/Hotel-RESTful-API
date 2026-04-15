package com.hotel.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelBriefResponse {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
}

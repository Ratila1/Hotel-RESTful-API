package com.hotel.api.exception;

public class InvalidHistogramParamException extends RuntimeException {

    public InvalidHistogramParamException(String param) {
        super("Invalid histogram parameter: '" + param + "'. Supported: brand, city, country, amenities");
    }
}

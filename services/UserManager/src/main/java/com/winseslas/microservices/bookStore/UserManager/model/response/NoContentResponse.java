package com.winseslas.microservices.bookStore.UserManager.model.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for a response indicating no content.
 */
@Setter
@Getter
public class NoContentResponse {
    private boolean success = false;
    private String message = "No Content. No data";

}

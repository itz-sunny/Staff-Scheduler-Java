package com.staffscheduler.apidoc;

import io.swagger.v3.oas.annotations.media.Schema;

public class ErrorResponse {
    private String message;
    @Schema(name = "_links")
    private Link links;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Link getLinks() {
        return links;
    }

    public void setLinks(Link links) {
        this.links = links;
    }
}

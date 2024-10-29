package com.yourpackage.server;

import com.yourpackage.models.UserService;

public class DeleteRequestHandler {
    private final UserService userService;

    public DeleteRequestHandler(UserService userService) {
        this.userService = userService;
    }

}

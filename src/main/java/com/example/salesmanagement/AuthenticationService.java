package com.example.salesmanagement;

import com.example.salesmanagement.user.User;

public class AuthenticationService {
    private static User currentAuthenticatedUser;

    public static User getCurrentAuthenticatedUser() {
        return currentAuthenticatedUser;
    }

    public static void setCurrentAuthenticatedUser(User currentAuthenticatedUser) {
        AuthenticationService.currentAuthenticatedUser = currentAuthenticatedUser;
    }

}

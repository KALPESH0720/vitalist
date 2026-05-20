package com.hospital.client.model;

public class Session {
    private static Session instance;

    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private String basicToken;

    private Session() {}

    public static Session get() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public void login(Long userId, String username, String fullName, String role, String basicToken) {
        this.userId     = userId;
        this.username   = username;
        this.fullName   = fullName;
        this.role       = role;
        this.basicToken = basicToken;
    }

    public void logout() {
        userId = null; username = null; fullName = null; role = null; basicToken = null;
    }

    public boolean isLoggedIn()   { return username != null; }
    public Long   getUserId()     { return userId; }
    public String getUsername()   { return username; }
    public String getFullName()   { return fullName; }
    public String getRole()       { return role; }
    public String getBasicToken() { return basicToken; }

    public boolean isAdmin()        { return "ADMIN".equals(role); }
    public boolean isDoctor()       { return "DOCTOR".equals(role); }
    public boolean isReceptionist() { return "RECEPTIONIST".equals(role); }

    // Access rules
    public boolean canManageUsers()      { return isAdmin(); }
    public boolean canUseAI()            { return isAdmin() || isDoctor(); }
    public boolean canWriteInventory()   { return isAdmin(); }
    public boolean canViewInventory()    { return true; }
    public boolean canManagePatients()   { return true; }
    public boolean canManageAppointments(){ return true; }
}

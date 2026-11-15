package com.mindsmart.dto;

import com.mindsmart.entity.Role;

public class AuthResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Long profileId;
    private boolean verified;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String name, String email, String phone, Role role, Long profileId, boolean verified, String message) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profileId = profileId;
        this.verified = verified;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

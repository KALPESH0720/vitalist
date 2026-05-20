package com.hospital.dto;

import com.hospital.model.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String phone;
    private boolean active;

    public static UserDTO fromEntity(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .build();
    }
}

package com.booking.authservice.model.dto;

import com.booking.authservice.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternalUserResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public static InternalUserResponse from(User user) {
        return InternalUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}

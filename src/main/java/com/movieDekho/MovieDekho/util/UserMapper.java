package com.movieDekho.MovieDekho.util;

import com.movieDekho.MovieDekho.dtos.user.UserResponseDTO;
import com.movieDekho.MovieDekho.models.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    
    public static UserResponseDTO toUserResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .role(user.getRole())
                .build();
    }
}

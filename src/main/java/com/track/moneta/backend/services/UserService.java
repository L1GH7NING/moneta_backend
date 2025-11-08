package com.track.moneta.backend.services;

import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.dto.UserUpdateDTO;

public interface UserService {
//    UserDTO getCurrentUser();
    UserDTO updateUser(Long userId, UserUpdateDTO updateDTO);
    UserDTO getUserById(Long userId);
}
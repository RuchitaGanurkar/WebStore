package com.webstore.service;
import com.webstore.dto.request.UserRequestDto;
import com.webstore.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Integer userId);
    UserResponseDto createUser(UserRequestDto userRequestDto);
    UserResponseDto updateUser(Integer userId, UserRequestDto userRequestDto);

    void deleteUser(Integer userId);


}

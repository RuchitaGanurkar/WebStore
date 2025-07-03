package com.webstore.service.product;
import com.webstore.dto.request.product.UserRequestDto;
import com.webstore.dto.response.product.UserResponseDto;
import java.util.List;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Integer userId);
    UserResponseDto createUser(UserRequestDto userRequestDto);
    UserResponseDto updateUser(Integer userId, UserRequestDto userRequestDto);

    void deleteUser(Integer userId);


}

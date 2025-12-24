package com.shalom.shalomhotel.Service.interfac;

import com.shalom.shalomhotel.Dto.LoginRequest;
import com.shalom.shalomhotel.Dto.Response;
import com.shalom.shalomhotel.Dto.UpdateUserRequest;
import com.shalom.shalomhotel.entity.User;

public interface IUserService {

    Response register(User user);

    Response login(LoginRequest loginRequest);

    Response getAllUsers();

    Response getUserBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);

    Response updateUser(String userId, UpdateUserRequest updateRequest);
}

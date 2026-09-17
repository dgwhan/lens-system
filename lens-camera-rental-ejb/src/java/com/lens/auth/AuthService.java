/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.lens.auth;

import com.lens.common.util.PasswordService;
import com.lens.user.facade.UsersFacadeLocal;
import com.lens.auth.dto.LoginRequest;
import com.lens.auth.dto.RegisterRequest;
import com.lens.user.entity.Users;
import jakarta.ejb.Stateless;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class AuthService implements AuthServiceLocal {

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    private PasswordService passwordService = new PasswordService();

    @Override
    public Users login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return null;
        }

        Users user = usersFacade.findByUsername(request.getUsername());
        if (user == null) {
            return null;
        }

        //kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        //kiểm tra password
        if (!passwordService.verify(request.getPassword(), user.getPassword())) {
            return null;
        }
        return user;
    }

    @Override
    public Users register(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        //kiểm tra username đã tồn tại
        if (usersFacade.isUsernameExists(request.getUsername())) {
            return null;
        }

        //kiểm tra email đã tồn tại nếu có giá trị
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (usersFacade.isEmailExists(request.getEmail().trim(), null)) {
                return null;
            }
        }

        //kiểm tra phone đã tồn tại
        if (usersFacade.isPhoneExists(request.getPhone(), null)) {
            return null;
        }

        //tạo user mới
        Users user = new Users();

        //gán username
        user.setUsername(request.getUsername());

        //hash password trước khi lưu
        String hashedPassword = passwordService.hash(request.getPassword());

        //gán password đã hash
        user.setPassword(hashedPassword);

        //gán thông tin người dùng
        user.setFullName(request.getFullName());
        String email = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null;
        user.setEmail(email);
        user.setPhone(request.getPhone());

        //gán role mặc định cho user đăng ký
        user.setRole("CUSTOMER");

        //kích hoạt tài khoản
        user.setStatus("ACTIVE");

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        //lưu db thông qua facade
        usersFacade.create(user);
        
        return user;
    }

}

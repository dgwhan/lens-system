/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/SessionLocal.java to edit this template
 */
package com.lens.auth;

import com.lens.auth.dto.LoginRequest;
import com.lens.auth.dto.RegisterRequest;
import com.lens.user.entity.Users;
import jakarta.ejb.Local;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface AuthServiceLocal {
    Users login(LoginRequest request);
    Users register(RegisterRequest request);
}

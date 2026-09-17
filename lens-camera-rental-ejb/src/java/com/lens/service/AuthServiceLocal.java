/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/SessionLocal.java to edit this template
 */
package com.lens.service;

import com.lens.dto.auth.LoginRequest;
import com.lens.dto.auth.RegisterRequest;
import com.lens.entity.Users;
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

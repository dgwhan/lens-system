/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package com.lens.service;

import com.lens.util.PasswordService;
import com.lens.facade.UsersFacadeLocal;
import com.lens.dto.auth.LoginRequest;
import com.lens.dto.auth.RegisterRequest;
import com.lens.entity.Users;
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

        //kiÃŸâ•—Ã¢m tra trÃŸâ•‘Ã­ng thâ”œÃ­i tâ”œÃ¡i khoÃŸâ•‘Ãºn
        if (!"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        //kiÃŸâ•—Ã¢m tra password
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

        //kiÃŸâ•—Ã¢m tra username â”€Ã¦â”œÃº tÃŸâ•—Ã´n tÃŸâ•‘Ã­i
        if (usersFacade.isUsernameExists(request.getUsername())) {
            return null;
        }

        //kiÃŸâ•—Ã¢m tra email â”€Ã¦â”œÃº tÃŸâ•—Ã´n tÃŸâ•‘Ã­i nÃŸâ•‘â”u câ”œâ”‚ giâ”œÃ­ trÃŸâ•—Ã¯
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (usersFacade.isEmailExists(request.getEmail().trim(), null)) {
                return null;
            }
        }

        //kiÃŸâ•—Ã¢m tra phone â”€Ã¦â”œÃº tÃŸâ•—Ã´n tÃŸâ•‘Ã­i
        if (usersFacade.isPhoneExists(request.getPhone(), null)) {
            return null;
        }

        //tÃŸâ•‘Ã­o user mÃŸâ•—Â¢i
        Users user = new Users();

        //gâ”œÃ­n username
        user.setUsername(request.getUsername());

        //hash password trâ•žâ–‘ÃŸâ•—Â¢c khi lâ•žâ–‘u
        String hashedPassword = passwordService.hash(request.getPassword());

        //gâ”œÃ­n password â”€Ã¦â”œÃº hash
        user.setPassword(hashedPassword);

        //gâ”œÃ­n thâ”œâ”¤ng tin ngâ•žâ–‘ÃŸâ•—Â¥i dâ”œâ•£ng
        user.setFullName(request.getFullName());
        String email = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim() : null;
        user.setEmail(email);
        user.setPhone(request.getPhone());

        //gâ”œÃ­n role mÃŸâ•‘â•–c â”€Ã¦ÃŸâ•—Ã¯nh cho user â”€Ã¦â”€Ã¢ng kâ”œâ•œ
        user.setRole("CUSTOMER");

        //kâ”œÂ¡ch hoÃŸâ•‘Ã­t tâ”œÃ¡i khoÃŸâ•‘Ãºn
        user.setStatus("ACTIVE");

        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        //lâ•žâ–‘u db thâ”œâ”¤ng qua facade
        usersFacade.create(user);
        
        return user;
    }

}

package com.lens.controller;

import com.lens.dto.auth.LoginRequest;
import com.lens.dto.auth.RegisterRequest;
import com.lens.entity.Users;
import com.lens.service.AuthServiceLocal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "authController")
@RequestScoped
public class AuthController implements Serializable {

    @jakarta.ejb.EJB
    private AuthServiceLocal authService;

    private String username;
    private String password;

    private String fullName;
    private String email;
    private String phone;

    public AuthController() {
    }

    //login
    public String login() {
        LoginRequest request = new LoginRequest(username, password);

        Users user = authService.login(request);

        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Incorrect username or password.")
            );

            return null;
        }

        FacesContext context = FacesContext.getCurrentInstance();

        HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());

        if ("ADMIN".equals(user.getRole())) {
            return "/admin/dashboard?faces-redirect=true";
        }

        return "/index?faces-redirect=true";
    }

    //logout 
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "/login?faces-redirect=true";
    }

    //register
    public String register() {
        RegisterRequest request = new RegisterRequest(username, password, fullName, email, phone);

        Users user = authService.register(request);

        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Registration failed",
                            "Username, email or phone number already exists."
                    )
            );

            return null;
        }
        
        return "/login?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

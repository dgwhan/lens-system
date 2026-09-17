package com.lens.auth;

import com.lens.auth.dto.RegisterRequest;
import com.lens.auth.security.SecurityRoles;
import com.lens.user.entity.Users;
import com.lens.user.facade.UsersFacadeLocal;
import com.lens.common.util.FacesUtil;
import com.lens.common.util.ValidationUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "authController")
@RequestScoped
public class AuthController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());

    @jakarta.ejb.EJB
    private AuthServiceLocal authService;

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    @Inject
    private SecurityContext securityContext;

    private String username;
    private String password;

    private String fullName;
    private String email;
    private String phone;

    public AuthController() {
    }

    // login
    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();

        // lấy request/response hiện tại từ JSF
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        // tạo credentail từ thông tin ng dùng nhập
        AuthenticationParameters authenticationParameters = AuthenticationParameters.withParams()
                .credential(new UsernamePasswordCredential(username, password));

        // yêu cầu jakarta security thực hiện authentication
        AuthenticationStatus status = securityContext.authenticate(request, response, authenticationParameters);

        if (status == AuthenticationStatus.SUCCESS) {
            Users user = usersFacade.findByUsername(username);
            if (user != null && user.getFullName() != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("fullName", user.getFullName());
            }

            if (request.isUserInRole(SecurityRoles.ADMIN) || securityContext.isCallerInRole(SecurityRoles.ADMIN)) {
                return "/admin/dashboard?faces-redirect=true";
            }
            return "/index?faces-redirect=true";
        }

        // authentication mechanism đã xử lý response và yêu cầu request flow tiếp tục
        if (status == AuthenticationStatus.SEND_CONTINUE) {
            return null;
        }

        LOGGER.warning("Authentication failed: Invalid username or password.");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password.",
                "Invalid username or password."));

        return null;
    }

    // logout
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();

        // lấy request/response hiện tại từ JSF
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        // yêu cầu jakarta security thực hiện logout
        try {
            request.logout();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } catch (ServletException e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logout failed", "Unable to logout."));

            return null;
        }

        return "/login?faces-redirect=true";
    }

    public String getCurrentUserFullName() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) {
            return "";
        }
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        if (request == null || request.getUserPrincipal() == null) {
            return "";
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String cachedFullName = (String) session.getAttribute("fullName");
            if (cachedFullName != null && !cachedFullName.trim().isEmpty()) {
                return cachedFullName;
            }
        }

        Users user = usersFacade.findByUsername(request.getUserPrincipal().getName());
        if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            if (session != null) {
                session.setAttribute("fullName", user.getFullName());
            }
            return user.getFullName();
        }

        return request.getUserPrincipal().getName();
    }

    // register
    public String register() {
        boolean hasError = false;

        // kiểm tra username
        if (username == null || username.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:username", "Username is required.");
            LOGGER.warning("Username is empty.");
            hasError = true;
        } else if (usersFacade.isUsernameExists(username.trim())) {
            FacesUtil.addFieldError("registerForm:username", "Username already exists.");
            LOGGER.warning("Username already exists.");
            hasError = true;
        }

        // kiểm tra password
        if (password == null || password.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:password", "Password is required.");
            LOGGER.warning("Password is empty.");
            hasError = true;
        } else if (!ValidationUtil.isValidPassword(password)) {
            FacesUtil.addFieldError("registerForm:password",
                    "Password must be at least 8 characters and contain both letters and numbers.");
            LOGGER.warning("Invalid password format.");
            hasError = true;
        }

        // kiểm tra số điện thoại
        if (phone == null || phone.trim().isEmpty()) {
            FacesUtil.addFieldError("registerForm:phone", "Phone number is required.");
            LOGGER.warning("Phone number is empty.");
            hasError = true;
        } else {
            String trimmedPhone = phone.trim();
            if (!ValidationUtil.isValidPhone(trimmedPhone)) {
                FacesUtil.addFieldError("registerForm:phone",
                        "Invalid phone number format (must be 10 digits starting with 0).");
                LOGGER.warning("Invalid phone number format.");
                hasError = true;
            } else if (usersFacade.isPhoneExists(trimmedPhone, null)) {
                FacesUtil.addFieldError("registerForm:phone", "Phone number is already in use.");
                LOGGER.warning("Phone number is already in use.");
                hasError = true;
            }
        }

        // kiểm tra email (nếu có nhập)
        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            if (!ValidationUtil.isValidEmail(trimmedEmail)) {
                FacesUtil.addFieldError("registerForm:email", "Invalid email format.");
                LOGGER.warning("Invalid email format.");
                hasError = true;
            } else if (usersFacade.isEmailExists(trimmedEmail, null)) {
                FacesUtil.addFieldError("registerForm:email", "Email is already in use.");
                LOGGER.warning("Email is already in use.");
                hasError = true;
            }
        }

        if (hasError) {
            return null;
        }

        // tạo request từ thông tin người dùng nhập
        RegisterRequest request = new RegisterRequest(
                username.trim(),
                password,
                fullName != null ? fullName.trim() : null,
                (email != null && !email.trim().isEmpty()) ? email.trim() : null,
                phone.trim());

        // thực hiện đăng ký thông qua service
        Users user = authService.register(request);

        if (user == null) {
            LOGGER.warning("Registration failed: Registration rejected by service.");
            FacesUtil.addErrorMessage("Registration failed due to a system error. Please try again.");
            return null;
        }

        LOGGER.info("User '" + username.trim() + "' registered successfully.");
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

package com.lens.controller;

import com.lens.entity.Users;
import com.lens.facade.UsersFacadeLocal;
import com.lens.util.FacesUtil;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "usersMB")
@SessionScoped
public class UserController implements Serializable {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9]).{8,255}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    private Users users = new Users();
    private boolean editMode;
    private String keyword = "";
    private String role = "";

    public UserController() {
    }

    // insert
    public String newUser() {
        users = new Users();
        users.setRole("CUSTOMER");
        users.setStatus("ACTIVE");
        editMode = false;
        return "form";
    }

    public String insertUser() {
        boolean hasError = false;

        if (isDuplicateUsername()) {
            hasError = true;
        }

        if (!isValidPassword()) {
            hasError = true;
        }

        if (!isValidPhone()) {
            hasError = true;
        } else if (isDuplicatePhone(null)) {
            hasError = true;
        }

        if (!isValidEmail()) {
            hasError = true;
        } else if (isDuplicateEmail(null)) {
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        try {
            Date now = new Date();
            users.setCreatedAt(now);
            users.setUpdatedAt(now);

            if (users.getStatus() == null || users.getStatus().trim().isEmpty()) {
                users.setStatus("ACTIVE");
            }

            usersFacade.create(users);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert",
                    "User created successfully.");
            return "list?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to create user.");
            return null;
        }
    }

    // open edit form
    public String editUser(Integer id) {
        users = usersFacade.find(id);
        if (users == null) {
            return "/404?faces-redirect=true";
        }
        editMode = true;
        return "form";
    }

    public String updateUser() {
        boolean hasError = false;

        if (!isValidPhone()) {
            hasError = true;
        } else if (isDuplicatePhone(users.getId())) {
            hasError = true;
        }

        if (!isValidEmail()) {
            hasError = true;
        } else if (isDuplicateEmail(users.getId())) {
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        try {
            users.setUpdatedAt(new Date());
            usersFacade.edit(users);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert",
                    "User updated successfully.");
            return "list?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to update user.");
            return null;
        }
    }

    // detail
    public String detailUser(Integer id) {
        users = usersFacade.find(id);
        if (users == null) {
            return "/404?faces-redirect=true";
        }
        return "detail";
    }

    // delete
    public void deleteUser(Integer id) {
        try {
            Users u = usersFacade.find(id);
            if (u != null) {
                usersFacade.remove(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // list user
    public List<Users> showAllUsers() {
        return usersFacade.findAll();
    }

    public List<Users> getUsersList() {
        return usersFacade.search(keyword, role);
    }

    // reset search
    public void resetFilter() {
        this.keyword = "";
        this.role = "";
    }

    // validate
    private boolean isDuplicateUsername() {
        if (usersFacade.isUsernameExists(users.getUsername())) {
            FacesUtil.addFieldError("userForm:username", "Username already exists.");
            return true;
        }
        return false;
    }

    private boolean isDuplicatePhone(Integer excludeId) {
        if (users.getPhone() != null && !users.getPhone().trim().isEmpty()) {
            if (usersFacade.isPhoneExists(users.getPhone().trim(), excludeId)) {
                FacesUtil.addFieldError("userForm:phone", "Phone number is already in use.");
                return true;
            }
        }
        return false;
    }

    private boolean isDuplicateEmail(Integer excludeId) {
        if (users.getEmail() != null && !users.getEmail().trim().isEmpty()) {
            if (usersFacade.isEmailExists(users.getEmail().trim(), excludeId)) {
                FacesUtil.addFieldError("userForm:email", "Email is already in use.");
                return true;
            }
        }
        return false;
    }

    public boolean isUsernameExists(String username) {
        return usersFacade.isUsernameExists(username);
    }

    public boolean isPhoneExists(String phone, Integer id) {
        return usersFacade.isPhoneExists(phone, id);
    }

    public boolean isEmailExists(String email, Integer id) {
        return usersFacade.isEmailExists(email, id);
    }

    private boolean isValidPassword() {
        if (users.getPassword() == null || !PASSWORD_PATTERN.matcher(users.getPassword()).matches()) {
            FacesUtil.addFieldError("userForm:password",
                    "Password must be at least 8 characters and contain both letters and numbers.");
            return false;
        }
        return true;
    }

    private boolean isValidPhone() {
        if (users.getPhone() == null || !PHONE_PATTERN.matcher(users.getPhone().trim()).matches()) {
            FacesUtil.addFieldError("userForm:phone",
                    "Invalid phone number format (must be 10 digits starting with 0).");
            return false;
        }
        return true;
    }

    private boolean isValidEmail() {
        if (users.getEmail() != null && !users.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(users.getEmail().trim()).matches()) {
                FacesUtil.addFieldError("userForm:email", "Invalid email format.");
                return false;
            }
        }
        return true;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTotalUsers() {
        return usersFacade.totalUsers();
    }

    public int getTotalAdminRole() {
        return usersFacade.totalAdminRole();
    }

    public int getTotalCustomerRole() {
        return usersFacade.totalCustomerRole();
    }

    public int getTotalAdmins() {
        return getTotalAdminRole();
    }

    public int getTotalCustomers() {
        return getTotalCustomerRole();
    }
}

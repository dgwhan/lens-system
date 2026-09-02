package com.lens.mbeans;

import com.lens.ebeans.Users;
import com.lens.sbeans.UsersFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
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
public class UsersMB implements Serializable {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9]).{8,255}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @jakarta.ejb.EJB
    private UsersFacadeLocal usersFacade;

    private Users users = new Users();
    private boolean editMode;
    private String keyword = "";
    private String role = "";

    public UsersMB() {
    }

    public String newUser() {
        users = new Users();
        users.setRole("CUSTOMER");
        users.setStatus("ACTIVE");
        editMode = false;
        return "form";
    }

    public String insertUser() {
        boolean hasError = false;

        if (usersFacade.isUsernameExists(users.getUsername())) {
            addFieldError("userForm:username", "Username already exists.");
            hasError = true;
        }

        if (users.getPassword() == null || !PASSWORD_PATTERN.matcher(users.getPassword()).matches()) {
            addFieldError("userForm:password", "Password must be at least 8 characters and contain both letters and numbers.");
            hasError = true;
        }

        if (users.getPhone() == null || !PHONE_PATTERN.matcher(users.getPhone().trim()).matches()) {
            addFieldError("userForm:phone", "Invalid phone number format (must be 10 digits starting with 0).");
            hasError = true;
        } else if (usersFacade.isPhoneExists(users.getPhone(), null)) {
            addFieldError("userForm:phone", "Phone number is already in use.");
            hasError = true;
        }

        if (users.getEmail() != null && !users.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(users.getEmail().trim()).matches()) {
                addFieldError("userForm:email", "Invalid email format.");
                hasError = true;
            } else if (usersFacade.isEmailExists(users.getEmail(), null)) {
                addFieldError("userForm:email", "Email is already in use.");
                hasError = true;
            }
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
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "User created successfully.");
            return "list?faces-redirect=true";

        } catch (Exception e) {
            addErrorMessage("Failed to create user.");
            return null;
        }
    }

    public String editUser(Integer id) {
        users = usersFacade.find(id);
        editMode = true;
        return "form";
    }

    public String detailUser(Integer id) {
        users = usersFacade.find(id);
        return "detail";
    }

    public String updateUser() {
        boolean hasError = false;

        if (users.getPhone() == null || !PHONE_PATTERN.matcher(users.getPhone().trim()).matches()) {
            addFieldError("userForm:phone", "Invalid phone number format (must be 10 digits starting with 0).");
            hasError = true;
        } else if (usersFacade.isPhoneExists(users.getPhone(), users.getId())) {
            addFieldError("userForm:phone", "Phone number is already in use.");
            hasError = true;
        }

        if (users.getEmail() != null && !users.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(users.getEmail().trim()).matches()) {
                addFieldError("userForm:email", "Invalid email format.");
                hasError = true;
            } else if (usersFacade.isEmailExists(users.getEmail(), users.getId())) {
                addFieldError("userForm:email", "Email is already in use.");
                hasError = true;
            }
        }

        if (hasError) {
            return null;
        }

        try {
            users.setUpdatedAt(new Date());
            usersFacade.edit(users);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "User updated successfully.");
            return "list?faces-redirect=true";

        } catch (Exception e) {
            addErrorMessage("Failed to update user.");
            return null;
        }
    }

    public void deleteUser(Integer id) {
        try {
            Users u = usersFacade.find(id);
            if (u != null) {
                usersFacade.remove(u);
            }
        } catch (Exception e) {
            // error
        }
    }

    private String sortOrder = "DESC";

    public List<Users> showAllUsers() {
        return usersFacade.findAll();
    }

    public List<Users> getUsersList() {
        return usersFacade.search(keyword, role, sortOrder);
    }

    public void toggleSortOrder() {
        if ("DESC".equalsIgnoreCase(this.sortOrder)) {
            this.sortOrder = "ASC";
        } else {
            this.sortOrder = "DESC";
        }
    }

    public void resetFilter() {
        this.keyword = "";
        this.role = "";
        this.sortOrder = "DESC";
    }

    private void addFieldError(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
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

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}

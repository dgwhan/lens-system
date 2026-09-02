package com.lens.sbeans;

import com.lens.ebeans.Users;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface UsersFacadeLocal {

    void create(Users users);

    void edit(Users users);

    void remove(Users users);

    Users find(Object id);

    List<Users> findAll();

    List<Users> findRange(int[] range);

    int count();
    
    List<Users> search(String keyword, String role);

    List<Users> search(String keyword, String role, String sortOrder);

    boolean isUsernameExists(String username);

    boolean isEmailExists(String email, Integer id);

    boolean isPhoneExists(String phone, Integer id);

}

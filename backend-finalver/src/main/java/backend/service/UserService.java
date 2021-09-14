package backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import backend.dao.UserDAO;
import backend.pojo.User;

class UserServiceException extends Exception {
    UserServiceException(String s) {
        super(s);
    }
}

@Service
public class UserService {
    private UserDAO userDAO;
    private SecretService secretService;

    @Autowired
    public UserService(UserDAO userDAO, SecretService secretService) {
        this.userDAO = userDAO;
        this.secretService = secretService;
    }

    public String getUserToken(String username, String password) throws UserServiceException {
        User user = userDAO.findUserByUsername(username);
        if (user == null) {
            throw new UserServiceException("用户不存在");
        }
        if (!password.equals(user.getPasskeys())) {
            throw new UserServiceException("密码错误");
        }
        return secretService.string2JWTtoken(username);
    }

    private void checkUsernameLength(String username) throws UserServiceException {
        if (username.length() < 2 || username.length() > 20) {
            throw new UserServiceException("用户名过长或过短");
        }
    }

    private void checkPasswordLength(String password) throws UserServiceException {
        if (password.length() < 8 || password.length() > 16) {
            throw new UserServiceException("密码过长或过短");
        }
    }

    public void tryRegister(String username, String password) throws UserServiceException {
        checkUsernameLength(username);
        checkPasswordLength(password);
        User user = userDAO.findUserByUsername(username);
        if (user != null) {
            throw new UserServiceException("用户名已存在");
        }
        user = new User();
        user.setUsername(username);
        user.setPasskeys(password);
        userDAO.save(user);
    }

    public void changePassword(String jwtToken,
                               String oldPassword,
                               String newPassword) throws UserServiceException {
        String username;
        try {
            username = secretService.jwtToken2String(jwtToken);
        } catch (Exception e) {
            throw new UserServiceException("登录过期或者token错误");
        }
        User user = userDAO.findUserByUsername(username);
        if (user == null) {
            throw new UserServiceException("用户不存在");
        }
        if (!oldPassword.equals(user.getPasskeys())) {
            throw new UserServiceException("旧密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw new UserServiceException("新密码不能与旧密码相同");
        }
        checkPasswordLength(newPassword);
        user.setPasskeys(newPassword);
        userDAO.save(user);
    }

    public void checkToken(String jwtToken) throws UserServiceException {
        String username;
        try {
            username = secretService.jwtToken2String(jwtToken);
        } catch (Exception e) {
            throw new UserServiceException("登录过期或者token错误");
        }
        if (userDAO.findUserByUsername(username) == null) {
            throw new UserServiceException("用户不存在");
        }
    }

    public int getUserID(String jwtToken) throws UserServiceException {
        String username;
        try {
            username = secretService.jwtToken2String(jwtToken);
        } catch (Exception e) {
            throw new UserServiceException("登录过期或者token错误");
        }
        User user = userDAO.findUserByUsername(username);
        if (user == null) {
            throw new UserServiceException("用户不存在");
        }
        return user.getId();
    }
}

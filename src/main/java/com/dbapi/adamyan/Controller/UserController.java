package com.dbapi.adamyan.Controller;


import com.dbapi.adamyan.DAO.UserDAO;
import com.dbapi.adamyan.Model.Message;
import com.dbapi.adamyan.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;
import java.util.List;

@Controller
@RequestMapping("/api/user")
public class UserController {
    private UserDAO userDAO;

    @Autowired
    UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Создание пользователя
    @PostMapping(path = "/{nickname}/create")
    public ResponseEntity create(@PathVariable(name = "nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);

        try {
            userDAO.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (DuplicateKeyException e) {
            List<User> result = userDAO.getDublicateUsers(user);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
    }

    // Получение информации о пользователе
    @GetMapping(path = "/{nickname}/profile")
    public ResponseEntity getProfile(@PathVariable(name = "nickname") String nickname) {
          User user = userDAO.getUserByNickname(nickname);
          if (user == null)
              return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user with nickname " + nickname));
          return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    // Обновление информации о пользователе
    @PostMapping(path = "/{nickname}/profile")
    public ResponseEntity updateUser(@PathVariable(name = "nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);
        try {
            User test = userDAO.getUserByNickname(nickname);
            if (test == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Can't find user with nickname " + nickname));
            userDAO.updateUser(user);
            User result = userDAO.getUserByNickname(nickname);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Not unique email"));
        }
    }
}
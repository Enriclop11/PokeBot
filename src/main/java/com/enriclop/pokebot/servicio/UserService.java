package com.enriclop.pokebot.servicio;

import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.repositorio.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameLike(username);
    }

    public User getUserByTwitchId(String twitchId) {
        return userRepository.findByTwitchIdLike(twitchId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).get();
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }
}

package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.User;
import com.isimm.Projet_Lazher.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public User saveUserWithPassword(User user, String password) {
        // First save the user to get the ID
        user = userRepository.save(user);
        
        // Store password in user_credentials table
        entityManager.createNativeQuery(
            "INSERT INTO user_credentials (user_id, password) VALUES (?, ?)")
            .setParameter(1, user.getId())
            .setParameter(2, password)
            .executeUpdate();
            
        return user;
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean checkPassword(User user, String password) {
        String storedPassword = (String) entityManager.createNativeQuery(
            "SELECT password FROM user_credentials WHERE user_id = ?")
            .setParameter(1, user.getId())
            .getSingleResult();
            
        return password.equals(storedPassword);
    }
}

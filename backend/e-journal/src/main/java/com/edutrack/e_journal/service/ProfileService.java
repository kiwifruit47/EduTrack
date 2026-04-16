package com.edutrack.e_journal.service;

import com.edutrack.e_journal.dto.ChangePasswordRequest;
import com.edutrack.e_journal.dto.UserDto;
import com.edutrack.e_journal.entity.User;
import com.edutrack.e_journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService    userService;

    public UserDto getProfile(UserDetails principal) {
        return userService.toUserDto(userService.resolveUser(principal));
    }

    public void changePassword(UserDetails principal, ChangePasswordRequest req) {
        User user = userService.resolveUser(principal);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public void uploadPicture(UserDetails principal, MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        User user = userService.resolveUser(principal);
        user.setProfilePicture(file.getBytes());
        user.setProfilePictureType(file.getContentType());
        userRepository.save(user);
    }

    public UserDto updateBio(UserDetails principal, String bio) {
        String stripped = bio.strip();
        if (stripped.length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bio must be 500 characters or fewer");
        User user = userService.resolveUser(principal);
        user.setBio(stripped.isEmpty() ? null : stripped);
        userRepository.save(user);
        return userService.toUserDto(user);
    }

    public void deletePicture(UserDetails principal) {
        User user = userService.resolveUser(principal);
        user.setProfilePicture(null);
        user.setProfilePictureType(null);
        userRepository.save(user);
    }
}

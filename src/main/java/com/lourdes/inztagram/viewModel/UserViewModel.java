package com.lourdes.inztagram.viewModel;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.FileUploadDetailRequest;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserLoginMapping;
import com.lourdes.inztagram.repository.FileUploadDetailRepository;
import com.lourdes.inztagram.repository.UserDetailsRepository;
import com.lourdes.inztagram.repository.UserLoginMappingRepository;
import com.lourdes.inztagram.utility.Hashing;

public class UserViewModel {
    private final String FOLDER_PATH = "/Users/lourdes/Downloads/Server-AttachedFilsSystem/";

    public String getRegistrationValidationStatusString(RegistrationValidationStatus status) {
        switch (status) {
            case GOOD_TO_GO:
                return "";
            case EMAIL_INVALID:
                return "Pleae Enter Proper Email ID";
            case EMAIL_EMPTY:
                return "Pleae Enter Email ID";
            case NAME_EMPTY:
                return "Pleae Enter Name";
            case USERNAME_EMPTY:
                return "Pleae Enter UserName";
            case USERNAME_INVALID:
                return "Pleae Enter Valid UserName";
            case PASSWORD_EMPTY:
                return "Pleae Enter Password";
            case PASSWORD_SHORT:
                return "Pleae Enter Strong Password";
            default:
                return "";
        }
    }

    public RegistrationValidationStatus validateRegistrationFields(UserDetails userDetails) {
        if(userDetails.getUserName() == null) { return RegistrationValidationStatus.USERNAME_EMPTY; }
        if(!userDetails.getUserName().matches("(?=[a-zA-Z0-9._]{3,20}$)(?!.*[_.]{2})[^_.].*[^_.]$")) { return RegistrationValidationStatus.USERNAME_INVALID; }
        if(userDetails.getEmailId() == null) { return RegistrationValidationStatus.EMAIL_EMPTY; }
        if(!userDetails.getEmailId().matches("[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) { return RegistrationValidationStatus.EMAIL_INVALID; }
        if(userDetails.getFullName() == null) { return RegistrationValidationStatus.NAME_EMPTY; }
        if(userDetails.getPassword() == null) { return RegistrationValidationStatus.PASSWORD_EMPTY; }
        if(userDetails.getPassword().length() < 6) { return RegistrationValidationStatus.PASSWORD_SHORT; }
        return RegistrationValidationStatus.GOOD_TO_GO;
    }

    public Boolean doesUserExist(String userName, UserDetailsRepository userDetailsRepository) {
        return userDetailsRepository.findById(userName).isPresent();
    }

    public void registerUser(UserDetails userDetails, UserDetailsRepository userDetailsRepository) {
        try {
            String hashedPassword = Hashing.hashSha256(userDetails.getPassword());
            userDetails.setPassword(hashedPassword);
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
        }
        userDetailsRepository.save(userDetails);
    }

    public Optional<String> loginUserAndGetSecretKey(String userName, String passwordUnhashed, UserDetailsRepository userDetailsRepository, UserLoginMappingRepository userLoginMappingRepository) {
        String uuidString = UUID.randomUUID().toString();
        String password;
        try {
            password = Hashing.hashSha256(passwordUnhashed);
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }

        // Check if user exists
        Optional<UserDetails> userDetailsOptional = userDetailsRepository.findById(userName);
        if(userDetailsOptional.isEmpty()) {
            return null;
        }
        // Validate password
        UserDetails userDetails = userDetailsOptional.get();
        if(!userDetails.getPassword().equals(password)) {
            return null;
        }
        
        // Remove previous login
        if(userDetails.getUuid() != null) {
            userLoginMappingRepository.deleteById(userDetails.getUuid());
        }
        userDetails.setUuid(uuidString);
        userDetailsRepository.save(userDetails);

        UserLoginMapping userLoginMapping = new UserLoginMapping();
        userLoginMapping.setUsername(userName);
        userLoginMapping.setUuid(uuidString);
        userLoginMappingRepository.save(userLoginMapping);
        return Optional.ofNullable(uuidString);
    }

    public Boolean isUseLoggedIn(String userId, UserLoginMappingRepository userLoginMappingRepository) {
        return userLoginMappingRepository.findById(userId).isPresent();
    }

    public String getUserNameForId(String userId, UserLoginMappingRepository userLoginMappingRepository) {
        Optional<UserLoginMapping> loginMapping = userLoginMappingRepository.findById(userId);
        if(loginMapping.isPresent()) {
            return loginMapping.get().getUsername();
        } else {
            return null;
        }
    }

    public String saveImageToFileSystem(MultipartFile file, String randomIDString) {
        if(file == null) { return null;}
        String filePath = FOLDER_PATH + randomIDString + ".jpeg";
        try {
            file.transferTo(new File(filePath));
        } catch( Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }
        return filePath;
    }

    public void saveImageUploadInfoInDatabase(FileUploadDetailRequest fileUploadDetailRequest, FileUploadDetailRepository fileUploadDetailRepository) {
        fileUploadDetailRepository.save(fileUploadDetailRequest);
    }
}

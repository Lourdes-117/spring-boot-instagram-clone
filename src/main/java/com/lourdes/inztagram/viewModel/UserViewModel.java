package com.lourdes.inztagram.viewModel;

import java.util.Optional;
import java.util.UUID;

import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserLoginMapping;
import com.lourdes.inztagram.repository.UserDetailsRepository;
import com.lourdes.inztagram.repository.UserLoginMappingRepository;

public class UserViewModel {

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
        userDetailsRepository.save(userDetails);
    }

    public Optional<String> loginUserAndGetSecretKey(String userName, String password, UserDetailsRepository userDetailsRepository, UserLoginMappingRepository userLoginMappingRepository) {
        String uuidString = UUID.randomUUID().toString();
        // Check if user exists
        Optional<UserDetails> userDetailsOptional = userDetailsRepository.findById(userName);
        if(userDetailsOptional.isEmpty()) {
            return null;
        }
        System.out.print("dsfdsf");
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
}

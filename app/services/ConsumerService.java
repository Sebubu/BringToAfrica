package services;

import models.User;
import org.mindrot.jbcrypt.BCrypt;

public class ConsumerService {

    public static User getConsumerByEmail(String email){
        if (email == null) {
            return null;
        }
        return User.find.where().like("email", email.toLowerCase()).findUnique();
    }

    public static boolean isValid(String email, String password) {
        User user = getConsumerByEmail(email);
        return user != null && BCrypt.checkpw(password, user.getPasswordHashedSalted());
    }
    public static void logIn(String email){
        play.mvc.Controller.session().clear();
        play.mvc.Controller.session("email", email);
    }

    public static boolean changePassword(User user, String oldPassword, String newPassword){
        if (isValid(user.getEmail(), oldPassword)) {
            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPasswordHashedSalted(hash);
            user.save();
            return true;
        } else {
            return false;
        }
    }

    public static boolean validatePasswords(String password1, String password2){
        String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!]).{8,20})";
        return password1.equals(password2) && !password1.isEmpty() && password1.matches(pattern);
    }
}

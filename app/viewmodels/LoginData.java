package viewmodels;

import play.data.validation.Constraints;
import services.ConsumerService;

public class LoginData {

    @Constraints.Required(message = "Bitte gib eine Emailadresse an.")
    @Constraints.Email(message = "Bitte gib eine gültige Emailadresse an.")
    public String email;
    @Constraints.Required(message = "Bitte gib eine Passwort an.")
    public String password;

    public String validate() {
        if (!ConsumerService.isValid(email.toLowerCase(), password)) {
            return "Email oder Passwort ist falsch.";
        }
        return null;
    }
}

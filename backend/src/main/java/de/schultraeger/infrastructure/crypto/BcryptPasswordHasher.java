package de.schultraeger.infrastructure.crypto;

import de.schultraeger.application.port.out.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {
    private static final int LOG_ROUNDS = 12;

    @Override
    public String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    @Override
    public boolean verify(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}

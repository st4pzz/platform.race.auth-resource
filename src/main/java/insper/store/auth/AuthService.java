package insper.store.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import insper.store.account.AccountController;
import insper.store.account.AccountIn;
import insper.store.account.AccountOut;
import insper.store.account.LoginIn;

@Service
public class AuthService {

    @Autowired
    private AccountController accountController;

    @Autowired
    private JwtService jwtService;

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackRegister")
    public String register(Register in) {
        final String password = in.password().trim();
        if (null == password || password.isEmpty()) throw new IllegalArgumentException("Password is required");
        if (password.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters long");

        ResponseEntity<AccountOut> response = accountController.create(AccountIn.builder()
            .name(in.name())
            .email(in.email())
            .password(password)
            .build()
        );
        if (response.getStatusCode().isError()) throw new IllegalArgumentException("Invalid credentials");
        if (null == response.getBody()) throw new IllegalArgumentException("Invalid credentials");
        return response.getBody().id();
    }

    public String fallbackRegister(Register in, Throwable t) {
        return new String(); 
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "fallbackAuthenticate")
    public LoginOut authenticate(String email, String password) {
        ResponseEntity<AccountOut> response = accountController.login(LoginIn.builder()
            .email(email)
            .password(password)
            .build()
        );
        if (response.getStatusCode().isError()) throw new IllegalArgumentException("Invalid credentials");
        if (null == response.getBody()) throw new IllegalArgumentException("Invalid credentials");
        final AccountOut account = response.getBody();

        // Cria um token JWT
        final String token = jwtService.create(account.id(), account.name(), "regular");

        return LoginOut.builder()
            .token(token)
            .build();
    }

    public LoginOut fallbackAuthenticate(String email, String password, Throwable t) {
        return null;
    }

    public Token solve(String token) {
        return jwtService.getToken(token);
    }
}

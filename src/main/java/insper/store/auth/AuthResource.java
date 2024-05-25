package insper.store.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class AuthResource implements AuthController {

    @Autowired
    private AuthService authService;

    @Override
    public ResponseEntity<?> create(RegisterIn in) {

        final String id = authService.register(Register.builder()
            .name(in.name())
            .email(in.email())
            .password(in.password())
            .build()
        );

        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri())
            .build();
    }

    @Override
    public ResponseEntity<LoginOut> authenticate(CredentialIn in) {
        return ResponseEntity.ok(authService.authenticate(in.email(), in.password()));
    }

    @Override
    public ResponseEntity<SolveOut> solve(SolveIn in) {
        final Token token = authService.solve(in.token());
        return ResponseEntity.ok(
            SolveOut.builder()
                .id(token.id())
                .name(token.name())
                .role(token.role())
                .build()
        );
    }

}

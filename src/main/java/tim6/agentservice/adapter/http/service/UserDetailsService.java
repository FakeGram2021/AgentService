package tim6.agentservice.adapter.http.service;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tim6.agentservice.domain.model.User;
import tim6.agentservice.domain.service.UserService;

@Service
@Primary
public class UserDetailsService
        implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String userId) throws UsernameNotFoundException {
        final Optional<User> optionalUser = this.userService.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(
                    String.format("There is no user with id: %s", userId));
        }

        final User user = optionalUser.get();
        return new org.springframework.security.core.userdetails.User(
                user.getId(), user.getId(), new ArrayList<>());
    }
}

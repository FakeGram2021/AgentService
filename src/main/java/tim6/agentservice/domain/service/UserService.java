package tim6.agentservice.domain.service;

import java.util.Optional;
import tim6.agentservice.domain.model.User;

public interface UserService {

    Optional<User> findById(final String userId);

    void persistUser(final User user);

}

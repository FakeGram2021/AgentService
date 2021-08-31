package tim6.agentservice.adapter.elasticsearch.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim6.agentservice.adapter.elasticsearch.models.UserES;
import tim6.agentservice.adapter.elasticsearch.repository.UserRepository;
import tim6.agentservice.domain.model.User;

@Service
public class UserService implements tim6.agentservice.domain.service.UserService {

    final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(String userId) {
        Optional<UserES> userES = userRepository.findById(userId);
        return userES.map(UserES::to);
    }

    @Override
    public void persistUser(User user) {
        userRepository.save(UserES.from(user));
    }

}

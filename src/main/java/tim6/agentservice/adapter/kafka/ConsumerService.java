package tim6.agentservice.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tim6.agentservice.adapter.kafka.models.KafkaMessage;
import tim6.agentservice.adapter.kafka.models.payloads.PostHistoryPayload;
import tim6.agentservice.adapter.kafka.models.payloads.UserPayload;
import tim6.agentservice.domain.exceptions.EntityNotFoundException;
import tim6.agentservice.domain.model.User;
import tim6.agentservice.domain.service.UserService;

@Service
public class ConsumerService {

    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ConsumerService(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(topics = "agent_service_topic", groupId = "groupId", containerFactory = "kafkaMessageListener")
    public void receiveMessage(final KafkaMessage message) {
        final String key = message.getKey();

        switch (key) {
            case "USER_INFO":
                onReceiveUserInfo(message);
                break;
            case "POST_HISTORY":
                onReceivePostHistory(message);
                break;
        }
    }

    private void onReceiveUserInfo(final KafkaMessage message) {
        final UserPayload userPayload = objectMapper.convertValue(message.getValue(),
                UserPayload.class);
        Optional<User> optionalUser = userService.findById(userPayload.getId());

        final User user;
        if (optionalUser.isEmpty()) {
            user = new User(userPayload.getId(), userPayload.getSex(),
                    userPayload.getBirthYear(), new ArrayList<>());
        } else {
            user = optionalUser.get();
            user.setBirthYear(userPayload.getBirthYear());
            user.setSex(userPayload.getSex());
        }

        userService.persistUser(user);
    }

    private void onReceivePostHistory(final KafkaMessage message) {
        final PostHistoryPayload postHistoryPayload = objectMapper.convertValue(message.getValue(),
                PostHistoryPayload.class);
        final Optional<User> optionalUser = userService.findById(postHistoryPayload.getUserId());
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException();
        }

        final User user = optionalUser.get();
        final List<String> postedTags = user.getPostedTagsHistory();
        postedTags.addAll(postHistoryPayload.getPostHistory());

        user.setPostedTagsHistory(postedTags);
        userService.persistUser(user);
    }
}

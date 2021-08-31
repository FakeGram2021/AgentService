package tim6.agentservice.domain.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {


    public EntityAlreadyExistsException() {
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}

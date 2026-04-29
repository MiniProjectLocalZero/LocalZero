package se.mau.localzero.messaging.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
public class MessageCommandInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MessageCommandInvoker.class);
    private final Stack<MessageCommand> history = new Stack<>();

    public boolean execute(MessageCommand messageCommand) {
        boolean result = messageCommand.execute();

        if (result) {
            history.push(messageCommand);
        }
        return result;
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        MessageCommand command = history.pop();

        try {
            command.undo();
            return true;
        } catch (Exception e) {
            logger.error("Failed to undo command: {} - error: {}", command.getDescription(), e.getMessage());
            return false;
        }
    }
}

package se.mau.localzero.messaging.command;

/**
 * Interface for all commands that can be executed on the message bus.
 * Base interface for all command implementations.
 *
 * Commands that support undo should implement UndoableCommand instead of this interface to follow Liskov substitution principle.
 */
public interface MessageCommand {

    /**
     * Execute the command
     * @return true if the command was executed successfully, false otherwise
     */
    boolean execute();

    /**
     * Undo the command and restore previous state
     */
    void undo();

    /**
     * Get a description of what this command does
     * @return A description of what this command does
     */
    String getDescription();
}

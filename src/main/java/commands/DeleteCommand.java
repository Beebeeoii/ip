package commands;

import model.Task;
import utils.InputValidator;
import view.Printable;

/**
 * Represents a command which deletes a <code>Task</code>.
 */
public class DeleteCommand extends Command {
    /**
     * Generates a <code>DeleteCommand</code> object.
     *
     * @param input User input into the application.
     * @param ui A Printable object used for UI display.
     */
    public DeleteCommand(String input, Printable ui) {
        super(input, ui);
    }

    /**
     * Deletes a <code>Task</code>.
     */
    @Override
    public void execute() {
        if (InputValidator.isCheckInputValid(this.input)) {
            int taskId = Integer.parseInt(input.split(" ")[1]);
            try {
                Task deletedTask = Task.delete(taskId);
                this.ui.printIndent("Deleted! The task has been deleted:");
                this.ui.printIndent(deletedTask.toString());

                this.ui.printIndent("");
                new ListCommand(this.ui).execute();
            } catch (IndexOutOfBoundsException e) {
                this.ui.printlnError("Invalid Task ID!");
            }
        } else {
            this.ui.printlnError("Invalid Syntax - \"delete [Task ID]\" (e.g. \"delete 1\")");
        }
    }
}

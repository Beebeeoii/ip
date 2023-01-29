package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;

import storage.Outputable;

/**
 * Represents a <code>Task</code> object.
 */
public abstract class Task {
    protected static final String EMPTY = "~";
    private static LinkedList<Task> tasks = new LinkedList<>();

    private final String title;
    private TaskStatus status;

    /**
     * Generates a <code>Task</code> object.
     *
     * @param title The title of the task to be completed.
     */
    protected Task(String title) {
        this.title = title;
        this.status = TaskStatus.NEW;
        Task.tasks.add(this);
    }

    public static boolean isIdValid(int id) {
        return id >= 1 && id <= Task.tasks.size();
    }

    public static void setStatusCompleted(int id) throws IndexOutOfBoundsException {
        if (!Task.isIdValid(id)) {
            throw new IndexOutOfBoundsException();
        }

        Task.tasks.get(id - 1).setStatus(TaskStatus.COMPLETED);
    }

    public static void setStatusNew(int id) throws IndexOutOfBoundsException {
        if (!Task.isIdValid(id)) {
            throw new IndexOutOfBoundsException();
        }

        Task.tasks.get(id - 1).setStatus(TaskStatus.NEW);
    }

    /**
     * Deletes a <code>Task</code> from the list of tasks with its <code>id</code>.
     *
     * @param id The <code>id</code> of the <code>Task</code> to be deleted.
     * @return The deleted <code>Task</code> object.
     * @throws IndexOutOfBoundsException If the <code>id</code> provided is not within range.
     */
    public static Task delete(int id) throws IndexOutOfBoundsException {
        if (!Task.isIdValid(id)) {
            throw new IndexOutOfBoundsException();
        }

        return Task.tasks.remove(id - 1);
    }

    /**
     * Deletes the last <code>Task</code> from the list.
     * @return The deleted <code>Task</code> object.
     * @throws IndexOutOfBoundsException If the list of tasks is empty.
     */
    public static Task deleteLast() throws IndexOutOfBoundsException {
        return Task.tasks.removeLast();
    }

    private void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Retrieves all <code>Task</code> in the task list and returns their
     * titles as a string array.
     *
     * @return A string array of all the <code>Task</code> titles.
     */
    public static String[] listAll() {
        ArrayList<String> tasks = new ArrayList<>();
        for (int i = 0; i < Task.tasks.size(); ++i) {
            tasks.add(String.format("%s", Task.tasks.get(i).toString()));
        }
        return tasks.toArray(new String[Task.tasks.size()]);
    }

    /**
     * Retrieves a <code>Task</code> title via its <code>id</code>.
     *
     * @param id The <code>id</code> of the <code>Task</code> to be retrieved.
     * @return The title of the specified <code>Task</code>.
     * @throws IndexOutOfBoundsException If the <code>id</code> is invalid.
     */
    public static String listOne(int id) throws IndexOutOfBoundsException {
        if (!Task.isIdValid(id)) {
            throw new IndexOutOfBoundsException();
        }

        return Task.tasks.get(id - 1).toString();
    }

    private String printStatus() {
        switch (this.status) {
        case NEW:
            return " ";
        case COMPLETED:
            return "X";
        default:
            // Should not reach here
            return "?";
        }
    }

    /**
     * Writes the lists of <code>Task</code> objects to an <code>Outputable</code>.
     *
     * @param out An <code>Outputable</code> object that handles saving of <code>Task</code> objects.
     * @throws IOException If some sort of IO error occurs during the process of writing.
     */
    public static void save(Outputable out) throws IOException {
        Base64.Encoder e = Base64.getEncoder();
        StringBuilder sb = new StringBuilder();
        for (Task t : Task.tasks) {

            String s = String.format("%s | %s | %s | %s | %s | %s",
                    e.encodeToString(t.getTaskType().toString().getBytes(StandardCharsets.UTF_8)),
                    e.encodeToString(t.title.getBytes(StandardCharsets.UTF_8)),
                    e.encodeToString(t.status.toString().getBytes(StandardCharsets.UTF_8)),
                    e.encodeToString(t.getDeadline().getBytes(StandardCharsets.UTF_8)),
                    e.encodeToString(t.getStartDateTime().getBytes(StandardCharsets.UTF_8)),
                    e.encodeToString(t.getEndDateTime().getBytes(StandardCharsets.UTF_8)));
            sb.append(s).append("\n");
        }

        out.write(sb.toString());
    }

    /**
     * Reads and decodes an array of <code>Task</code> data to be used by Membot.
     *
     * @param in The array of data to be loaded.
     */
    public static void load(ArrayList<String> in) {
        Base64.Decoder d = Base64.getDecoder();
        for (String s : in) {
            String[] split = s.split(" \\| ");

            byte[] taskType = d.decode(split[0]);
            byte[] title = d.decode(split[1]);
            byte[] status = d.decode(split[2]);
            byte[] deadline = d.decode(split[3]);
            byte[] startDateTime = d.decode(split[4]);
            byte[] endDateTime = d.decode(split[5]);

            Task restoredTask = null;
            switch (TaskType.valueOf(new String(taskType, StandardCharsets.UTF_8))) {
            case TODO:
                restoredTask = new ToDo(new String(title, StandardCharsets.UTF_8));
                break;
            case DEADLINE:
                restoredTask = new Deadline(new String(title, StandardCharsets.UTF_8),
                        new String(deadline, StandardCharsets.UTF_8));
                break;
            case EVENT:
                restoredTask = new Event(new String(title, StandardCharsets.UTF_8),
                        new String(startDateTime, StandardCharsets.UTF_8),
                        new String(endDateTime, StandardCharsets.UTF_8));
                break;
            default:
                break;
            }

            restoredTask.setStatus(TaskStatus.valueOf(new String(status, StandardCharsets.UTF_8)));
        }
    }

    /**
     * Finds all <code>Task</code> that has titles that contains the specified keyword.
     *
     * @param keyword The keyword to be used to find <code>Task</code>.
     * @return The list of <code>Task</code> objects that have titles containing the keyword.
     */
    public static ArrayList<Task> find(String keyword) {
        ArrayList<Task> res = new ArrayList<>();
        for (Task t : Task.tasks) {
            if (t.title.contains(keyword)) {
                res.add(t);
            }
        }

        return res;
    }

    /**
     * Returns the <code>Task</code> type.
     *
     * @return The <code>Task</code> type.
     */
    public abstract TaskType getTaskType();

    /**
     * Returns the deadline attached to the <code>Task</code>.
     *
     * @return The deadline attached to the <code>Task</code>.
     */
    public abstract String getDeadline();

    /**
     * Returns the start dateTime attached to the <code>Task</code>.
     *
     * @return The start dateTime attached to the <code>Task</code>.
     */
    public abstract String getStartDateTime();

    /**
     * Returns the end dateTime attached to the <code>Task</code>.
     *
     * @return The end dateTime attached to the <code>Task</code>.
     */
    public abstract String getEndDateTime();

    /**
     * Returns a <code>String</code> representation of the <code>Task</code>.
     *
     * @return A <code>String</code> representation of the <code>Task</code>.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", this.printStatus(), this.title);
    }
}

enum TaskStatus {
    NEW,
    COMPLETED
}

enum TaskType {
    TODO,
    DEADLINE,
    EVENT
}

package model;

public class Task {

    private String taskID;
    private String taskName;
    private String status;
    private Worker assignedWorker;

    public Task(String taskID, String taskName, String deadline, int priority) {

        this.taskID = taskID;
        this.taskName = taskName;
        this.status = "Pending";

        if (deadline == null || deadline.trim().isEmpty()) {
            throw new IllegalArgumentException("Deadline cannot be empty.");
        }

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5.");
        }
    }

    public String getTaskID(){
        return taskID;
    }

    public String getTaskName(){
        return taskName;
    }


    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void assignWorker(Worker worker){

        this.assignedWorker = worker;

    }

    public Worker getAssignedWorker(){
        return assignedWorker;
    }
}

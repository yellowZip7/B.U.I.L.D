package model;

public class Worker {

    private String workerID;
    private String workerName;
    private String position;
    private String username;
    private String password;

    public Worker(String workerID, String workerName, String position, String username, String password){

        this.workerID = workerID;
        this.workerName = workerName;
        this.position = position;
        this.username = username;
        this.password = password;

    }

    public String getWorkerName(){
        return workerName;
    }


    public String getEmail() {
        return username;
    }

    public String getWorkerID(){
        return workerID;
    }
    public String getPosition() {
        return position;
    }

    public String getPassword() {
        return password;
    }
}


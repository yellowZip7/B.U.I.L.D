package model;


public class Project {
    public Resource[] resources = new Resource[100];
    public int resourceCount = 0;

    private String projectId;
    private String projectName;
    private String location;
    private String startDate;
    private String endDate;
    private double budget;
    private String status;
    public Task[] tasks = new Task[100];
    public int taskCount = 0;


    public Project(String projectId, String projectName, String location, String startDate, String endDate, double budget){
        this.projectId = projectId;
        this.projectName = projectName;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = "Ongoing";
    }
    public void setProjectName(String projectName){
        this.projectName = projectName;
    }

    public void setBudget(double budget){
        this.budget = budget;
    }
    public void setLocation(String location){
        this.location = location;
    }


    public String getProjectId(){
        return projectId;
    }
    public String getProjectName(){
        return projectName;
    }
    public double getBudget(){
        return budget;
    }
    public String getLocation(){
        return location;
    }
    public String getStatus(){
        return status;
    }
    public String getStartDate(){
        return startDate;
    }
    public String getEndDate(){
        return endDate;
    }


    public static void displayProject(Project p){

        System.out.println("\n+--------------------------------------+");
        System.out.println("|          PROJECT DETAILS             |");
        System.out.println("+--------------------------------------+");

        System.out.printf("| ID: %-32s |\n", p.getProjectId());
        System.out.printf("| Name: %-30s |\n", p.getProjectName());
        System.out.printf("| Budget: %-28.2f |\n", p.getBudget());
        System.out.printf("| Status: %-28s |\n", p.getStatus());

        System.out.println("+--------------------------------------+");
    }

    public void addTask(Task task){

        if (taskCount >= tasks.length) {
            System.out.println("Task limit reached. Cannot add more tasks.");
            return;
        }

        tasks[taskCount] = task;
        taskCount++;

    }


}

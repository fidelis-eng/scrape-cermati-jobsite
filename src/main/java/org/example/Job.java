package org.example;

import java.util.List;

public class Job {
    private final String title;
    private final String location;
    private final List<String> description;
    private final List<String> qualification;
    private final String job_type;
    public Job(String title, String location, List<String> description, List<String> qualification, String job_type ) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.qualification = qualification;
        this.job_type = job_type;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getQualification() {
        return qualification;
    }

    public String getJob_type() {
        return job_type;
    }

}

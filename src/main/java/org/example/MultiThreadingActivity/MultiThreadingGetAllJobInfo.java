package org.example.MultiThreadingActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;

public class MultiThreadingGetAllJobInfo implements Runnable{
    private final List<String[]> linksList;
    private final Map<String, ArrayList<Map<String, Object>>> jobInfosMap;
    private final MyCallback myCallback;
    public MultiThreadingGetAllJobInfo(List<String[]> linksList, List<String> deptNamesList, MultiThreadingGetAllJobInfo.MyCallback myCallback) {
        this.linksList = linksList;
//      initialize structure hashmap for the final result
        this.jobInfosMap = new HashMap<>();
        for(String dept:deptNamesList){
            this.jobInfosMap.put(dept, new ArrayList<>());
        }
        this.myCallback = myCallback;
    }
//  callback to process the final result from each thread
    public interface MyCallback {
        public void onSuccess(Map<String, ArrayList<Map<String, Object>>> result);
        public void onError();
    }

    public void run(){
//      initialize driver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        System.out.println("visiting each job posting");
//      iterate each links
        for(String[] info : this.linksList) {
            String jobTitle = "";
            String jobLocation = "";
            String jobType = "";
            // initialize list to save job description
            List<String> jobDescriptionsList = new ArrayList<>();
            //initialize list to save job qualification
            List<String> qualificationList = new ArrayList<>();
            String jobDepartment = info[0]; // get job department
            String jobUrl = info[1]; // get job url

            try{
//              navigate to the webpage
                driver.get(jobUrl);

                Thread.sleep(4000); // wait time to load

//              access element job title, job location, and job type to access the text
                jobTitle = driver.findElement(By.cssSelector("h1.job-title")).getText();
                jobLocation = driver.findElement(By.tagName("spl-job-location")).getAttribute("formattedaddress");
                jobType = driver.findElement(By.cssSelector("ul.job-details > li.job-detail")).getText();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // access to job description
            try {
                WebElement wysiwygjobDesc = driver.findElement(By.cssSelector("#st-jobDescription > div.wysiwyg"));
                List<WebElement> pJobDesc = wysiwygjobDesc.findElements(By.cssSelector("p")); // access tag names p
                List<WebElement> ulJobDesc = wysiwygjobDesc.findElements(By.cssSelector("ul")); // access tag name ul
                List<WebElement> olJobDesc = wysiwygjobDesc.findElements(By.cssSelector("ol")); // access tag name ol

//              iterate each <ul>
                for(WebElement ul: ulJobDesc){
                    List<WebElement> ulliJobDesc = ul.findElements(By.cssSelector("li")); // access tag name li
//                  Iterate over each <li> element and save it to list
                    for (WebElement li : ulliJobDesc) {
                        if (!"".equals(li)) jobDescriptionsList.add(li.getText());
                    }
                }
//              iterate each paragraph
                for(WebElement p : pJobDesc){
                    String ptext = p.getText();
                    if (ptext.contains("\n")){
                        String[] words = p.getText().split("\n");

                        jobDescriptionsList.addAll(Arrays.asList(words));
                        continue;
                    }
                    if (!"".equals(ptext)) jobDescriptionsList.add(ptext);
                }
//              iterate each ordered list
                for(WebElement ol : olJobDesc){
                    List<WebElement> olliJobDesc = ol.findElements(By.cssSelector("li"));
//                  Iterate over each <li> element and save it to list
                    for (WebElement li : olliJobDesc) {
                        if (!"".equals(li)) jobDescriptionsList.add(li.getText());
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            }
            //access to job qualification
            try {
                WebElement wysiwygJobQualify = driver.findElement(By.cssSelector("#st-qualifications > div.wysiwyg"));
                List<WebElement> pJobQualify = wysiwygJobQualify.findElements(By.cssSelector("p")); // access tag names p
                List<WebElement> ulJobQualify = wysiwygJobQualify.findElements(By.cssSelector("ul")); // access tag name ul
                List<WebElement> olJobQualify = wysiwygJobQualify.findElements(By.cssSelector("ol")); // access tag name ol

//              iterate each <ul>
                for(WebElement ul: ulJobQualify){
                    List<WebElement> ulliJobQualify = ul.findElements(By.cssSelector("li")); // access tag name li
//                  Iterate over each <li> element and save it to list
                    for (WebElement li : ulliJobQualify) {
                        if (!"".equals(li)) qualificationList.add(li.getText());
                    }
                }
//              iterate each paragraph
                for(WebElement p : pJobQualify){
                    String ptext = p.getText();
                    if (ptext.contains("\n")){
                        String[] words = p.getText().split("\n");
                        qualificationList.addAll(Arrays.asList(words));
                        continue;
                    }
                    if (!"".equals(ptext)) qualificationList.add(ptext);
                }
//              iterate each ordered list
                for(WebElement ol : olJobQualify){
                    List<WebElement> olliJobQualify = ol.findElements(By.cssSelector("li"));
//                  Iterate over each <li> element and save it to list
                    for (WebElement li : olliJobQualify) {
                        if (!"".equals(li)) qualificationList.add(li.getText());
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
//              create job object
                Job job = new Job(jobTitle, jobLocation, jobDescriptionsList, qualificationList, jobType);
                Gson gson = new Gson();
//              intention to change job object to Map
                String jobJson = gson.toJson(job);
                Map<String, Object> jobMap = gson.fromJson(jobJson, new TypeToken<Map<String, Object>>() {}.getType());
//              update/add new jobObject to each department respectively
                ArrayList<Map<String, Object>> currentList = this.jobInfosMap.get(jobDepartment);
                currentList.add(jobMap);

                this.jobInfosMap.put(jobDepartment, currentList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.myCallback.onSuccess(this.jobInfosMap);

        driver.quit();
    }

}


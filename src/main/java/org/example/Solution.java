package org.example;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Solution {

    public static void main(String[] args) {
        String url = "https://www.cermati.com/karir";

        // Initialize ChromeDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        // initialize scraping activity
        ScrapeActivity scr = new ScrapeActivity();

        // get viewAllJobs URL from the webpage
        System.out.println("To-do: navigating to the job vacancy webpage");
        String jobVacancyUrl = scr.NavigateJobVacancy(url, driver);
        System.out.println(jobVacancyUrl);

        // get all jobs urls
        int threadcount = 5;
        scr.setThreadcount(threadcount);
        System.out.println("To-do: getting all URLs job openings (threading process)");
        List<String[]> jobOpenings = scr.getAllUrlsJobOpenings(driver, jobVacancyUrl);
//        for(String[] el:jobOpenings){
//            System.out.println(el[0]);
//            System.out.println(el[1]);
//        }

//      get all job information through Url and map the result
        System.out.println("To-do: getting all job information (threading process)");
        Map<String, ArrayList<Map<String, Object>>> result = scr.getAllJobsInfo(jobOpenings);
//        for(String key : result.keySet()){
//            System.out.println("key: " + key + " value: ");
//            System.out.println(result.get(key));
//        }
//      save to json file
        System.out.println("Final: saving to json");
        try (Writer writer = new FileWriter("solution.json")) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(result, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        driver.quit();

    }
}
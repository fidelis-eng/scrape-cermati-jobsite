package org.example;

import org.example.MultiThreadingActivity.MultiThreadingGetAllJobInfo;
import org.example.MultiThreadingActivity.MultiThreadingGetAllUrlsJobOpenings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrapeActivity{
    private int threadcount = 0;
    private int finishedThreads = 0;
    private int numberpages = 0;
    private List<String> deptNamesList = new ArrayList<>();

    public int getThreadcount() {
        return threadcount;
    }

    public void setThreadcount(int threadcount) {
        this.threadcount = threadcount;
    }

    public int getNumberpages() {
        return numberpages;
    }

    public void setNumberpages(int numberpages) {
        this.numberpages = numberpages;
    }

    public String NavigateJobVacancy(String url, WebDriver driver){
        try {
            // Navigate to the webpage
            driver.get(url);

            // Wait for dynamic content to load (if necessary)
            Thread.sleep(2000); // Adjust the wait time as needed

            WebElement viewAllJobsElement = driver.findElement(By.cssSelector("#career-landing > div > div:nth-child(4) > div > a"));
            if (viewAllJobsElement != null) {
                return viewAllJobsElement.getAttribute("href");
            }
            System.out.println("Could not find viewAllJobs element");
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String[]> getAllUrlsJobOpenings(WebDriver driver, String jobVacancyUrl){
        try {
            driver.get(jobVacancyUrl);

            Thread.sleep(5000); // wait time to load

            Select departments = new Select(driver.findElement(By.cssSelector("#job-department.form-control")));
            List<WebElement> options = departments.getOptions();
            for (WebElement option : options) {
                if (!"All Department".equals(option.getText())){
                    this.deptNamesList.add(option.getText());
                }
            }
            // access element jobs pagination
            WebElement pageJobContainer = driver.findElement(By.xpath("//div[@class='page-job-container']/div"));

            // get pagination buttons
            WebElement careerPagination = pageJobContainer.findElement(By.cssSelector("div:nth-child(11) > div"));
            List<WebElement> arrowBtns = careerPagination.findElements(By.cssSelector("button.arrow-icon"));
            WebElement doubleRightBtn = arrowBtns.getLast();
//          visit to the last page
            doubleRightBtn.click();

            Thread.sleep(1000); // wait time to load
            // get the total of pages as the total pages
            int numberLastPage = Integer.parseInt(careerPagination.findElement(By.cssSelector("button.active")).getText());
            setNumberpages(numberLastPage);

            // assign each pages among available threads
            List<List<Integer>> pagesList = distributesPagesAmongThreads(numberLastPage, this.threadcount);
            // initialize list to save job urls and name job department
            ArrayList<String[]> jobOpenings = new ArrayList<>();
            // initialize list to hold threads
            List<Thread> threads = new ArrayList<>();
            this.finishedThreads = 0;

//          collect job URLs using MultiThreading
            for(int i = 0 ; i < this.threadcount ; i++){
                Thread th = new Thread(new MultiThreadingGetAllUrlsJobOpenings(numberLastPage, pagesList.get(i), jobVacancyUrl, new MultiThreadingGetAllUrlsJobOpenings.MyCallback() {
                    @Override
                    public synchronized void onSuccess(List<String[]> result) {
                        getAllJobThreadCompletion(result, jobOpenings);
                    }
                    @Override
                    public void onError() {

                    }
                }));
                th.start();
                threads.add(th);
            }
            // Wait for all threads to complete
            for (Thread th: threads) {
                try {
                    // Join each thread
                    String nameCurrentThread = th.getName(); // get name thread
                    th.join();
                    this.finishedThreads++; //count finished thread
                    System.out.println("Number of finished thread is "+this.finishedThreads);
                    System.out.println("thread name = "+ nameCurrentThread);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(2000);
            return jobOpenings;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, ArrayList<Map<String, Object>>> getAllJobsInfo(List<String[]> jobOpenings){
        try{
            // assign each links among available threads
            List<List<String[]>> linksList = distributesLinksAmongThreads(jobOpenings.size(), this.threadcount, jobOpenings);
            // initialize list to hold threads
            List<Thread> threads = new ArrayList<>();
            this.finishedThreads = 0;
            // initialize list to save all job information (final result)
            Map<String, ArrayList<Map<String, Object>>> jobInfos = new HashMap<>();

//          initialize structure hashmap for the final result
            for(String d : this.deptNamesList){
               jobInfos.put(d, new ArrayList<>());
            }

//          collect all job informations using MultiThreading
            for(int i = 0 ; i < threadcount ; i++){
                Thread th = new Thread(new MultiThreadingGetAllJobInfo(linksList.get(i), this.deptNamesList, new MultiThreadingGetAllJobInfo.MyCallback() {
                    @Override
                    public synchronized void onSuccess(Map<String, ArrayList<Map<String, Object>>> result) {
                        getAllJobsInfoThreadCompletion(result, jobInfos);
                    }
                    @Override
                    public void onError() {

                    }
                }));
                th.start();
                threads.add(th);
            }
            // Wait for all threads to complete
            for (Thread th: threads) {
                try {
                    // Join each thread
                    String nameCurrentThread = th.getName();
                    th.join();
                    this.finishedThreads++;
                    System.out.println("Number of finished thread is "+this.finishedThreads);
                    System.out.println("thread name = "+ nameCurrentThread);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Thread.sleep(5000);
            return jobInfos;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
//  this code for evenly assigns a given number of pages among a specified number of threads.
//  Example: If 5 threads are assigned to handle 10 pages, each thread receives 2 pages with any remainder
//  being equally distributed among the threads.
    public List<List<Integer>> distributesPagesAmongThreads(int numberPages, int threadCount){

        int pagenumber = 1; // nth of page
        int lengthNumberofAssignedPages = numberPages/threadCount; // the size of number assigned pages
        int remainderNumberofPages = numberPages % threadCount; // remainder from length of pages divide with number of threads
        int countRemainder = 1; // counter that the condition depends on remainderNumberofPages
        if (remainderNumberofPages == 0) {
            countRemainder = 0;
        }
        List<List<Integer>> pagesList = new ArrayList<>(); // initialize list for collect the assigned pages

//      iterate each thread
        for(int i = 0 ; i < threadCount ; i++){
//          initialize list for save assigned pages
            List<Integer> assignedPages = new ArrayList<>();
//          assigned pages for each thread
            for (int j = 0 ; j < lengthNumberofAssignedPages + countRemainder; j++){
                assignedPages.add(pagenumber);
                pagenumber++;
//              condition to countRemainder
                if (remainderNumberofPages == 0) {
                    countRemainder = 0;
                }
            }
            remainderNumberofPages--; //reduce the remainder
            pagesList.add(assignedPages);
        }
        return pagesList;
    }
    public List<List<String[]>> distributesLinksAmongThreads(int numberLinks, int threadCount, List<String[]> jobOpenings){

        int lengthNumberofAssignedLinks = numberLinks/threadCount; // the size of number assigned links
        int remainderNumberofLinks = numberLinks % threadCount; // remainder from length of jobOpenings divide with number of threads
        int countRemainder = 1; // as an adder that the condition depends on remainderNumberofLinks
        if (remainderNumberofLinks == 0){
            countRemainder = 0;
        }
        List<List<String[]>> linksList = new ArrayList<>(); // initialize list for collect the assigned links
        int index = 0; // start
        int counter = lengthNumberofAssignedLinks + countRemainder; // upper bound
//      iterate each thread
        for(int i = 0 ; i < threadCount ; i++){
//          initialize list for save assigned links
            List<String[]> assignedLinks = new ArrayList<>();
//          assigned links for each thread
            while (index < counter) {
                assignedLinks.add(jobOpenings.get(index));
                index++;
//              condition to countRemainder
                if (remainderNumberofLinks == 0) {
                    countRemainder = 0;
                }
            }
//          update counter to keep being upper bound
            counter += lengthNumberofAssignedLinks + countRemainder;
//          count down the remainder number of links
            remainderNumberofLinks--;
            linksList.add(assignedLinks);
        }

        return linksList;
    }
//  method to handling the result from thread
    private synchronized void getAllJobThreadCompletion(List<String[]> result, ArrayList<String[]> jobOpenings) {
        jobOpenings.addAll(result); // merge list
    }
    private synchronized void getAllJobsInfoThreadCompletion(Map<String, ArrayList<Map<String, Object>>> result, Map<String, ArrayList<Map<String, Object>>> jobInfos) {
//      iterate each department and update list inside of it
        for(String d : this.deptNamesList){
            ArrayList<Map<String, Object>> currentlist = jobInfos.get(d);
            currentlist.addAll(result.get(d)); // merge list
            jobInfos.put(d, currentlist);
        }
    }
}

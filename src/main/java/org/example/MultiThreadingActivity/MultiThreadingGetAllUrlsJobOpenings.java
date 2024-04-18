package org.example.MultiThreadingActivity;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadingGetAllUrlsJobOpenings implements Runnable{
    private final int numberPages;
    private final List<Integer> pagesList;
    private final String jobVacancyUrl;
    private final List<String[]> jobUrls ;
    private final MyCallback myCallback;
    public MultiThreadingGetAllUrlsJobOpenings(int numberPages,List<Integer> pagesList, String jobVacancyUrl, MultiThreadingGetAllUrlsJobOpenings.MyCallback myCallback) {
        this.numberPages = numberPages;
        this.pagesList = pagesList;
        this.jobVacancyUrl = jobVacancyUrl;
        this.jobUrls = new ArrayList<>();
        this.myCallback = myCallback;
    }
    public interface MyCallback {
        public void onSuccess(List<String[]> result);
        public void onError();
    }

    public void run(){
//      initialize driver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

//      navigate to the webpage
        driver.get(this.jobVacancyUrl);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // get pagination buttons
        List<WebElement> arrowBtns = driver.findElements(By.cssSelector("div.page-job-container > div > div:last-child > div > button.arrow-icon"));
        WebElement doubleLeftBtn = arrowBtns.getFirst();
        WebElement leftBtn = arrowBtns.get(1);
        WebElement rightBtn = arrowBtns.get(2);
        WebElement doubleRightBtn = arrowBtns.getLast();

        int idxPagesList = 0;
//      iterate each assigned pages
        for(int i = 0 ; i < this.numberPages ; i++){
//          find current page number that being shown
            WebElement activePageBtns = driver.findElement(By.cssSelector("div.page-job-container > div > div:last-child > div > button.active"));

            if(this.pagesList.get(idxPagesList) == Integer.parseInt(activePageBtns.getText())){
                // count div inside each page
                int countJobs = driver.findElements(By.cssSelector("div.page-job-container > div > div.page-job-list-wrapper")).size();

                // visit each job in current page
                for (int j = 1 ; j <= countJobs ; j++){
                    WebElement jobDeparment = driver.findElement(By.xpath("//*[@class='page-job-container']/div/div["+ j +"]/p[1]"));
                    String jobUrl = driver.findElement(By.cssSelector("div.page-job-container > div > div:nth-child("+ j +") > a")).getAttribute("href");
                    String[] jobInfo = {jobDeparment.getText(),jobUrl};
                    this.jobUrls.add(jobInfo);
                }
                System.out.println();

                idxPagesList++;
            }
            if (idxPagesList == this.pagesList.size()){
                break;
            }
            // move on to the next page
            rightBtn.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("job urls size = "+this.jobUrls.size());

        this.myCallback.onSuccess(this.jobUrls);

        driver.quit();
    }
}

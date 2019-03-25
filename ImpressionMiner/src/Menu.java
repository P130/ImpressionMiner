import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import java.util.concurrent.*;

public class Menu {

    private String query;
    private List<String> terms;
    private boolean qquery;

    public void Show(){

        int input;

        do {
            System.out.println("-----Main Menu-----");
            System.out.println("1.Enter query");
            System.out.println("2.Start mining impressions");
            System.out.println("3.Display available websites-targets");
            System.out.println("4.Display terms");
            System.out.println("5.Exit application");

            Scanner s = new Scanner(System.in);
            input = s.nextInt();

            Action(input);
        }while(input != 5);


    }

    private void Action(int input){
        switch(input) {
            case 1:
                EnterQuery();
                break;
            case 2:
                StartMiningImpressions();
                break;
            case 3:
                DisplayWebSites();
                break;
            case 4:
                DisplayTerms();
                break;
            case 5:
                System.exit(0);
                break;
            default:
                System.out.println("You chose something else.");
        }

    }

    private void EnterQuery() {

        System.out.println("Please enter the query below(use space to separate the terms): ");

        Scanner s = new Scanner(System.in);
        query = s.nextLine();
        terms = Arrays.asList(query.split("\\s* \\s*"));

        System.out.println("You entered " + terms.size() + " terms.");

        qquery = true;

    }


    private void StartMiningImpressions() {

        List<Future<int[][]>> results;
        int nowebsites = 0;

        if(!qquery) {
            System.out.println("You need to enter a query first");
            EnterQuery();
        }


        ExecutorService executor = Executors.newCachedThreadPool();
        List<Callable<int[][]>> callables = new ArrayList<>();

        SQLiteDB db = new SQLiteDB();
        ResultSet rs;
        rs = db.retrieveWebSites();


        int noterms = terms.size();
        int[][] searchRes = new int[2][noterms];

        try {
            while (rs.next()) {

                String url = rs.getString("url");
                callables.add(new MineImpressions(url,query));
                nowebsites++;
            }

            if(nowebsites > 0) {
                results = executor.invokeAll(callables);
                for (Future<int[][]> result : results) {

                    int[][] r = result.get();

                    //Add the new array to the search results
                    for (int j = 0; j < noterms; j++) {
                        searchRes[0][j] += r[0][j];
                        searchRes[1][j] += r[1][j];
                    }

                }
                //add each term to db
                for(int j=0; j<noterms; j++)
                    db.addTerm(terms.get(j), searchRes[0][j], searchRes[1][j]);

            }
            else
            {
                System.out.println("There are no websites to search...");
            }

            executor.shutdown();

        } catch (SQLException e) {
            System.err.println("An error occurred while trying reading websites.");
            //e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private void print2DArray(int[][] array) {
        int rows = array[0].length;
        int cols = array.length;
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }


    private void DisplayWebSites() {

        SQLiteDB db = new SQLiteDB();
        ResultSet rs;
        int input;
        String url;


        rs = db.retrieveWebSites();

        try {
            System.out.println("---Websites Targets---");
            while (rs.next()) {
                System.out.println(rs.getString("url"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while trying reading websites.");
            //e.printStackTrace();
        }

        do {

            System.out.println("1.Add a website");
            System.out.println("2.Remove a website");
            System.out.println("3.Back to main menu");

            Scanner s = new Scanner(System.in);
            input = s.nextInt();

            switch(input) {
                case 1:
                    do {
                        System.out.println("Enter the url of the website you want to add (c to cancel):");
                        Scanner i = new Scanner(System.in);
                        url = i.nextLine();
                    }while(url.equals(""));

                    if(!url.equals("c"))
                        db.addWebsite(url);
                    break;
                case 2:
                    do {
                        System.out.println("Enter the url of the website you want to remove (c to cancel):");
                        Scanner i = new Scanner(System.in);
                        url = i.nextLine();
                    }while(url.equals(""));

                    if(!url.equals("c"))
                        db.removeWebsite(url);
                    break;
                case 3:
                    System.out.println("Going back to main menu");
                    break;
                default:
                    System.out.println("Your choice was invalid");
            }
        }while(input!=3);

    }

    private void DisplayTerms() {
        SQLiteDB db = new SQLiteDB();
        ResultSet rs;

        String term;
        int input;
        int count;
        int impression;

        do {

            String type = "";
            System.out.println("---Terms---");
            System.out.println("1.Show positive terms");
            System.out.println("2.Show negative terms");
            System.out.println("3.Show neutral terms");
            System.out.println("4.Show all terms");
            System.out.println("5.Back to main menu");

            Scanner s = new Scanner(System.in);
            input = s.nextInt();


            switch (input) {
                case 1:
                    type = "good";
                    break;
                case 2:
                    type = "bad";
                    break;
                case 3:
                    type = "neutral";
                    break;
                case 4:
                    type = "all";
                    break;

            }

            if (type != "") {
                rs = db.retrieveTerms(type);

                try {
                    while (rs.next()) {
                        term = rs.getString("term");
                        count = rs.getInt("count");
                        impression = rs.getInt("impression");

                        System.out.println("Term \'" + term + "\' has been matched in total "
                                + count + " times and it's impression is " + impression);
                    }

                } catch (SQLException e) {
                    //e.printStackTrace();
                    System.err.println("Something went wrong with displaying terms");
                }

            }
        }while(input!=5);

    }

}

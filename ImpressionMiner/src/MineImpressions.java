import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MineImpressions implements Callable {
    private String url;
    private String query;
    private String regex;
    private List<String> goodTerms;
    private List<String> badTerms;
    private int ret;

    public MineImpressions(String url, String query) {
        this.url = url;
        this.query = query;
    }

    public int[][] call() {

        int col = 0;
        int start, end;

        initTermLists();

        List<String> terms = Arrays.asList(query.split("\\s* \\s*"));
        int[][] localRes = new int[2][terms.size()];

        try {

            Document doc = Jsoup.connect(url).timeout(10000).get();
            String text = doc.text();

            //System.out.println("[" + Thread.currentThread().getName() + "]" + " Searching website " + url + ".");
            for(String term : terms) {

                int impression = 0;
                int matches = 0;
                regex = "(?i)" + term;  //Adding (?i) to remove case sensitivity
                Pattern checkRegex = Pattern.compile(regex);
                Matcher regexMatcher = checkRegex.matcher(text);

                while(regexMatcher.find()) {

                    if(regexMatcher.group().length() != 0) {
                        matches++;
                    }

                    start = regexMatcher.start();
                    end = regexMatcher.end();
                    impression += getImpression(text,start,end);

                }

                /*System.out.println("Term \'" + term + "\'" +
                        " got a total score " + impression + " in website " + url);*/

                localRes[0][col] = matches;
                localRes[1][col] = impression;

                col++;


            }
        } catch (IOException e) {
            System.err.println("Could not get document from " + url);
        }

        System.out.println("[" + Thread.currentThread().getName() + "]" + " Searching website " + url);
        print2DArray(localRes);

        return localRes;

    }

    private void print2DArray(int[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        for(int i=0; i<rows; i++) {
            if(i==0)
                System.out.print("Matches: ");
            else if(i==1)
                System.out.print("Score:");
            for(int j=0; j<cols; j++) {
                System.out.print(arr[i][j] + " | ");
            }
            System.out.println();
        }
    }

    private int getImpression(String text, int start, int end) {

        int good = 0;
        int bad = 0;

        int left = start - 50;
        int right = end + 50;
        String subtext;

        if(left < 0)
            left = 0;
        if(right > text.length())
            right = text.length();

        subtext = text.substring(left, right);

        for(String goodterm : goodTerms) {

            regex = "(?i)" + goodterm;
            Pattern checkRegex = Pattern.compile(regex);
            Matcher regexMatcher = checkRegex.matcher(subtext);

            while(regexMatcher.find()) {
                if (regexMatcher.group().length() != 0) {
                    good++;
                }
            }

        }


        for(String badterm : badTerms) {

            regex = "(?i)" + badterm;
            Pattern checkRegex = Pattern.compile(regex);
            Matcher regexMatcher = checkRegex.matcher(subtext);

            while(regexMatcher.find()) {
                if (regexMatcher.group().length() != 0) {
                    bad++;
                }
            }
        }

        return good-bad;

    }

    private void initTermLists() {
        goodTerms = new ArrayList<>();
        badTerms = new ArrayList<>();

        SQLiteDB db = new SQLiteDB();
        ResultSet rs;

        rs = db.retrieveTerms("good");

        try {
            while(rs.next()) {
                goodTerms.add(rs.getString("term"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rs = db.retrieveTerms("bad");

        try {
            while(rs.next()) {
                badTerms.add(rs.getString("term"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

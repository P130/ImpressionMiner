import java.sql.*;


public class SQLiteDB {

    private static Connection con; // connection to Database
    private static boolean hasData1 = false;
    private static boolean hasData2 = false;

    public ResultSet retrieveWebSites() {
        if(con == null) {
            getConnection();
        }

        ResultSet res;
        try {
            Statement state = con.createStatement();
            res = state.executeQuery("SELECT url FROM websites");
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ResultSet retrieveTerms(String type) {
        if(con == null) {
            getConnection();
        }

        String query;
        switch(type) {
            case "good":
                query = "SELECT term, count, impression from terms WHERE impression > 0";
                break;
            case "bad":
                query = "SELECT term, count, impression from terms WHERE impression < 0";
                break;
            case "neutral":
                query = "SELECT term, count, impression from terms WHERE impression = 0";
                break;
            default:
                query = "SELECT term, count, impression from terms";
        }
        ResultSet res;
        try {
            Statement state = con.createStatement();
            res = state.executeQuery(query);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    private void getConnection() {
        try
        {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:SQLiteDB1");
            initializeTerms();
            initializeWebsites();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void initializeTerms() {
        if(!hasData1) {
            hasData1 = true;

            try
            {
                Statement state = con.createStatement();
                ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='terms'");

                if(!res.next()) {
                    System.out.println("Building the terms table with prepopulated values.");
                    // need to build the table
                    Statement state2 = con.createStatement();
                    state2.execute("CREATE TABLE terms (id integer,"
                            + "term varchar(60),"
                            + "count integer,"
                            + "impression integer,"
                            + "primary key(id));");

                    //inserting some sample data
                    PreparedStatement prep = con.prepareStatement("INSERT INTO terms values(?,?,?,?);");
                    prep.setString(2, "nice");
                    prep.setInt(3, 20);
                    prep.setInt(4, 19);
                    prep.addBatch();
                    prep.setString(2, "awful");
                    prep.setInt(3, 40);
                    prep.setInt(4, -20);
                    prep.addBatch();
                    prep.setString(2, "human");
                    prep.setInt(3, 60);
                    prep.setInt(4, 0);
                    prep.addBatch();
                    prep.setString(2, "and");
                    prep.setInt(3, 200);
                    prep.setInt(4, 50);
                    prep.addBatch();
                    prep.setString(2, "the");
                    prep.setInt(3, 250);
                    prep.setInt(4, -10);
                    prep.addBatch();
                    prep.executeBatch();

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    private void initializeWebsites() {
        if(!hasData2) {
            hasData2 = true;
        }

        try
        {
            Statement state = con.createStatement();
            ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='websites'");

            if(!res.next()) {
                System.out.println("Building the Websites table with prepopulated values.");
                // need to build the table
                Statement state2 = con.createStatement();
                state2.execute("CREATE TABLE websites (id integer,"
                        + "url varchar(60),"
                        + "primary key(id));");

                //inserting some sample data
                PreparedStatement prep = con.prepareStatement("INSERT INTO websites values(?,?);");
                prep.setString(2, "https://edition.cnn.com/");
                prep.addBatch();
                prep.setString(2, "https://www.quora.com/");
                prep.addBatch();
                prep.setString(2, "https://www.nbcnews.com/");
                prep.addBatch();
                prep.setString(2, "https://www.reddit.com/");
                prep.addBatch();
                prep.setString(2, "http://digg.com/");
                prep.addBatch();
                prep.setString(2, "https://stackoverflow.com/");
                prep.addBatch();
                prep.setString(2, "http://www.bbc.com/news");
                prep.addBatch();
                prep.setString(2, "https://www.vox.com/");
                prep.addBatch();
                prep.setString(2, "http://www.foxnews.com/");
                prep.addBatch();
                prep.setString(2, "https://www.wsj.com/europe");
                prep.addBatch();
                prep.executeBatch();


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTerm(String term, int count, int impression) {
        if(con == null) {
            getConnection();
        }

        //first we need to check if the term already exists in table
        ResultSet res;
        try {
            Statement state = con.createStatement();
            res = state.executeQuery("SELECT * FROM terms WHERE term='" + term +"'");

            if(!res.next()) {
                PreparedStatement prep = con.prepareStatement("INSERT INTO terms values(?,?,?,?);");
                prep.setString(2, term);
                prep.setInt(3, count);
                prep.setInt(4, impression);
                prep.execute();
            } else {
                int oldcount = res.getInt("count");
                int oldimpression = res.getInt("impression");

                int newcount = oldcount + count;
                int newimpression = oldimpression + impression;


                String query = "UPDATE terms SET count=?, impression=? WHERE term=?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setInt(1, newcount);
                statement.setInt(2, newimpression);
                statement.setString(3, term);

                statement.executeUpdate();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void addWebsite(String url) {
        if(con == null) {
            getConnection();
        }

        try {
            PreparedStatement prep = con.prepareStatement("INSERT INTO websites values (?,?);");
            prep.setString(2, url);
            prep.execute();
        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Something went wrong with inserting website " + url + " in database.");
        }
    }

    public void removeWebsite(String url) {
        if(con == null) {
            getConnection();
        }

        try {
            PreparedStatement prep = con.prepareStatement("DELETE FROM websites WHERE url=?;");
            prep.setString(1, url);
            prep.execute();
        } catch (SQLException e) {
            //e.printStackTrace();
            System.err.println("Something went wrong with deleting website " + url + " from database.");
        }
    }

}

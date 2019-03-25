import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<String> websites = new ArrayList<>();

    public static void main(String[] args) throws SQLException {

        Menu menu = new Menu();
        menu.Show();

    }

}

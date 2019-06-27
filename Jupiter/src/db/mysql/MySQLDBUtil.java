package db.mysql;

public class MySQLDBUtil {
    private static final String HOSTNAME = "localhost";
    private static final String PORT_NUM = "3310"; // change it to your mysql port number
    //private static final String PORT_NUM = "3306"; // run on EC2
    public static final String DB_NAME = "laiproject";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    //在公司里肯定不会把username，root放在这里
    public static final String URL = "jdbc:mysql://"
            + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
            + "?user=" + USERNAME + "&password=" + PASSWORD
            + "&autoReconnect=true&serverTimezone=UTC";
}

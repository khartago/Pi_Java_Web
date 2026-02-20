package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    Connection con;
    public static Mydatabase instance;
    private Mydatabase(){

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/agriculture_db","root","");
            System.out.println("connexion etablie");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL manquant. Verifiez que le projet est bien un projet Maven et que les dependances sont chargees (clic droit sur pom.xml -> Maven -> Reload Project).", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static Mydatabase getInstance(){
        if(instance==null){
            instance=new Mydatabase();
        }
        return instance;
    }

    public Connection getConnextion(){

        return con;
    }
}

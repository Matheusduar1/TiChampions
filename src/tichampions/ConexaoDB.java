package tichampions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {
    private static final String URL = "jdbc:mysql://localhost:3306/ti_champions";
    private static final String USER = "root"; // Mude se seu usuário do MySQL for diferente
    private static final String PASS = ""; // Coloque sua senha real do MySQL aqui

    public static Connection conectar() {
        try {
            // Força o carregamento do Driver do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.out.println("ALERTA: Driver do MySQL (.jar) não encontrado nas Bibliotecas do NetBeans!");
            return null;
        } catch (SQLException e) {
            System.out.println("Erro na conexão com o Banco: " + e.getMessage());
            return null;
        }
    }
}
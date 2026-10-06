package tichampions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RankingDAO {
    public static void salvarPartida(String nomeEquipe, int andar, int batalhas) {
        String sql = "INSERT INTO historico_partidas (nome_equipe, andar_alcancado, batalhas_vencidas) VALUES (?, ?, ?)";
        
        Connection conn = ConexaoDB.conectar();
        
        // Se a conexão for nula (banco offline ou sem driver), ele NÃO tenta salvar e o jogo continua
        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nomeEquipe);
                stmt.setInt(2, andar);
                stmt.setInt(3, batalhas);
                stmt.executeUpdate();
                System.out.println("Partida salva no banco de dados com sucesso!");
            } catch (SQLException e) {
                System.out.println("Erro ao tentar salvar partida: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ex) {} // Fecha a conexão com segurança
            }
        } else {
            System.out.println("Aviso: Banco de Dados offline. Partida não foi salva no Ranking.");
        }
    }
}
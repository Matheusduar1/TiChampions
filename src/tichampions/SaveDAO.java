package tichampions;

import java.awt.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SaveDAO {
    
    public static boolean temSave() {
        String sql = "SELECT COUNT(*) FROM save_game WHERE id = 1";
        try (Connection conn = ConexaoDB.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {}
        return false;
    }
    
    // NOVO MÉTODO PARA APAGAR O SAVE QUANDO COMEÇAR NOVO JOGO
    public static void deletarSave() {
        try (Connection conn = ConexaoDB.conectar()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM save_itens");
                    stmt.executeUpdate("DELETE FROM save_herois");
                    stmt.executeUpdate("DELETE FROM save_game");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao deletar save antigo: " + e.getMessage());
        }
    }

    public static void salvarEstado(MotorGrafico m) {
        Connection conn = ConexaoDB.conectar();
        if (conn == null) return;

        try {
            deletarSave(); // Limpa o save anterior primeiro

            String sqlGame = "INSERT INTO save_game (id, andar_total, batalhas_seguidas, qtd_jogadores) VALUES (1, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGame)) {
                stmt.setInt(1, m.andarTotal);
                stmt.setInt(2, m.batalhasSeguidas);
                stmt.setInt(3, m.qtdJogadores);
                stmt.executeUpdate();
            }

            // ADICIONADA A COLUNA ja_fugiu
            String sqlHeroi = "INSERT INTO save_herois (id_save, slot_idx, nome_personagem, classe_nome, hp, hp_max, hardware, software, manutencao, firewall, ja_fugiu) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlItem = "INSERT INTO save_itens (id_heroi, nome_item, descricao, tipo, poder, status_uso) VALUES (?, ?, ?, ?, ?, ?)";

            for (int i = 0; i < m.party.size(); i++) {
                HeroiGUI h = m.party.get(i);
                try (PreparedStatement stmt = conn.prepareStatement(sqlHeroi, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, i);
                    stmt.setString(2, h.nome);
                    stmt.setString(3, h.classe.nomeClasse);
                    stmt.setInt(4, h.status.hp);
                    stmt.setInt(5, h.status.hpMax);
                    stmt.setInt(6, h.status.hardware);
                    stmt.setInt(7, h.status.software);
                    stmt.setInt(8, h.status.manutencao);
                    stmt.setInt(9, h.status.firewall);
                    stmt.setInt(10, h.jaFugiuNestaRun ? 1 : 0); // Salva se já fugiu
                    stmt.executeUpdate();

                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int idHeroi = rs.getInt(1);
                        for (Item item : h.mochila) salvarItemDB(conn, sqlItem, idHeroi, item, 0);
                        if (h.armaEquipada != null) salvarItemDB(conn, sqlItem, idHeroi, h.armaEquipada, 1);
                        if (h.armaduraEquipada != null) salvarItemDB(conn, sqlItem, idHeroi, h.armaduraEquipada, 2);
                    }
                }
            }
            System.out.println("Jogo salvo com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao salvar o jogo: " + e.getMessage());
        } finally {
            try { conn.close(); } catch (Exception e) {}
        }
    }

    private static void salvarItemDB(Connection conn, String sql, int idHeroi, Item item, int statusUso) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idHeroi);
            stmt.setString(2, item.nome);
            stmt.setString(3, item.descricao);
            stmt.setInt(4, item.tipo);
            stmt.setInt(5, item.poder);
            stmt.setInt(6, statusUso);
            stmt.executeUpdate();
        }
    }

    public static void carregarEstado(MotorGrafico m) {
        Connection conn = ConexaoDB.conectar();
        if (conn == null) return;

        try {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM save_game WHERE id = 1")) {
                if (rs.next()) {
                    m.andarTotal = rs.getInt("andar_total");
                    m.batalhasSeguidas = rs.getInt("batalhas_seguidas");
                    m.qtdJogadores = rs.getInt("qtd_jogadores");
                }
            }

            m.party.clear();

            String sqlHeroi = "SELECT * FROM save_herois WHERE id_save = 1 ORDER BY slot_idx ASC";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlHeroi)) {
                while (rs.next()) {
                    int idHeroi = rs.getInt("id_heroi");
                    String nomeBase = rs.getString("nome_personagem");
                    HeroiGUI h = criarHeroiBase(nomeBase);
                    h.setClasse(criarClasseBase(rs.getString("classe_nome")));

                    h.status.hp = rs.getInt("hp");
                    h.status.hpMax = rs.getInt("hp_max");
                    h.status.hardware = rs.getInt("hardware");
                    h.status.software = rs.getInt("software");
                    h.status.manutencao = rs.getInt("manutencao");
                    h.status.firewall = rs.getInt("firewall");
                    h.jaFugiuNestaRun = rs.getInt("ja_fugiu") == 1; // Puxa se ele já fugiu

                    carregarItensDoHeroi(conn, h, idHeroi);
                    m.party.add(h);
                }
            }
            
            m.mecanicas.gerarItensLoja();
            m.jogadorTurnoAtual = 0;
            m.turnoInimigo = false;
            m.estadoAtual = MotorGrafico.Estado.LOJA;
            
        } catch (Exception e) {
            System.out.println("Erro ao carregar arquivo: " + e.getMessage());
        } finally {
            try { conn.close(); } catch (Exception e) {}
        }
    }

    private static void carregarItensDoHeroi(Connection conn, HeroiGUI h, int idHeroi) throws Exception {
        String sql = "SELECT * FROM save_itens WHERE id_heroi = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idHeroi);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item(
                        rs.getString("nome_item"), rs.getString("descricao"),
                        rs.getInt("tipo"), rs.getInt("poder"), 
                        getIconeByName(rs.getString("nome_item"))
                    );
                    int uso = rs.getInt("status_uso");
                    if (uso == 0) h.mochila.add(item);
                    else if (uso == 1) h.armaEquipada = item;
                    else if (uso == 2) h.armaduraEquipada = item;
                }
            }
        }
    }

    private static HeroiGUI criarHeroiBase(String nome) {
        if (nome.contains("Matheus")) { Matheus m = new Matheus(); m.sprite = Recursos.imgMatheus; return m; }
        if (nome.contains("Lucas")) { Lucas l = new Lucas(); l.sprite = Recursos.imgLucas; return l; }
        Elvis e = new Elvis(); e.sprite = Recursos.imgElvis; return e;
    }

    private static ClasseRPG criarClasseBase(String classe) {
        if (classe.equals("Infra")) return new Infra();
        if (classe.equals("Java Champion")) return new JavaChampion();
        if (classe.equals("HackerMan")) return new HackerMan();
        if (classe.equals("Dono de LanHouse")) return new DonoLanHouse();
        return new Professor();
    }

    private static Image getIconeByName(String nome) {
        if (nome.equals("Café Forte")) return Recursos.imgItens[0];
        if (nome.equals("Placa RTX")) return Recursos.imgItens[1];
        if (nome.equals("Nobreak")) return Recursos.imgItens[2];
        if (nome.equals("Ferro de Solda")) return Recursos.imgItens[3];
        if (nome.equals("Camisa de Evento")) return Recursos.imgItens[4];
        if (nome.equals("Memória Velha")) return Recursos.imgItens[5];
        return null;
    }
}
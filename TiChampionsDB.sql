CREATE DATABASE IF NOT EXISTS ti_champions;
USE ti_champions;
CREATE TABLE historico_partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_equipe VARCHAR(100) NOT NULL,
    andar_alcancado INT NOT NULL,
    batalhas_vencidas INT NOT NULL,
    data_partida DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tabela que guarda o estado geral do jogo
CREATE TABLE IF NOT EXISTS save_game (
    id INT PRIMARY KEY DEFAULT 1,
    andar_total INT,
    batalhas_seguidas INT,
    qtd_jogadores INT
);

-- Tabela que guarda os heróis e os seus atributos exatos no momento da gravação
CREATE TABLE IF NOT EXISTS save_herois (
    id_heroi INT PRIMARY KEY AUTO_INCREMENT,
    id_save INT,
    slot_idx INT,
    nome_personagem VARCHAR(50),
    classe_nome VARCHAR(50),
    hp INT,
    hp_max INT,
    hardware INT,
    software INT,
    manutencao INT,
    firewall INT
);

-- Tabela que guarda os itens (Mochila e Equipamentos)
CREATE TABLE IF NOT EXISTS save_itens (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    id_heroi INT,
    nome_item VARCHAR(50),
    descricao VARCHAR(100),
    tipo INT,
    poder INT,
    status_uso INT -- 0 = Mochila, 1 = Arma Equipada, 2 = Armadura Equipada
);
#USE ti_champions;
#ALTER TABLE save_herois ADD COLUMN ja_fugiu INT DEFAULT 0;

#select * from historico_partidas; select * from save_game; select * from save_herois; select * from save_itens;

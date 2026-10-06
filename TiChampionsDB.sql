CREATE DATABASE IF NOT EXISTS ti_champions;
USE ti_champions;

CREATE TABLE historico_partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_equipe VARCHAR(100) NOT NULL,
    andar_alcancado INT NOT NULL,
    batalhas_vencidas INT NOT NULL,
    data_partida DATETIME DEFAULT CURRENT_TIMESTAMP
);

select * from historico_partidas
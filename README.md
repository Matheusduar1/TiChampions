# TiChampions
🏆 TI Champions - GUI Edition
TI Champions é um RPG de turnos 2D desenvolvido nativamente em Java (Swing/AWT). O jogo coloca você e seus amigos na pele de estudantes e profissionais de TI que precisam enfrentar ameaças tecnológicas, desde Estagiários desavisados até o temível BOSS Arquiteto, utilizando ataques baseados em Hardware (Dano Físico) e Software (Dano Mágico).

O projeto evoluiu de uma aplicação simples de console para uma arquitetura orientada a objetos robusta (MVC), contando com interface gráfica, efeitos sonoros e persistência de dados em banco de dados.

✨ Principais Funcionalidades
Sistema de Combate Tático: Escolha entre ataques de Hardware ou Software com base nas fraquezas do inimigo (Manutenção ou Firewall).

Multiplayer Local (Party System): Jogue com até 3 jogadores em co-op local. Cada jogador possui um turno individual, passivas únicas e inventário próprio.

Sistema de Classes e Passivas:

Personagens com passivas únicas (Ex: Sobreviver a 1 de HP, Alternar Buffs).

Classes exclusivas (Infra, Java Champion, HackerMan, Dono de LanHouse, Professor), cada uma com atributos e habilidades ativas específicas.

Loja Dinâmica e Auto-Equip: A cada andar vencido, os jogadores acessam a Loja do Marcão/Lendária do Diegão. O sistema possui inteligência para substituir automaticamente equipamentos obsoletos na mochila.

Mecanismos de Áudio (Sound Design): Integração completa com javax.sound.sampled para músicas de fundo em loop contínuo e efeitos sonoros independentes (Hit, Hurt, Special, GameOver).

Integração com Banco de Dados: Histórico de partidas e Leaderboard persistidos localmente utilizando MySQL e JDBC (Padrão DAO).

🏗️ Arquitetura do Projeto
O jogo foi refatorado para eliminar a "God Class" e seguir boas práticas de engenharia de software, dividindo as responsabilidades de forma semelhante ao padrão MVC (Model-View-Controller):

MotorGrafico.java: O "Coração" (Model), armazena as listas de inimigos, jogadores, atributos e os estados principais do jogo.

Renderizador.java: A View, responsável exclusivamente por desenhar a interface gráfica, menus, barras de HP e sprites utilizando Graphics2D.

ControladorMouse.java: O Controller, gerencia os inputs físicos do usuário e roteia as ações.

Mecanicas.java: O cérebro das regras de negócio, controlando os turnos, geração de inimigos, danos e lógica da loja.

GerenciadorAudio.java: Isola a lógica multithreading para reprodução de sons em formato .wav.

Recursos.java: Carregador estático de assets (imagens e texturas), otimizando a memória.

ConexaoDB.java & RankingDAO.java: Camada de persistência para salvar o fim das runs no MySQL.

🛠️ Tecnologias Utilizadas
Linguagem: Java (JDK 25)

Interface Gráfica: Java Swing e AWT

Banco de Dados: MySQL Workbench 8.0+

Conectividade: MySQL Connector/J (JDBC)

Design Patterns: MVC, DAO, Herança e Polimorfismo.

🚀 Como Executar o Projeto
Pré-requisitos
Java JDK instalado (Recomendado Java 17 ou superior).

IDE Java (Apache NetBeans, Eclipse ou IntelliJ).

Servidor MySQL rodando localmente.

Driver mysql-connector-j.jar baixado.

1. Configurando o Banco de Dados
Abra o seu MySQL Workbench e execute o seguinte script para criar o banco de dados e a tabela de histórico de partidas:

SQL
CREATE DATABASE IF NOT EXISTS ti_champions;
USE ti_champions;

CREATE TABLE historico_partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_equipe VARCHAR(100) NOT NULL,
    andar_alcancado INT NOT NULL,
    batalhas_vencidas INT NOT NULL,
    data_partida DATETIME DEFAULT CURRENT_TIMESTAMP
);
2. Configurando o Projeto na IDE
Clone o repositório: git clone [https://github.com/seu-usuario/ti-champions.git](https://github.com/seu-usuario/ti-champions.git)

Abra o projeto na sua IDE.

Adicione o arquivo mysql-connector-j-x.x.x.jar na pasta de bibliotecas (Libraries / Modulepath) do seu projeto.

No arquivo ConexaoDB.java, atualize as constantes USER e PASS com o usuário e senha do seu servidor MySQL local.

Certifique-se de que as pastas sprites e audios estejam na raiz da pasta src/.

Rode a classe TiChampionsMain.java.

👥 Autores e Equipe (Idealizadores do Projeto)
Matheus Duarte - Desenvolvimento Core, Engenharia de Software e Game Design.

Lucas Narezzi - Game Design e Documentação.

Elvis Almeida - Game Design e Documentação.

Projeto acadêmico e de portfólio. Nenhuma linha de código fonte de terceiros (engines) foi utilizada para a construção do motor base.

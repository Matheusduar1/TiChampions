# 🏆 TI Champions - GUI Edition

![Java](https://img.shields.io/badge/Java-JDK%2017%2B%20%7C%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing/AWT](https://img.shields.io/badge/GUI-Java%20Swing%20%26%20AWT-007396?style=for-the-badge)
![MySQL](https://img.shields.io/badge/Database-MySQL%208.0%2B-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Architecture](https://img.shields.io/badge/Pattern-MVC%20%2B%20DAO-success?style=for-the-badge)

**TI Champions** é um RPG Roguelike de turnos 2D desenvolvido nativamente em **Java (Swing/AWT)**, sem o uso de engines externas. O jogo coloca você e seus amigos na pele de estudantes e profissionais de TI que precisam subir andares corporativos e enfrentar ameaças tecnológicas — desde Estagiários desavisados e SQL Injections até chefões lendários como o **BOSS Arquiteto**, **Tela Azul (BSOD)** e **WannaCry**.

O projeto evoluiu de uma aplicação de console para uma arquitetura orientada a objetos robusta (**MVC + DAO**), contando com motor gráfico próprio a 60 FPS, escalonamento dinâmico de resolução, trilha e efeitos sonoros em tempo real, sistema de **Save State** completo e histórico de partidas persistido em banco de dados **MySQL**.

---

## ✨ Principais Funcionalidades

* **⚔️ Combate Tático em Turnos:** Escolha estrategicamente entre ataques de **Hardware** (Dano Físico vs. Manutenção) ou **Software** (Dano Mágico vs. Firewall), além de contar com sistema de precisão, acerto crítico, inspeção de status de inimigos (`Examinar`) e mecânica de fuga com penalidade.
* **👥 Multiplayer Co-op Local (1 a 3 Jogadores):** Monte uma *party* de até 3 heróis jogando localmente. Cada integrante possui seu próprio turno, inventário, equipamentos e habilidades.
* **🦸 Personagens e Classes Exclusivas:** Combine 3 personagens únicos com 5 classes de TI distintas, criando builds variadas de ataque, defesa ou suporte.
* **🛒 Loja Dinâmica (Marcão & Diegão):** Ao vencer cada andar, a equipe visita a **Loja do Marcão** — ou a **Loja Lendária do Diegão** após derrotar um Boss. O sistema gerencia limites de carga e substitui automaticamente equipamentos inferiores.
* **💾 Save State Completo + Ranking (MySQL & JDBC):** Salve o progresso da sua *run* diretamente na loja (`Guardar e Sair`) e retome quando quiser em `Continuar Jogo`, preservando andar, atributos, mochila e itens equipados. Ao final da partida (Game Over), o desempenho da equipe é registrado no histórico.
* **🔊 Áudio & Resolução Adaptável:** Integração com `javax.sound.sampled` para música tema em loop e efeitos sonoros (`Hit`, `Hurt`, `Special`, `Start`, `GameOver`), além de seletor de resolução (`800x600`, `1280x720`, `1920x1080`) com redimensionamento vetorial via `Graphics2D`.

---

## 🎮 Guia de Jogo: Personagens, Classes e Ameaças

### 🦸 Personagens Jogáveis
| Personagem | HP Base | Hardware | Software | Manutenção | Firewall | Passiva Exclusiva |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **Matheus Duarte** | 80 | 5 | 25 | 5 | 15 | **Hiperfoco:** Alterna buffs de `+10 Hardware` e `+10 Software` a cada combate. |
| **Lucas Narezzi** | 120 | 15 | 2 | 20 | 10 | **Eu sou IA:** Ganha `+5 Manutenção` permanentemente no início de cada andar. |
| **Elvis Almeida** | 100 | 20 | 5 | 15 | 2 | **Limão com Mel:** Sobrevive à morte com 25% do HP Máximo (1x por *run*). |

### 🛡️ Classes de TI
| Classe | Modificadores de Atributo | Habilidade Ativa (1x por Andar) |
| :--- | :--- | :--- |
| **Infra** | `+10 Hardware`, `+20 HP` | **Sobrecarga:** Causa super dano de Hardware `(3x + 20)`, mas custa `15 HP`. |
| **Java Champion** | `+10 Manutenção`, `+10 Firewall`, `-5 Hardware` | **Encapsulamento:** Aumenta Manutenção e Firewall em `+25`. |
| **HackerMan** | `+10 Software`, `-5 Manutenção` | **DDOS:** Dispara um ataque de Software `(2x)` em **todos os inimigos**. |
| **Dono de LanHouse** | `+8 Hardware`, `-5 Software`, `30% Crítico` | **+1 Ficha:** Causa dano massivo de Hardware `(4x)` e **ganha um turno extra**. |
| **Professor** | `+30 HP`, `+5 Firewall` | **Ensinamentos:** Recupera `60 HP` do próprio herói. |

### 👾 Inimigos e Bosses
* **Ameaças Comuns:** *Estagiário*, *Hardware Curto*, *Gabinete Assombrado* (Dano de Hardware) | *SQL Injection*, *Cavalo de Troia*, *Criptominerador* (Dano de Software).
* **Chefes de Andar (a cada 5 batalhas):**
  * 👑 **BOSS Arquiteto** *(Especialista em Hardware)*
  * 💻 **BOSS Tela Azul - BSOD** *(Especialista em Software)*
  * 🔒 **BOSS WannaCry** *(Especialista em Software)*

---

## 🏗️ Arquitetura do Projeto

O código-fonte (localizado em `src/tichampions/`) foi estruturado seguindo boas práticas de Programação Orientada a Objetos e separação de responsabilidades (**MVC + DAO**):

```text
TiChampions/
├── src/
│   ├── audios/                 # Efeitos sonoros e trilha musical (.wav)
│   ├── sprites/                # Assets visuais (backgrounds, inimigos, itens, npc, personagens)
│   └── tichampions/
│       ├── TiChampionsMain.java    # Ponto de entrada (JFrame principal)
│       ├── MotorGrafico.java       # Estado global do jogo e Game Loop (60 FPS)
│       ├── Renderizador.java       # View: Renderização de telas, HUD, animações e logs (Graphics2D)
│       ├── ControladorMouse.java   # Controller: Captura de eventos de mouse e navegação de menus
│       ├── Mecanicas.java          # Regras de negócio: Controle de turnos, geração de andares e loja
│       ├── HeroiGUI.java           # Abstração dos heróis, cálculo de ataque e classes filhas
│       ├── ClasseRPG.java          # Abstração das classes de RPG e polimorfismo de skills
│       ├── InimigoGUI.java         # Entidade de inimigos/bosses e IA de ataque
│       ├── Item.java & Status.java # Modelos de atributos e equipamentos/consumíveis
│       ├── Recursos.java           # Carregador estático de sprites e texturas
│       ├── GerenciadorAudio.java   # Sistema de reprodução de clipes de áudio e música em loop
│       ├── ConexaoDB.java          # Fábrica de conexões JDBC com o MySQL
│       ├── SaveDAO.java            # Persistência completa do estado do jogo (Save/Load)
│       └── RankingDAO.java         # Registro do histórico de partidas no banco de dados
└── TiChampionsDB.sql           # Script de criação do banco de dados e tabelas
```

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java (Compatível com JDK 17 a JDK 25)
* **Interface Gráfica:** Java Swing & AWT (`JPanel`, `Graphics2D`, `Timer`)
* **Áudio:** Java Sound API (`javax.sound.sampled`)
* **Banco de Dados:** MySQL 8.0+
* **Conectividade:** JDBC (`mysql-connector-j`)
* **Padrões de Projeto:** MVC, DAO, Herança, Polimorfismo e Encapsulamento

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
1. **Java JDK** instalado (Java 17 ou superior).
2. **IDE Java** (Apache NetBeans, IntelliJ IDEA ou Eclipse).
3. **Servidor MySQL** ativo localmente (XAMPP, WAMP ou MySQL Server).
4. Driver **`mysql-connector-j-26.7.0.jar`** (já incluído na raiz do projeto).

> **💡 Nota:** Caso o MySQL não esteja ativo, o jogo funciona normalmente em modo *offline* (apenas as opções de salvar progresso e gravar no ranking serão desativadas automaticamente sem travar o jogo).

### 1. Configurando o Banco de Dados
Abra o seu **MySQL Workbench** (ou cliente SQL de preferência) e execute o script `TiChampionsDB.sql` (ou copie o código abaixo):

```sql
CREATE DATABASE IF NOT EXISTS ti_champions;
USE ti_champions;

-- Histórico de partidas finalizadas (Game Over)
CREATE TABLE IF NOT EXISTS historico_partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_equipe VARCHAR(100) NOT NULL,
    andar_alcancado INT NOT NULL,
    batalhas_vencidas INT NOT NULL,
    data_partida DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Estado geral do Save Game
CREATE TABLE IF NOT EXISTS save_game (
    id INT PRIMARY KEY DEFAULT 1,
    andar_total INT,
    batalhas_seguidas INT,
    qtd_jogadores INT
);

-- Heróis salvos na partida atual
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
    firewall INT,
    ja_fugiu INT DEFAULT 0
);

-- Itens da mochila e equipamentos salvos
CREATE TABLE IF NOT EXISTS save_itens (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    id_heroi INT,
    nome_item VARCHAR(50),
    descricao VARCHAR(100),
    tipo INT,
    poder INT,
    status_uso INT -- 0 = Mochila, 1 = Arma Equipada, 2 = Armadura Equipada
);
```

### 2. Executando na IDE
1. Clone o repositório ou abra a pasta `TiChampions` no **NetBeans** (ou outra IDE).
2. Certifique-se de que o arquivo `mysql-connector-j-26.7.0.jar` está adicionado às **Libraries / Classpath** do projeto.
3. Caso seu MySQL possua senha configurada, edite as constantes `USER` e `PASS` em [`src/tichampions/ConexaoDB.java`](src/tichampions/ConexaoDB.java).
4. Execute a classe principal [`tichampions.TiChampionsMain`](src/tichampions/TiChampionsMain.java).

---

## 👥 Autores e Equipe

* **Matheus Duarte** — *Desenvolvimento Core, Arquitetura de Software e Game Design*
* **Lucas Narezzi** — *Game Design, Balanceamento e Documentação*
* **Elvis Almeida** — *Game Design, Balanceamento e Documentação*

---
*Projeto acadêmico e de portfólio desenvolvido em Java puro, construindo o motor gráfico e de combate inteiramente do zero.*

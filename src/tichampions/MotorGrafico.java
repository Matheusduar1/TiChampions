package tichampions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Random;

public class MotorGrafico extends JPanel implements ActionListener, MouseListener {

    enum Estado { MENU, OPCOES, MODO_JOGO, SELECAO_PERSONAGEM, SELECAO_CLASSE, COMBATE, LOJA, GAME_OVER }
    Estado estadoAtual = Estado.MENU;

    Timer timerGameLoop;
    double tempoAnimacao = 0;
    double scaleX = 1.0, scaleY = 1.0;
    int mouseX = 0, mouseY = 0;

    // SISTEMA DE COMBATE E SELEÇÃO
    ArrayList<HeroiGUI> party = new ArrayList<>();
    ArrayList<InimigoGUI> inimigos = new ArrayList<>();
    int qtdJogadores = 1;
    int jogadorTurnoAtual = 0;
    boolean turnoInimigo = false;
    
    // Controles de Seleção
    int heroiSelecionadoUI = -1; // Usado na tela de seleção
    int classeSelecionadaUI = -1;
    boolean escolhendoAlvo = false;
    
    // Submenus
    boolean menuItensAberto = false; 
    boolean menuStatusAberto = false;
    Item itemFocado = null; // Qual item está sendo visto no inventário
    
    // LOG E PROGRESSÃO
    boolean mostrandoLog = false;
    String textoLog = "";
    Timer timerLog;
    int andarTotal = 1;
    int batalhasSeguidas = 0;
    boolean lojaLendaria = false;
    Random rng = new Random();

    // SPRITES
    Image bgMenu, bgCombate, bgLoja, spriteMarcao, spriteDiegao;
    Image imgMatheus, imgLucas, imgElvis;
    Image[] imgInimigos = new Image[5]; 
    Image[] imgItens = new Image[5];
    
    // Loja Dinâmica
    Item[] itensLojaAtual = new Item[3];

    public MotorGrafico() {
        this.setPreferredSize(new Dimension(1280, 720));
        this.addMouseListener(this);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) { 
                mouseX = (int)(e.getX() / scaleX); 
                mouseY = (int)(e.getY() / scaleY); 
            }
        });
        carregarRecursos();
        timerGameLoop = new Timer(16, this); timerGameLoop.start();
    }

    private void carregarRecursos() {
        try {
            bgMenu = ImageIO.read(new File("sprites/backgrounds/bg_menu.png"));
            bgCombate = ImageIO.read(new File("sprites/backgrounds/bg_combate.png"));
            bgLoja = ImageIO.read(new File("sprites/backgrounds/bg_loja.png"));
            spriteMarcao = ImageIO.read(new File("sprites/npc/marcao.png"));
            spriteDiegao = ImageIO.read(new File("sprites/npc/diegao.png"));
            imgMatheus = ImageIO.read(new File("sprites/personagens/matheus.png"));
            imgLucas = ImageIO.read(new File("sprites/personagens/lucas.png"));
            imgElvis = ImageIO.read(new File("sprites/personagens/elvis.png"));
            
            imgInimigos[0] = ImageIO.read(new File("sprites/inimigos/estagiario.png"));
            imgInimigos[1] = ImageIO.read(new File("sprites/inimigos/sql_injection.png"));
            imgInimigos[2] = ImageIO.read(new File("sprites/inimigos/hardware_curto.png"));
            imgInimigos[3] = ImageIO.read(new File("sprites/inimigos/boss_arquiteto.png"));
            
            imgItens[0] = ImageIO.read(new File("sprites/itens/cafe.png"));
            imgItens[1] = ImageIO.read(new File("sprites/itens/placa_video.png"));
            imgItens[2] = ImageIO.read(new File("sprites/itens/nobreak.png"));
        } catch (Exception e) {}
    }

    private void exibirLog(String mensagem, Runnable proximaAcao) {
        textoLog = mensagem; mostrandoLog = true;
        menuItensAberto = false; menuStatusAberto = false; itemFocado = null;
        if (timerLog != null && timerLog.isRunning()) timerLog.stop();
        timerLog = new Timer(1500, e -> {
            mostrandoLog = false;
            if (proximaAcao != null) proximaAcao.run();
            repaint();
        });
        timerLog.setRepeats(false); timerLog.start();
    }

    private void gerarItensLoja() {
        // Escalonamento Dinâmico
        int power = 5 + (andarTotal * 2);
        itensLojaAtual[0] = new Item("Café Forte", "Cura " + (20+power*2) + " HP", 0, 20+power*2);
        itensLojaAtual[1] = new Item("Placa RTX", "+" + power + " ATK", 1, power);
        itensLojaAtual[2] = new Item("Nobreak", "+" + power + " DEF", 2, power);
    }

    private void gerarAndarDeCombate() {
        inimigos.clear(); batalhasSeguidas++;
        boolean isBoss = (batalhasSeguidas >= 5);

        if (isBoss) {
            inimigos.add(new InimigoGUI("BOSS: Arquiteto", new Status(300+(andarTotal*10), 30, 0, 20, 20), 3));
            batalhasSeguidas = 0;
        } else {
            int qInimigos = (rng.nextInt(100) < 10) ? qtdJogadores + rng.nextInt(2) + 1 : qtdJogadores;
            for(int i=0; i < qInimigos; i++) {
                inimigos.add(new InimigoGUI("Bug Lv." + andarTotal, new Status(40+(andarTotal*5), 8+andarTotal, 0, 5, 5), rng.nextInt(3)));
            }
        }
        
        for(HeroiGUI h : party) { h.skillUsadaNoAndar = false; h.fugiuDestaBatalha = false; }
        jogadorTurnoAtual = 0; turnoInimigo = false;
        estadoAtual = Estado.COMBATE;
        verificarTurnoValido();
    }

    private void avancarTurno() {
        inimigos.removeIf(i -> i.status.hp <= 0);
        escolhendoAlvo = false; // Reseta travas
        
        if (inimigos.isEmpty()) {
            for(HeroiGUI h : party) h.fugiuNaUltima = false; // Venceu, limpa debuffs!
            andarTotal++; lojaLendaria = (rng.nextInt(100) < 5);
            gerarItensLoja();
            exibirLog("Batalha Vencida! Avançando...", () -> estadoAtual = Estado.LOJA);
            return;
        }
        
        int vivos = 0, fugiram = 0;
        for(HeroiGUI h : party) {
            if(h.status.hp > 0 && !h.fugiuDestaBatalha) vivos++;
            if(h.fugiuDestaBatalha) fugiram++;
        }
        
        if (vivos == 0) {
            if (fugiram > 0) exibirLog("Os sobreviventes fugiram para a Loja!", () -> { gerarItensLoja(); estadoAtual = Estado.LOJA; });
            else exibirLog("GAME OVER! A equipe foi derrotada.", () -> estadoAtual = Estado.GAME_OVER);
            return;
        }

        if (!turnoInimigo) {
            jogadorTurnoAtual++;
            if (jogadorTurnoAtual >= party.size()) { turnoInimigo = true; executarTurnoInimigo(); } 
            else verificarTurnoValido();
        } else {
            turnoInimigo = false; jogadorTurnoAtual = 0; verificarTurnoValido();
        }
    }

    private void verificarTurnoValido() {
        if(turnoInimigo) return;
        HeroiGUI atual = party.get(jogadorTurnoAtual);
        if (atual.status.hp <= 0 || atual.fugiuDestaBatalha) avancarTurno();
    }

    private void executarTurnoInimigo() {
        ArrayList<HeroiGUI> alvosVivos = new ArrayList<>();
        for(HeroiGUI h : party) { if(h.status.hp > 0 && !h.fugiuDestaBatalha) alvosVivos.add(h); }
        if(alvosVivos.isEmpty()) { avancarTurno(); return; }

        InimigoGUI atacante = inimigos.get(0);
        HeroiGUI alvo = alvosVivos.get(rng.nextInt(alvosVivos.size()));
        exibirLog(atacante.atacar(alvo), () -> avancarTurno());
    }

    @Override
    public void actionPerformed(ActionEvent e) { tempoAnimacao += 0.05; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        scaleX = getWidth() / 1280.0; scaleY = getHeight() / 720.0;
        g2d.scale(scaleX, scaleY);

        switch (estadoAtual) {
            case MENU: desenharMenu(g2d); break;
            case OPCOES: desenharOpcoes(g2d); break;
            case MODO_JOGO: desenharModoJogo(g2d); break;
            case SELECAO_PERSONAGEM: desenharSelecaoPersonagem(g2d); break;
            case SELECAO_CLASSE: desenharSelecaoClasse(g2d); break;
            case COMBATE: desenharCombate(g2d); break;
            case LOJA: desenharLoja(g2d); break;
            case GAME_OVER: desenharGameOver(g2d); break;
        }

        if (mostrandoLog) {
            g.setColor(new Color(0, 0, 0, 200)); g.fillRect(0, 0, 1280, 720);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(textoLog, (1280 - fm.stringWidth(textoLog))/2, 360);
        }
    }

    private void desenharMenu(Graphics2D g) {
        if (bgMenu != null) g.drawImage(bgMenu, 0, 0, 1280, 720, null);
        else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 1280, 720); }
        int btnY = 350 + (int)(Math.sin(tempoAnimacao) * 10);
        desenharBotao(g, "INICIAR JOGO", 540, btnY, 200, 60, false);
        desenharBotao(g, "OPÇÕES", 540, btnY + 80, 200, 60, false);
        desenharBotao(g, "SAIR", 540, btnY + 160, 200, 60, false);
    }

    private void desenharOpcoes(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("MENU DE OPÇÕES", 450, 100);
        desenharBotao(g, "Resolução 800x600", 500, 200, 280, 60, false);
        desenharBotao(g, "Resolução 1280x720", 500, 300, 280, 60, false);
        desenharBotao(g, "Resolução 1920x1080", 500, 400, 280, 60, false);
        desenharBotao(g, "VOLTAR", 540, 550, 200, 60, false);
    }

    private void desenharModoJogo(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("QUANTOS JOGADORES?", 400, 200);
        desenharBotao(g, "1 JOGADOR", 540, 300, 200, 60, false);
        desenharBotao(g, "2 JOGADORES", 540, 400, 200, 60, false);
        desenharBotao(g, "3 JOGADORES", 540, 500, 200, 60, false);
    }

    private void desenharSelecaoPersonagem(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SELECIONE O PLAYER " + (party.size() + 1), 400, 100);
        
        desenharBotaoSprite(g, imgMatheus, 300, 200, 159, 300, heroiSelecionadoUI == 0);
        desenharBotaoSprite(g, imgLucas, 559, 200, 159, 300, heroiSelecionadoUI == 1);
        desenharBotaoSprite(g, imgElvis, 818, 200, 159, 300, heroiSelecionadoUI == 2);
        
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Matheus", 330, 530); g.drawString("Lucas", 600, 530); g.drawString("Elvis", 860, 530);

        if (heroiSelecionadoUI != -1) desenharBotao(g, "AVANÇAR", 1000, 600, 200, 60, false);
    }
    
    private void desenharSelecaoClasse(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("ESCOLHA SUA CLASSE", 420, 100);
        
        desenharBotao(g, "Infra (Guerreiro)", 200, 200, 250, 80, classeSelecionadaUI == 0);
        desenharBotao(g, "Java Champion (Tank)", 500, 200, 250, 80, classeSelecionadaUI == 1);
        desenharBotao(g, "HackerMan (Mago)", 800, 200, 250, 80, classeSelecionadaUI == 2);
        desenharBotao(g, "LanHouse (Rogue)", 350, 350, 250, 80, classeSelecionadaUI == 3);
        desenharBotao(g, "Professor (Paladino)", 650, 350, 250, 80, classeSelecionadaUI == 4);

        if (classeSelecionadaUI != -1) desenharBotao(g, "CONFIRMAR", 1000, 600, 200, 60, false);
    }

    private void desenharCombate(Graphics2D g) {
        if (bgCombate != null) g.drawImage(bgCombate, 0, 0, 1280, 720, null);
        else { g.setColor(new Color(50, 0, 0)); g.fillRect(0, 0, 1280, 720); }

        // Desenhar Inimigos
        for(int i=0; i < inimigos.size(); i++) {
            int x = 1280/(inimigos.size()+1) * (i+1) - 140;
            Image spr = imgInimigos[inimigos.get(i).spriteId];
            if (spr != null) g.drawImage(spr, x, 150, 280, 280, null);
            else { g.setColor(Color.RED); g.fillRoundRect(x, 150, 280, 280, 20, 20); }
            
            // Nome acima da cabeça
            g.setColor(Color.BLACK); g.fillRect(x + 20, 110, 240, 30);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(inimigos.get(i).nome + " ("+inimigos.get(i).status.hp+" HP)", x + 30, 130);
            
            // Highlight se escolhendo alvo
            if(escolhendoAlvo) {
                g.setColor(Color.YELLOW); g.setStroke(new BasicStroke(3)); g.drawRect(x, 150, 280, 280);
            }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        
        if (!turnoInimigo && !party.isEmpty() && !mostrandoLog) {
            HeroiGUI h = party.get(jogadorTurnoAtual);
            
            // HUD de Turno Escurecido
            g.setColor(new Color(0, 0, 0, 180)); g.fillRect(10, 10, 350, 100);
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Vez de: " + h.nome, 20, 40);
            g.setColor(Color.WHITE); g.drawString("HP: " + h.status.hp + " | " + h.classe.nomeClasse, 20, 70);
            if (h.fugiuNaUltima) { g.setColor(Color.RED); g.drawString("DEBUFF: -50% Dano!", 20, 95); }

            desenharMiniInventario(g, h);

            if (escolhendoAlvo) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("SELECIONE O ALVO CLICANDO NO INIMIGO", 350, 630);
                desenharBotao(g, "CANCELAR", 1050, 560, 180, 80, false);
            }
            else if (menuItensAberto) {
                // Desenha os Itens da Mochila
                for(int i=0; i<Math.min(h.mochila.size(), 4); i++) { // Mostra max 4 na tela
                    desenharBotao(g, h.mochila.get(i).nome, 250 + (i*160), 540, 150, 60, itemFocado == h.mochila.get(i));
                }
                
                if (itemFocado != null) {
                    g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16));
                    g.drawString("Efeito: " + itemFocado.descricao, 250, 630);
                    desenharBotao(g, itemFocado.tipo == 0 ? "USAR" : "EQUIPAR", 250, 650, 120, 40, false);
                    desenharBotao(g, "DESCARTAR", 390, 650, 120, 40, false);
                }
                desenharBotao(g, "VOLTAR", 1050, 560, 180, 80, false);
            } 
            else if (menuStatusAberto) {
                g.setColor(new Color(0, 0, 0, 220)); g.fillRect(300, 100, 680, 400);
                g.setColor(Color.WHITE); g.drawRect(300, 100, 680, 400);
                if (h.sprite != null) g.drawImage(h.sprite, 320, 150, 159, 300, null);
                g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("STATUS DE " + h.nome.toUpperCase(), 500, 150);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                g.drawString("Classe: " + h.classe.nomeClasse, 500, 200);
                g.drawString("HP: " + h.status.hp + " / " + h.status.hpMax, 500, 240);
                g.drawString("Físico: " + h.status.danoFisico + (h.armaEquipada!=null?"(+"+h.armaEquipada.poder+")":""), 500, 280);
                g.drawString("Hacking: " + h.status.hacking, 500, 320);
                g.drawString("Def Fís: " + h.status.defFisica + (h.armaduraEquipada!=null?"(+"+h.armaduraEquipada.poder+")":""), 500, 360);
                g.drawString("Antivírus: " + h.status.antivirus, 500, 400);
                desenharBotao(g, "FECHAR STATUS", 540, 560, 240, 80, false);
            } 
            else {
                desenharBotao(g, "ATACAR", 250, 560, 180, 80, false);
                desenharBotao(g, "MOCHILA", 450, 560, 180, 80, false);
                if(!h.skillUsadaNoAndar) desenharBotao(g, "SKILL", 650, 560, 180, 80, false);
                else { g.setColor(Color.DARK_GRAY); g.fillRect(650, 560, 180, 80); }
                desenharBotao(g, "STATUS", 850, 560, 180, 80, false);
                desenharBotao(g, "FUGIR", 1050, 560, 180, 80, false);
            }
        }
    }
    
    private void desenharMiniInventario(Graphics2D g, HeroiGUI h) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(20, 540, 60, 60); g.fillRect(90, 540, 60, 60); g.fillRect(160, 540, 60, 60);
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2));
        g.drawRect(20, 540, 60, 60); g.drawRect(90, 540, 60, 60); g.drawRect(160, 540, 60, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("ARMA", 30, 615); g.drawString("ARMAD.", 95, 615); g.drawString("ATIVO", 170, 615);
        
        // Pinta Verde se tem algo equipado
        if (h.armaEquipada != null) { g.setColor(Color.GREEN); g.fillRect(22, 542, 56, 56); }
        if (h.armaduraEquipada != null) { g.setColor(Color.BLUE); g.fillRect(92, 542, 56, 56); }
    }

    private void desenharLoja(Graphics2D g) {
        if (bgLoja != null) g.drawImage(bgLoja, 0, 0, 1280, 720, null);
        else { g.setColor(new Color(0, 50, 50)); g.fillRect(0, 0, 1280, 720); }

        Image npcImg = lojaLendaria ? spriteDiegao : spriteMarcao;
        if (npcImg != null) g.drawImage(npcImg, 100, 180, 159, 300, null);

        g.setColor(Color.WHITE); g.fillRect(450, 30, 400, 80);
        g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 25));
        g.drawString(lojaLendaria ? "LOJA LENDÁRIA DO DIEGÃO" : "LOJA DO MARCÃO", 480, 80);

        // Desenha as Caixas de Nomes na Loja
        for(int i=0; i<3; i++) {
            int x = 450 + (i*200);
            desenharBotaoSprite(g, imgItens[i], x, 200, 150, 250, false);
            g.setColor(Color.BLACK); g.fillRect(x, 460, 150, 40);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 14));
            if(itensLojaAtual[i] != null) g.drawString(itensLojaAtual[i].nome, x+10, 485);
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        g.setColor(Color.WHITE); g.drawString("Pegue UM item para o Herói atual (" + party.get(jogadorTurnoAtual).nome + ")", 50, 560);
        
        desenharBotao(g, "Próximo Andar", 800, 560, 200, 60, false);
        desenharBotao(g, "Sair do Jogo", 1050, 560, 150, 60, false);
    }
    
    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(100, 0, 0)); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("GAME OVER", 450, 300);
        desenharBotao(g, "VOLTAR AO MENU", 500, 500, 280, 60, false);
    }

    private void desenharBotao(Graphics2D g, String t, int x, int y, int w, int h, boolean selecionado) {
        boolean hover = (!mostrandoLog && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h);
        g.setColor(hover || selecionado ? new Color(70, 70, 70) : new Color(30, 30, 30)); g.fillRect(x, y, w, h);
        g.setColor(hover || selecionado ? Color.WHITE : Color.GRAY); g.setStroke(new BasicStroke(hover || selecionado ? 4 : 2)); g.drawRect(x, y, w, h);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics(); g.drawString(t, x + (w - fm.stringWidth(t)) / 2, y + ((h - fm.getHeight()) / 2) + fm.getAscent());
    }
    
    private void desenharBotaoSprite(Graphics2D g, Image img, int x, int y, int w, int h, boolean selecionado) {
        if (img != null) g.drawImage(img, x, y, w, h, null);
        else { g.setColor(Color.BLUE); g.fillRect(x, y, w, h); }
        if (selecionado) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4)); g.drawRect(x, y, w, h); }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mostrandoLog) return; 
        int mx = (int)(e.getX() / scaleX); int my = (int)(e.getY() / scaleY);

        if (estadoAtual == Estado.MENU) {
            if (mx > 540 && mx < 740 && my > 350 && my < 410) estadoAtual = Estado.MODO_JOGO;
            if (mx > 540 && mx < 740 && my > 430 && my < 490) estadoAtual = Estado.OPCOES;
            if (mx > 540 && mx < 740 && my > 510 && my < 570) System.exit(0);
        }
        else if (estadoAtual == Estado.OPCOES) {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (mx > 500 && mx < 780 && my > 200 && my < 260) { frame.setSize(800, 600); frame.setLocationRelativeTo(null); }
            if (mx > 500 && mx < 780 && my > 300 && my < 360) { frame.setSize(1280, 720); frame.setLocationRelativeTo(null); }
            if (mx > 500 && mx < 780 && my > 400 && my < 460) { frame.setSize(1920, 1080); frame.setLocationRelativeTo(null); }
            if (mx > 540 && mx < 740 && my > 550 && my < 610) estadoAtual = Estado.MENU; 
        }
        else if (estadoAtual == Estado.MODO_JOGO) {
            if (mx > 500 && mx < 780 && my > 300 && my < 360) { qtdJogadores = 1; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 400 && my < 460) { qtdJogadores = 2; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { qtdJogadores = 3; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
        }
        else if (estadoAtual == Estado.SELECAO_PERSONAGEM) {
            if (mx > 300 && mx < 459 && my > 200 && my < 500) heroiSelecionadoUI = 0;
            if (mx > 559 && mx < 718 && my > 200 && my < 500) heroiSelecionadoUI = 1;
            if (mx > 818 && mx < 977 && my > 200 && my < 500) heroiSelecionadoUI = 2;
            
            if (heroiSelecionadoUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI h = (heroiSelecionadoUI == 0) ? new Matheus() : (heroiSelecionadoUI == 1) ? new Lucas() : new Elvis();
                h.sprite = (heroiSelecionadoUI == 0) ? imgMatheus : (heroiSelecionadoUI == 1) ? imgLucas : imgElvis;
                party.add(h);
                heroiSelecionadoUI = -1;
                estadoAtual = Estado.SELECAO_CLASSE; // Vai para escolher a classe deste herói
            }
        }
        else if (estadoAtual == Estado.SELECAO_CLASSE) {
            if (mx > 200 && mx < 450 && my > 200 && my < 280) classeSelecionadaUI = 0;
            if (mx > 500 && mx < 750 && my > 200 && my < 280) classeSelecionadaUI = 1;
            if (mx > 800 && mx < 1050 && my > 200 && my < 280) classeSelecionadaUI = 2;
            if (mx > 350 && mx < 600 && my > 350 && my < 430) classeSelecionadaUI = 3;
            if (mx > 650 && mx < 900 && my > 350 && my < 430) classeSelecionadaUI = 4;
            
            if (classeSelecionadaUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI heroiAtual = party.get(party.size() - 1); // Pega o último adicionado
                switch(classeSelecionadaUI) {
                    case 0: heroiAtual.setClasse(new Infra()); break;
                    case 1: heroiAtual.setClasse(new JavaChampion()); break;
                    case 2: heroiAtual.setClasse(new HackerMan()); break;
                    case 3: heroiAtual.setClasse(new DonoLanHouse()); break;
                    case 4: heroiAtual.setClasse(new Professor()); break;
                }
                classeSelecionadaUI = -1;
                if (party.size() >= qtdJogadores) gerarAndarDeCombate();
                else estadoAtual = Estado.SELECAO_PERSONAGEM; // Volta pra escolher o Player 2/3
            }
        }
        else if (estadoAtual == Estado.COMBATE && !turnoInimigo) {
            HeroiGUI p = party.get(jogadorTurnoAtual);

            if (menuStatusAberto) {
                if (mx > 540 && mx < 780 && my > 560 && my < 640) menuStatusAberto = false; 
                return;
            }
            if (menuItensAberto) {
                // Clique nos itens da mochila
                for(int i=0; i<Math.min(p.mochila.size(), 4); i++) {
                    if (mx > 250+(i*160) && mx < 400+(i*160) && my > 540 && my < 600) { itemFocado = p.mochila.get(i); }
                }
                if (itemFocado != null) {
                    if (mx > 250 && mx < 370 && my > 650 && my < 690) { // USAR/EQUIPAR
                        if (itemFocado.tipo == 0) p.status.hp = Math.min(p.status.hpMax, p.status.hp + itemFocado.poder);
                        else if (itemFocado.tipo == 1) p.armaEquipada = itemFocado;
                        else p.armaduraEquipada = itemFocado;
                        
                        p.mochila.remove(itemFocado);
                        exibirLog(p.nome + " utilizou " + itemFocado.nome + "!", () -> avancarTurno());
                    }
                    if (mx > 390 && mx < 510 && my > 650 && my < 690) { // DESCARTAR
                        p.mochila.remove(itemFocado); itemFocado = null; repaint();
                    }
                }
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { menuItensAberto = false; itemFocado = null; }
                return;
            }

            if (escolhendoAlvo) {
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) escolhendoAlvo = false; // CANCELAR
                for(int i=0; i < inimigos.size(); i++) {
                    int x = 1280/(inimigos.size()+1) * (i+1) - 140;
                    if (mx > x && mx < x+280 && my > 150 && my < 430) {
                        exibirLog(p.atacarBasico(inimigos.get(i)), () -> avancarTurno());
                    }
                }
                return;
            }

            if (mx > 250 && mx < 430 && my > 560 && my < 640) escolhendoAlvo = true; // ATACAR
            if (mx > 450 && mx < 630 && my > 560 && my < 640) { if(p.mochila.size()>0) menuItensAberto = true; else exibirLog("Mochila Vazia!", null); }
            if (!p.skillUsadaNoAndar && mx > 650 && mx < 830 && my > 560 && my < 640) { 
                p.skillUsadaNoAndar = true; exibirLog(p.classe.usarSkill(p, inimigos), () -> avancarTurno());
            }
            if (mx > 850 && mx < 1030 && my > 560 && my < 640) menuStatusAberto = true; 
            
            if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { 
                p.fugiuNaUltima = true; p.fugiuDestaBatalha = true;
                exibirLog(p.nome + " fugiu da sala com medo!", () -> avancarTurno());
            }
        }
        else if (estadoAtual == Estado.LOJA) {
            HeroiGUI pAtual = party.get(jogadorTurnoAtual);
            
            for(int i=0; i<3; i++) {
                int x = 450 + (i*200);
                if (itensLojaAtual[i] != null && mx > x && mx < x+150 && my > 200 && my < 450) {
                    pAtual.mochila.add(itensLojaAtual[i]);
                    exibirLog(pAtual.nome + " guardou " + itensLojaAtual[i].nome + " na mochila!", null);
                    itensLojaAtual[i] = null; // Some da prateleira
                    
                    // Passa a vez de comprar para o próximo jogador
                    jogadorTurnoAtual++;
                    if (jogadorTurnoAtual >= party.size()) {
                        exibirLog("Todos compraram! Partindo para o combate...", () -> gerarAndarDeCombate());
                    }
                }
            }
            if (mx > 800 && mx < 1000 && my > 560 && my < 620) gerarAndarDeCombate();
            if (mx > 1050 && mx < 1200 && my > 560 && my < 620) System.exit(0);
        }
    }
    public void mouseClicked(MouseEvent e) {} public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
}
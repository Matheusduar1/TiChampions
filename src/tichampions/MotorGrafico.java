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

    Timer timerGameLoop; double tempoAnimacao = 0;
    double scaleX = 1.0, scaleY = 1.0; int mouseX = 0, mouseY = 0;

    ArrayList<HeroiGUI> party = new ArrayList<>();
    ArrayList<InimigoGUI> inimigos = new ArrayList<>();
    int qtdJogadores = 1, jogadorTurnoAtual = 0; boolean turnoInimigo = false;
    
    int heroiSelecionadoUI = -1, classeSelecionadaUI = -1;
    boolean escolhendoAlvo = false, menuItensAberto = false, menuStatusAberto = false, subMenuItem = false;
    Item itemFocado = null; 
    
    // Novo Sistema de LOG LATERAL
    ArrayList<String> logBatalha = new ArrayList<>();
    Timer timerEsperaAcao; // Pausa para a animação acontecer antes do turno passar
    boolean bloqueiaClique = false;

    int andarTotal = 1, batalhasSeguidas = 0; boolean lojaLendaria = false;
    Random rng = new Random();

    Image bgMenu, bgCombate, bgLoja, spriteMarcao, spriteDiegao;
    Image imgMatheus, imgLucas, imgElvis;
    Image[] imgInimigos = new Image[5]; Image[] imgItens = new Image[5];
    Item[] itensLojaAtual = new Item[3];

    public MotorGrafico() {
        this.setPreferredSize(new Dimension(1280, 720));
        this.addMouseListener(this);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) { mouseX = (int)(e.getX() / scaleX); mouseY = (int)(e.getY() / scaleY); }
        });
        carregarRecursos();
        timerGameLoop = new Timer(16, this); timerGameLoop.start();
    }

    private void carregarRecursos() {
        try {
            bgMenu = ImageIO.read(new File("sprites/backgrounds/bg_menu.png")); bgCombate = ImageIO.read(new File("sprites/backgrounds/bg_combate.png"));
            bgLoja = ImageIO.read(new File("sprites/backgrounds/bg_loja.png")); spriteMarcao = ImageIO.read(new File("sprites/npc/marcao.png"));
            spriteDiegao = ImageIO.read(new File("sprites/npc/diegao.png")); imgMatheus = ImageIO.read(new File("sprites/personagens/matheus.png"));
            imgLucas = ImageIO.read(new File("sprites/personagens/lucas.png")); imgElvis = ImageIO.read(new File("sprites/personagens/elvis.png"));
            imgInimigos[0] = ImageIO.read(new File("sprites/inimigos/estagiario.png")); imgInimigos[1] = ImageIO.read(new File("sprites/inimigos/sql_injection.png"));
            imgInimigos[2] = ImageIO.read(new File("sprites/inimigos/hardware_curto.png")); imgInimigos[3] = ImageIO.read(new File("sprites/inimigos/boss_arquiteto.png"));
            imgItens[0] = ImageIO.read(new File("sprites/itens/cafe.png")); imgItens[1] = ImageIO.read(new File("sprites/itens/placa_video.png"));
            imgItens[2] = ImageIO.read(new File("sprites/itens/nobreak.png"));
        } catch (Exception e) {}
    }

    // Adiciona msg na lista e dá um delay para a pessoa ler
    private void addLog(String msg, Runnable proximaAcao) {
        logBatalha.add(msg);
        if (logBatalha.size() > 2) logBatalha.remove(0); // Mantém só as últimas 2 mensagens
        
        bloqueiaClique = true;
        menuItensAberto = false; menuStatusAberto = false; subMenuItem = false;
        
        if (timerEsperaAcao != null && timerEsperaAcao.isRunning()) timerEsperaAcao.stop();
        timerEsperaAcao = new Timer(1200, e -> {
            bloqueiaClique = false;
            if (proximaAcao != null) proximaAcao.run();
            repaint();
        });
        timerEsperaAcao.setRepeats(false); timerEsperaAcao.start();
    }

    private void gerarItensLoja() {
        int power = 5 + (andarTotal * 2);
        itensLojaAtual[0] = new Item("Café Forte", "Cura " + (20+power*2) + " HP", 0, 20+power*2, imgItens[0]);
        itensLojaAtual[1] = new Item("Placa RTX", "+" + power + " Hardware", 1, power, imgItens[1]);
        itensLojaAtual[2] = new Item("Nobreak", "+" + power + " Manutenção", 2, power, imgItens[2]);
    }

    private void gerarAndarDeCombate() {
        inimigos.clear(); batalhasSeguidas++;
        if (batalhasSeguidas >= 5) {
            inimigos.add(new InimigoGUI("BOSS: Arquiteto", new Status(300+(andarTotal*10), 20+andarTotal, 0, 15, 15), 3, 0));
            batalhasSeguidas = 0;
        } else {
            int qInimigos = (rng.nextInt(100) < 10) ? qtdJogadores + rng.nextInt(2) + 1 : qtdJogadores;
            for(int i=0; i < qInimigos; i++) {
                int idSpr = rng.nextInt(3); int tAtaque = (idSpr == 1) ? 1 : 0; 
                inimigos.add(new InimigoGUI("Bug Lv." + andarTotal, new Status(40+(andarTotal*10), 10+andarTotal, 10+andarTotal, 2, 2), idSpr, tAtaque));
            }
        }
        logBatalha.clear();
        for(HeroiGUI h : party) { 
            h.skillUsadaNoAndar = false; h.fugiuDestaBatalha = false; h.tentouFugirNoAndar = false; 
            logBatalha.add(h.aplicarPassivaTurno()); // Passiva inicial no log
        }
        
        jogadorTurnoAtual = 0; turnoInimigo = false;
        estadoAtual = Estado.COMBATE; verificarTurnoValido();
    }

    private void avancarTurno() {
        inimigos.removeIf(i -> i.status.hp <= 0);
        escolhendoAlvo = false; 
        
        if (inimigos.isEmpty()) {
            for(HeroiGUI h : party) h.fugiuNaUltima = false;
            andarTotal++; lojaLendaria = (rng.nextInt(100) < 5); gerarItensLoja();
            addLog("Batalha Vencida! Avançando...", () -> estadoAtual = Estado.LOJA); return;
        }
        
        int vivos = 0, fugiram = 0;
        for(HeroiGUI h : party) { if(h.status.hp > 0 && !h.fugiuDestaBatalha) vivos++; if(h.fugiuDestaBatalha) fugiram++; }
        
        if (vivos == 0) {
            if (fugiram > 0) addLog("Os sobreviventes fugiram para a Loja!", () -> { gerarItensLoja(); estadoAtual = Estado.LOJA; });
            else addLog("GAME OVER! A equipe foi derrotada.", () -> estadoAtual = Estado.GAME_OVER);
            return;
        }

        if (!turnoInimigo) {
            jogadorTurnoAtual++;
            if (jogadorTurnoAtual >= party.size()) { turnoInimigo = true; executarTurnoInimigo(); } else verificarTurnoValido();
        } else { turnoInimigo = false; jogadorTurnoAtual = 0; verificarTurnoValido(); }
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
        addLog(atacante.atacar(alvo), () -> avancarTurno());
    }

    @Override
    public void actionPerformed(ActionEvent e) { 
        tempoAnimacao += 0.05; 
        for(InimigoGUI ini : inimigos) { if(ini.timerPiscar > 0) ini.timerPiscar--; } // Controla a piscada do hit
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        scaleX = getWidth() / 1280.0; scaleY = getHeight() / 720.0; g2d.scale(scaleX, scaleY);

        switch (estadoAtual) {
            case MENU: desenharMenu(g2d); break; case OPCOES: desenharOpcoes(g2d); break;
            case MODO_JOGO: desenharModoJogo(g2d); break; case SELECAO_PERSONAGEM: desenharSelecaoPersonagem(g2d); break;
            case SELECAO_CLASSE: desenharSelecaoClasse(g2d); break; case COMBATE: desenharCombate(g2d); break;
            case LOJA: desenharLoja(g2d); break; case GAME_OVER: desenharGameOver(g2d); break;
        }
    }

    private void desenharHUDGlobal(Graphics2D g, boolean mostrarVoltar) {
        if(mostrarVoltar) desenharBotao(g, "VOLTAR", 20, 20, 150, 40, false);
        desenharBotao(g, "SAIR", 1100, 20, 150, 40, false);
    }

    // --- MÉTODOS DE DESENHO ---
    private void desenharMenu(Graphics2D g) {
        if (bgMenu != null) g.drawImage(bgMenu, 0, 0, 1280, 720, null); else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 1280, 720); }
        int btnY = 350 + (int)(Math.sin(tempoAnimacao) * 10);
        desenharBotao(g, "INICIAR JOGO", 540, btnY, 200, 60, false); desenharBotao(g, "OPÇÕES", 540, btnY + 80, 200, 60, false);
        desenharBotao(g, "SAIR", 540, btnY + 160, 200, 60, false);
    }
    
    private void desenharOpcoes(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("MENU DE OPÇÕES", 450, 100);
        desenharBotao(g, "Resolução 800x600", 500, 200, 280, 60, false); desenharBotao(g, "Resolução 1280x720", 500, 300, 280, 60, false);
        desenharBotao(g, "Resolução 1920x1080", 500, 400, 280, 60, false); desenharBotao(g, "VOLTAR", 540, 550, 200, 60, false);
    }
    
    private void desenharModoJogo(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("QUANTOS JOGADORES?", 400, 200); desenharHUDGlobal(g, true);
        desenharBotao(g, "1 JOGADOR", 540, 300, 200, 60, false); desenharBotao(g, "2 JOGADORES", 540, 400, 200, 60, false);
        desenharBotao(g, "3 JOGADORES", 540, 500, 200, 60, false);
    }

    private void desenharSelecaoPersonagem(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); desenharHUDGlobal(g, true);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SELECIONE O PLAYER " + (party.size() + 1), 400, 100);
        
        desenharBotaoSprite(g, imgMatheus, 250, 200, 159, 300, heroiSelecionadoUI == 0);
        desenharBotaoSprite(g, imgLucas, 550, 200, 159, 300, heroiSelecionadoUI == 1);
        desenharBotaoSprite(g, imgElvis, 850, 200, 159, 300, heroiSelecionadoUI == 2);
        
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Matheus", 280, 530); g.drawString("Lucas", 590, 530); g.drawString("Elvis", 890, 530);
        
        if (heroiSelecionadoUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            if(heroiSelecionadoUI == 0) { g.drawString("HP: 80 | Hard: 5 | Soft: 25", 220, 560); g.drawString("Passiva: Alterna Buffs", 220, 590); }
            if(heroiSelecionadoUI == 1) { g.drawString("HP: 120 | Hard: 15 | Soft: 2", 520, 560); g.drawString("Passiva: Buff Defesa Início", 520, 590); }
            if(heroiSelecionadoUI == 2) { g.drawString("HP: 100 | Hard: 20 | Soft: 5", 820, 560); g.drawString("Passiva: Sobrevive a 0 HP", 820, 590); }
            desenharBotao(g, "AVANÇAR", 1000, 600, 200, 60, false);
        }
    }
    
    private void desenharSelecaoClasse(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); desenharHUDGlobal(g, true);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("ESCOLHA SUA CLASSE", 420, 100);
        
        desenharBotao(g, "Infra", 200, 200, 250, 60, classeSelecionadaUI == 0);
        desenharBotao(g, "Java Champion", 500, 200, 250, 60, classeSelecionadaUI == 1);
        desenharBotao(g, "HackerMan", 800, 200, 250, 60, classeSelecionadaUI == 2);
        desenharBotao(g, "LanHouse", 350, 300, 250, 60, classeSelecionadaUI == 3);
        desenharBotao(g, "Professor", 650, 300, 250, 60, classeSelecionadaUI == 4);

        if (classeSelecionadaUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            ClasseRPG cTemp = null;
            if(classeSelecionadaUI==0) cTemp = new Infra(); if(classeSelecionadaUI==1) cTemp = new JavaChampion();
            if(classeSelecionadaUI==2) cTemp = new HackerMan(); if(classeSelecionadaUI==3) cTemp = new DonoLanHouse();
            if(classeSelecionadaUI==4) cTemp = new Professor();
            
            g.drawString("Atributos: " + cTemp.descAtributos, 200, 450); g.drawString("Skill Ativa: " + cTemp.descSkill, 200, 490);
            desenharBotao(g, "CONFIRMAR", 1000, 600, 200, 60, false);
        }
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(100, 0, 0)); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 60)); g.drawString("GAME OVER", 450, 300);
        desenharBotao(g, "VOLTAR AO MENU", 500, 500, 280, 60, false);
    }
    
    private void desenharMiniInventario(Graphics2D g, HeroiGUI h) {
        g.setColor(Color.DARK_GRAY); g.fillRect(20, 540, 60, 60); g.fillRect(90, 540, 60, 60); g.fillRect(160, 540, 60, 60);
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2)); g.drawRect(20, 540, 60, 60); g.drawRect(90, 540, 60, 60); g.drawRect(160, 540, 60, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 10)); g.drawString("ARMA", 30, 615); g.drawString("ARMAD.", 95, 615); g.drawString("ATIVO", 170, 615);
        
        // Desenha a ÍCONE DO ITEM dentro do quadradinho!
        if (h.armaEquipada != null && h.armaEquipada.icone != null) g.drawImage(h.armaEquipada.icone, 22, 542, 56, 56, null); 
        if (h.armaduraEquipada != null && h.armaduraEquipada.icone != null) g.drawImage(h.armaduraEquipada.icone, 92, 542, 56, 56, null); 
        if (h.ativoEquipado != null && h.ativoEquipado.icone != null) g.drawImage(h.ativoEquipado.icone, 162, 542, 56, 56, null); 
    }

    private void desenharLogBatalha(Graphics2D g) {
        if(logBatalha.isEmpty()) return;
        g.setColor(new Color(0, 0, 0, 180)); g.fillRect(820, 380, 440, 120);
        g.setColor(Color.WHITE); g.drawRect(820, 380, 440, 120);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        
        int yText = 410;
        for(String msg : logBatalha) { g.drawString("> " + msg, 830, yText); yText += 40; }
    }

    private void desenharCombate(Graphics2D g) {
        if (bgCombate != null) g.drawImage(bgCombate, 0, 0, 1280, 720, null); else { g.setColor(new Color(50, 0, 0)); g.fillRect(0, 0, 1280, 720); }

        for(int i=0; i < inimigos.size(); i++) {
            InimigoGUI ini = inimigos.get(i);
            int x = 1280/(inimigos.size()+1) * (i+1) - 140;
            
            // Lógica do PISCAR! Se tiver tomando dano, não desenha (pisca)
            if (ini.timerPiscar % 10 < 5) {
                Image spr = imgInimigos[ini.spriteId];
                if (spr != null) g.drawImage(spr, x, 150, 280, 280, null); else { g.setColor(Color.RED); g.fillRoundRect(x, 150, 280, 280, 20, 20); }
            }
            
            g.setColor(Color.BLACK); g.fillRect(x, 100, 280, 40);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 16));
            String tipoDano = (ini.tipoAtaque == 1) ? "[SOFTWARE]" : "[HARDWARE]";
            g.drawString(tipoDano + " " + ini.nome + " ("+ini.status.hp+" HP)", x + 10, 125);
            
            if(escolhendoAlvo) { g.setColor(Color.YELLOW); g.setStroke(new BasicStroke(3)); g.drawRect(x, 150, 280, 280); }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        desenharLogBatalha(g); // Mostra o log lateral
        
        if (!turnoInimigo && !party.isEmpty() && !bloqueiaClique) {
            HeroiGUI h = party.get(jogadorTurnoAtual);
            
            g.setColor(new Color(0, 0, 0, 180)); g.fillRect(10, 10, 350, 100);
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 20)); g.drawString("Vez de: " + h.nome, 20, 40);
            g.setColor(Color.WHITE); g.drawString("HP: " + h.status.hp + " | " + h.classe.nomeClasse, 20, 70);
            if (h.fugiuNaUltima) { g.setColor(Color.RED); g.drawString("DEBUFF: -50% Dano!", 20, 95); }

            desenharMiniInventario(g, h);

            if (escolhendoAlvo) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("SELECIONE O ALVO CLICANDO NO INIMIGO", 350, 630);
                desenharBotao(g, "CANCELAR", 1050, 560, 180, 80, false);
            }
            else if (menuItensAberto) {
                for(int i=0; i<Math.min(h.mochila.size(), 4); i++) { 
                    desenharBotao(g, h.mochila.get(i).nome, 250 + (i*160), 540, 150, 60, itemFocado == h.mochila.get(i));
                }
                
                if (subMenuItem && itemFocado != null) {
                    g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16)); g.drawString("Efeito: " + itemFocado.descricao, 250, 630);
                    desenharBotao(g, itemFocado.tipo == 0 ? "USAR" : "EQUIPAR", 250, 650, 120, 40, false);
                    desenharBotao(g, "DESCARTAR", 390, 650, 120, 40, false);
                    desenharBotao(g, "CANCELAR", 530, 650, 120, 40, false);
                }
                desenharBotao(g, "VOLTAR", 1050, 560, 180, 80, false);
            } 
            else if (menuStatusAberto) {
                g.setColor(new Color(0, 0, 0, 220)); g.fillRect(300, 100, 680, 400); g.setColor(Color.WHITE); g.drawRect(300, 100, 680, 400);
                if (h.sprite != null) g.drawImage(h.sprite, 320, 150, 159, 300, null);
                g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("STATUS DE " + h.nome.toUpperCase(), 500, 150);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                g.drawString("Classe: " + h.classe.nomeClasse, 500, 200); g.drawString("HP: " + h.status.hp + " / " + h.status.hpMax, 500, 240);
                g.drawString("Hardware(Fís): " + h.status.hardware + (h.armaEquipada!=null?"(+"+h.armaEquipada.poder+")":""), 500, 280);
                g.drawString("Software(Mag): " + h.status.software, 500, 320);
                g.drawString("Manutenção: " + h.status.manutencao + (h.armaduraEquipada!=null?"(+"+h.armaduraEquipada.poder+")":""), 500, 360);
                g.drawString("Firewall: " + h.status.firewall, 500, 400);
                desenharBotao(g, "FECHAR", 540, 560, 240, 80, false);
            } 
            else {
                desenharBotao(g, "ATACAR", 250, 560, 180, 80, false); desenharBotao(g, "MOCHILA", 450, 560, 180, 80, false);
                if(!h.skillUsadaNoAndar) desenharBotao(g, "SKILL", 650, 560, 180, 80, false); else { g.setColor(Color.DARK_GRAY); g.fillRect(650, 560, 180, 80); }
                desenharBotao(g, "STATUS", 850, 560, 180, 80, false); 
                
                // Botão de Fugir desabilita se ele já tentou neste andar
                if(!h.tentouFugirNoAndar) desenharBotao(g, "FUGIR", 1050, 560, 180, 80, false);
                else { g.setColor(Color.DARK_GRAY); g.fillRect(1050, 560, 180, 80); }
            }
        }
    }

    private void desenharLoja(Graphics2D g) {
        if (bgLoja != null) g.drawImage(bgLoja, 0, 0, 1280, 720, null); else { g.setColor(new Color(0, 50, 50)); g.fillRect(0, 0, 1280, 720); }

        Image npcImg = lojaLendaria ? spriteDiegao : spriteMarcao;
        if (npcImg != null) g.drawImage(npcImg, 100, 180, 159, 300, null);

        g.setColor(Color.WHITE); g.fillRect(450, 30, 400, 80);
        g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 25));
        g.drawString(lojaLendaria ? "LOJA LENDÁRIA DO DIEGÃO" : "LOJA DO MARCÃO", 480, 80);

        for(int i=0; i<3; i++) {
            int x = 450 + (i*200);
            desenharBotaoSprite(g, imgItens[i], x, 150, 150, 150, false);
            g.setColor(Color.BLACK); g.fillRect(x, 310, 150, 100);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14));
            if(itensLojaAtual[i] != null) {
                g.drawString(itensLojaAtual[i].nome, x+10, 335);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(itensLojaAtual[i].descricao, x+10, 360);
                
                // Impede que o fujão compre itens
                if(!party.get(jogadorTurnoAtual).fugiuDestaBatalha) desenharBotao(g, "PEGAR", x, 420, 150, 40, false);
                else { g.setColor(Color.RED); g.drawString("Fujão Bloqueado!", x+10, 440); }
            }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Aperte PEGAR em UM item para o Herói: " + party.get(jogadorTurnoAtual).nome, 50, 560);
        
        desenharBotao(g, "Próximo Andar", 800, 560, 200, 60, false);
        desenharBotao(g, "Sair do Jogo", 1050, 560, 150, 60, false);
    }

    private void desenharBotao(Graphics2D g, String t, int x, int y, int w, int h, boolean selecionado) {
        boolean hover = (!bloqueiaClique && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h);
        g.setColor(hover || selecionado ? new Color(70, 70, 70) : new Color(30, 30, 30)); g.fillRect(x, y, w, h);
        g.setColor(hover || selecionado ? Color.WHITE : Color.GRAY); g.setStroke(new BasicStroke(hover || selecionado ? 4 : 2)); g.drawRect(x, y, w, h);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics(); g.drawString(t, x + (w - fm.stringWidth(t)) / 2, y + ((h - fm.getHeight()) / 2) + fm.getAscent());
    }
    
    private void desenharBotaoSprite(Graphics2D g, Image img, int x, int y, int w, int h, boolean selecionado) {
        if (img != null) g.drawImage(img, x, y, w, h, null); else { g.setColor(Color.BLUE); g.fillRect(x, y, w, h); }
        if (selecionado) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4)); g.drawRect(x, y, w, h); }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (bloqueiaClique) return; 
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
            if (mx > 20 && mx < 170 && my > 20 && my < 60) { estadoAtual = Estado.MENU; } // VOLTAR
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } // SAIR
            if (mx > 500 && mx < 780 && my > 300 && my < 360) { qtdJogadores = 1; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 400 && my < 460) { qtdJogadores = 2; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { qtdJogadores = 3; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
        }
        else if (estadoAtual == Estado.SELECAO_PERSONAGEM) {
            if (mx > 20 && mx < 170 && my > 20 && my < 60) { estadoAtual = Estado.MODO_JOGO; party.clear(); heroiSelecionadoUI = -1;} 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            
            if (mx > 250 && mx < 409 && my > 200 && my < 500) heroiSelecionadoUI = 0;
            if (mx > 550 && mx < 709 && my > 200 && my < 500) heroiSelecionadoUI = 1;
            if (mx > 850 && mx < 1009 && my > 200 && my < 500) heroiSelecionadoUI = 2;
            
            if (heroiSelecionadoUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI h = (heroiSelecionadoUI == 0) ? new Matheus() : (heroiSelecionadoUI == 1) ? new Lucas() : new Elvis();
                h.sprite = (heroiSelecionadoUI == 0) ? imgMatheus : (heroiSelecionadoUI == 1) ? imgLucas : imgElvis;
                party.add(h); heroiSelecionadoUI = -1; estadoAtual = Estado.SELECAO_CLASSE; 
            }
        }
        else if (estadoAtual == Estado.SELECAO_CLASSE) {
            if (mx > 20 && mx < 170 && my > 20 && my < 60) { party.remove(party.size() - 1); estadoAtual = Estado.SELECAO_PERSONAGEM; classeSelecionadaUI = -1; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); }
            
            if (mx > 200 && mx < 450 && my > 200 && my < 260) classeSelecionadaUI = 0;
            if (mx > 500 && mx < 750 && my > 200 && my < 260) classeSelecionadaUI = 1;
            if (mx > 800 && mx < 1050 && my > 200 && my < 260) classeSelecionadaUI = 2;
            if (mx > 350 && mx < 600 && my > 300 && my < 360) classeSelecionadaUI = 3;
            if (mx > 650 && mx < 900 && my > 300 && my < 360) classeSelecionadaUI = 4;
            
            if (classeSelecionadaUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI heroiAtual = party.get(party.size() - 1); 
                switch(classeSelecionadaUI) {
                    case 0: heroiAtual.setClasse(new Infra()); break; case 1: heroiAtual.setClasse(new JavaChampion()); break;
                    case 2: heroiAtual.setClasse(new HackerMan()); break; case 3: heroiAtual.setClasse(new DonoLanHouse()); break;
                    case 4: heroiAtual.setClasse(new Professor()); break;
                }
                classeSelecionadaUI = -1;
                if (party.size() >= qtdJogadores) gerarAndarDeCombate(); else estadoAtual = Estado.SELECAO_PERSONAGEM; 
            }
        }
        else if (estadoAtual == Estado.GAME_OVER) {
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { party.clear(); andarTotal = 1; batalhasSeguidas = 0; estadoAtual = Estado.MENU; }
        }
        else if (estadoAtual == Estado.COMBATE && !turnoInimigo) {
            HeroiGUI p = party.get(jogadorTurnoAtual);

            if (menuStatusAberto) { if (mx > 540 && mx < 780 && my > 560 && my < 640) menuStatusAberto = false; return; }
            if (menuItensAberto) {
                for(int i=0; i<Math.min(p.mochila.size(), 4); i++) {
                    if (mx > 250+(i*160) && mx < 400+(i*160) && my > 540 && my < 600) { itemFocado = p.mochila.get(i); subMenuItem = true; }
                }
                
                if (subMenuItem && itemFocado != null) {
                    if (mx > 250 && mx < 370 && my > 650 && my < 690) { // USAR EQUIPAR
                        if (itemFocado.tipo == 0) p.status.hp = Math.min(p.status.hpMax, p.status.hp + itemFocado.poder);
                        else if (itemFocado.tipo == 1) p.armaEquipada = itemFocado;
                        else p.armaduraEquipada = itemFocado;
                        p.mochila.remove(itemFocado); addLog(p.nome + " utilizou " + itemFocado.nome + "!", () -> avancarTurno());
                    }
                    if (mx > 390 && mx < 510 && my > 650 && my < 690) { p.mochila.remove(itemFocado); itemFocado = null; subMenuItem = false; repaint(); } // DESCARTA
                    if (mx > 530 && mx < 650 && my > 650 && my < 690) { itemFocado = null; subMenuItem = false; repaint(); } // CANCELA SUB
                }
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { menuItensAberto = false; itemFocado = null; subMenuItem = false; }
                return;
            }

            if (escolhendoAlvo) {
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) escolhendoAlvo = false; 
                for(int i=0; i < inimigos.size(); i++) {
                    int x = 1280/(inimigos.size()+1) * (i+1) - 140;
                    if (mx > x && mx < x+280 && my > 150 && my < 430) { addLog(p.atacarBasico(inimigos.get(i)), () -> avancarTurno()); }
                }
                return;
            }

            if (mx > 250 && mx < 430 && my > 560 && my < 640) escolhendoAlvo = true; // ATACAR
            if (mx > 450 && mx < 630 && my > 560 && my < 640) { if(p.mochila.size()>0) menuItensAberto = true; else addLog("Mochila Vazia!", null); }
            if (!p.skillUsadaNoAndar && mx > 650 && mx < 830 && my > 560 && my < 640) { p.skillUsadaNoAndar = true; addLog(p.classe.usarSkill(p, inimigos), () -> avancarTurno()); }
            if (mx > 850 && mx < 1030 && my > 560 && my < 640) menuStatusAberto = true; // STATUS
            
            if (!p.tentouFugirNoAndar && mx > 1050 && mx < 1230 && my > 560 && my < 640) { 
                p.tentouFugirNoAndar = true; p.fugiuDestaBatalha = true;
                addLog(p.nome + " fugiu! Cuidado com as costas...", () -> avancarTurno());
            }
        }
        else if (estadoAtual == Estado.LOJA) {
            HeroiGUI pAtual = party.get(jogadorTurnoAtual);
            
            if(!pAtual.fugiuDestaBatalha) { // O Fujão não clica nos itens da mesa
                for(int i=0; i<3; i++) {
                    int x = 450 + (i*200);
                    if (itensLojaAtual[i] != null && mx > x && mx < x+150 && my > 420 && my < 460) {
                        pAtual.mochila.add(itensLojaAtual[i]);
                        addLog(pAtual.nome + " pegou: " + itensLojaAtual[i].nome + "!", null);
                        itensLojaAtual[i] = null; 
                        
                        jogadorTurnoAtual++;
                        if (jogadorTurnoAtual >= party.size()) { addLog("Todos agiram! Partindo...", () -> gerarAndarDeCombate()); }
                    }
                }
            } else {
                // Se for o turno do fujão, ele pula automático
                jogadorTurnoAtual++;
                if (jogadorTurnoAtual >= party.size()) { gerarAndarDeCombate(); }
            }
            
            if (mx > 800 && mx < 1000 && my > 560 && my < 620) gerarAndarDeCombate();
            if (mx > 1050 && mx < 1200 && my > 560 && my < 620) System.exit(0);
        }
    }
    public void mouseClicked(MouseEvent e) {} public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
}
package tichampions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class MotorGrafico extends JPanel implements ActionListener, MouseListener {

    enum Estado { MENU, OPCOES, MODO_JOGO, SELECAO_PERSONAGEM, SELECAO_CLASSE, COMBATE, LOJA, GAME_OVER }
    Estado estadoAtual = Estado.MENU;

    Timer timerGameLoop; double tempoAnimacao = 0;
    double scaleX = 1.0, scaleY = 1.0; int mouseX = 0, mouseY = 0;

    ArrayList<HeroiGUI> party = new ArrayList<>();
    ArrayList<InimigoGUI> inimigos = new ArrayList<>();
    int qtdJogadores = 1, jogadorTurnoAtual = 0; boolean turnoInimigo = false;
    
    int heroiSelecionadoUI = -1, classeSelecionadaUI = -1;
    
    // VARIÁVEIS DE CONTROLE
    boolean escolhendoAlvo = false, menuItensAberto = false, menuStatusAberto = false, subMenuItem = false;
    boolean mostrandoTutorial = false, dropResolucao = false, turnoExtraLanHouse = false;
    Item itemFocado = null; 
    
    ArrayList<String> logBatalha = new ArrayList<>();
    Timer timerEsperaAcao; boolean bloqueiaClique = false;

    int andarTotal = 1, batalhasSeguidas = 0; boolean lojaLendaria = false;
    Random rng = new Random();

    Image bgMenu, bgLoja, spriteMarcao, spriteDiegao;
    Image[] bgsCombate = new Image[3]; 
    Image imgMatheus, imgLucas, imgElvis;
    Image[] imgInimigos = new Image[5]; 
    Image[] imgItens = new Image[6]; 
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
            bgMenu = ImageIO.read(new File("sprites/backgrounds/bg_menu.png")); 
            bgLoja = ImageIO.read(new File("sprites/backgrounds/bg_loja.png")); 
            bgsCombate[0] = ImageIO.read(new File("sprites/backgrounds/bg_combate1.png"));
            bgsCombate[1] = ImageIO.read(new File("sprites/backgrounds/bg_combate2.png"));
            bgsCombate[2] = ImageIO.read(new File("sprites/backgrounds/bg_combate3.png"));
            
            spriteMarcao = ImageIO.read(new File("sprites/npc/marcao.png")); spriteDiegao = ImageIO.read(new File("sprites/npc/diegao.png")); 
            imgMatheus = ImageIO.read(new File("sprites/personagens/matheus.png")); imgLucas = ImageIO.read(new File("sprites/personagens/lucas.png")); 
            imgElvis = ImageIO.read(new File("sprites/personagens/elvis.png"));
            
            imgInimigos[0] = ImageIO.read(new File("sprites/inimigos/estagiario.png")); imgInimigos[1] = ImageIO.read(new File("sprites/inimigos/sql_injection.png"));
            imgInimigos[2] = ImageIO.read(new File("sprites/inimigos/hardware_curto.png")); imgInimigos[3] = ImageIO.read(new File("sprites/inimigos/boss_arquiteto.png"));
            
            imgItens[0] = ImageIO.read(new File("sprites/itens/cafe.png")); 
            imgItens[1] = ImageIO.read(new File("sprites/itens/placa_video.png"));
            imgItens[2] = ImageIO.read(new File("sprites/itens/nobreak.png"));
            imgItens[3] = ImageIO.read(new File("sprites/itens/ferro_solda.png"));
            imgItens[4] = ImageIO.read(new File("sprites/itens/camisa_evento.png"));
            imgItens[5] = ImageIO.read(new File("sprites/itens/memoria_enferrujada.png"));
        } catch (Exception e) {}
    }

    private void addLog(String msg, Runnable proximaAcao) {
        logBatalha.add(msg);
        if (logBatalha.size() > 5) logBatalha.remove(0); // Agora guarda até 5 linhas
        
        bloqueiaClique = true;
        menuItensAberto = false; menuStatusAberto = false; subMenuItem = false;
        
        if (timerEsperaAcao != null && timerEsperaAcao.isRunning()) timerEsperaAcao.stop();
        timerEsperaAcao = new Timer(1500, e -> {
            bloqueiaClique = false; if (proximaAcao != null) proximaAcao.run(); repaint();
        });
        timerEsperaAcao.setRepeats(false); timerEsperaAcao.start();
    }

    private void gerarItensLoja() {
        int power = 5 + (andarTotal * 2);
        ArrayList<Item> pool = new ArrayList<>();
        pool.add(new Item("Café Forte", "Cura " + (20+power*2) + " HP", 0, 20+power*2, imgItens[0]));
        pool.add(new Item("Placa RTX", "+" + power + " Hardware", 1, power, imgItens[1]));
        pool.add(new Item("Nobreak", "+" + power + " Manutenção", 2, power, imgItens[2]));
        pool.add(new Item("Ferro de Solda", "+" + (power+3) + " Hardware", 1, power+3, imgItens[3]));
        pool.add(new Item("Camisa de Evento", "+" + power + " Firewall", 4, power, imgItens[4]));
        pool.add(new Item("Memória Velha", "+" + (power+5) + " Software", 3, power+5, imgItens[5]));

        Collections.shuffle(pool, rng);
        itensLojaAtual[0] = pool.get(0); itensLojaAtual[1] = pool.get(1); itensLojaAtual[2] = pool.get(2);
    }

    private void gerarAndarDeCombate() {
        inimigos.clear(); batalhasSeguidas++;
        if (batalhasSeguidas >= 5) {
            inimigos.add(new InimigoGUI(andarTotal, new Status(300+(andarTotal*10), 20+andarTotal, 0, 15, 15), 3, 0));
            batalhasSeguidas = 0;
        } else {
            int qInimigos = (rng.nextInt(100) < 10) ? qtdJogadores + rng.nextInt(2) + 1 : qtdJogadores;
            for(int i=0; i < qInimigos; i++) {
                int idSpr = rng.nextInt(3); int tAtaque = (idSpr == 1) ? 1 : 0; 
                inimigos.add(new InimigoGUI(andarTotal, new Status(40+(andarTotal*10), 10+andarTotal, 10+andarTotal, 2, 2), idSpr, tAtaque));
            }
        }
        logBatalha.clear(); turnoExtraLanHouse = false;
        for(HeroiGUI h : party) { 
            h.skillUsadaNoAndar = false; h.fugiuDestaBatalha = false; h.tentouFugirNoAndar = false; 
            logBatalha.add(h.aplicarPassivaTurno()); 
        }
        jogadorTurnoAtual = 0; turnoInimigo = false; estadoAtual = Estado.COMBATE; verificarTurnoValido();
    }

    private void avancarTurno() {
        inimigos.removeIf(i -> i.status.hp <= 0);
        escolhendoAlvo = false; 
        
        if (inimigos.isEmpty()) {
            for(HeroiGUI h : party) if(!h.fugiuDestaBatalha) h.fugiuNaUltima = false; 
            andarTotal++; lojaLendaria = (batalhasSeguidas == 0); 
            gerarItensLoja();
            addLog("Batalha Vencida! Avançando...", () -> { prepararLoja(); estadoAtual = Estado.LOJA; }); return;
        }
        
        int vivos = 0, fugiram = 0;
        for(HeroiGUI h : party) { if(h.status.hp > 0 && !h.fugiuDestaBatalha) vivos++; if(h.fugiuDestaBatalha) fugiram++; }
        
        if (vivos == 0) {
            if (fugiram > 0) addLog("Todos fugiram! Indo à Loja...", () -> { gerarItensLoja(); prepararLoja(); estadoAtual = Estado.LOJA; });
            else addLog("GAME OVER! A equipe foi derrotada.", () -> estadoAtual = Estado.GAME_OVER);
            return;
        }

        if (!turnoInimigo) {
            if (turnoExtraLanHouse) {
                turnoExtraLanHouse = false; 
                addLog(party.get(jogadorTurnoAtual).nome + " ganhou +1 Ficha! Turno Extra!", null);
            } else {
                jogadorTurnoAtual++;
                if (jogadorTurnoAtual >= party.size()) { turnoInimigo = true; executarTurnoInimigo(); } 
                else verificarTurnoValido();
            }
        } else { turnoInimigo = false; jogadorTurnoAtual = 0; verificarTurnoValido(); }
    }

    private void prepararLoja() {
        jogadorTurnoAtual = 0;
        while(jogadorTurnoAtual < party.size() && party.get(jogadorTurnoAtual).fugiuDestaBatalha) jogadorTurnoAtual++;
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

        for(InimigoGUI atacante : inimigos) {
            if(alvosVivos.isEmpty()) break;
            HeroiGUI alvo = alvosVivos.get(rng.nextInt(alvosVivos.size()));
            atacante.ativarAtaqueAnim(); 
            logBatalha.add(atacante.atacar(alvo));
            if(logBatalha.size() > 5) logBatalha.remove(0);
            if(alvo.status.hp <= 0) alvosVivos.remove(alvo);
        }
        
        // Bloqueia a tela por 2 segundos para dar tempo de ler todos os ataques
        bloqueiaClique = true;
        if (timerEsperaAcao != null && timerEsperaAcao.isRunning()) timerEsperaAcao.stop();
        timerEsperaAcao = new Timer(2000, e -> { bloqueiaClique = false; avancarTurno(); repaint(); });
        timerEsperaAcao.setRepeats(false); timerEsperaAcao.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) { 
        tempoAnimacao += 0.05; 
        for(InimigoGUI ini : inimigos) { 
            if(ini.timerPiscar > 0) ini.timerPiscar--; 
            if(ini.timerAtacar > 0) ini.timerAtacar--;
        } 
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

    private void desenharBotaoHover(Graphics2D g, String t, int x, int y, int w, int h, boolean flutuar) {
        boolean hover = (!bloqueiaClique && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h);
        int ofsY = (hover && flutuar) ? (int)(Math.sin(tempoAnimacao) * 5) : 0;
        
        g.setColor(hover ? new Color(70, 70, 70) : new Color(30, 30, 30)); g.fillRect(x, y - ofsY, w, h);
        g.setColor(hover ? Color.WHITE : Color.GRAY); g.setStroke(new BasicStroke(hover ? 4 : 2)); g.drawRect(x, y - ofsY, w, h);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics(); g.drawString(t, x + (w - fm.stringWidth(t)) / 2, (y - ofsY) + ((h - fm.getHeight()) / 2) + fm.getAscent());
    }
    
    private void desenharBotaoSprite(Graphics2D g, Image img, int x, int y, int w, int h, boolean selecionado) {
        if (img != null) g.drawImage(img, x, y, w, h, null); else { g.setColor(Color.DARK_GRAY); g.fillRect(x, y, w, h); }
        if (selecionado) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4)); g.drawRect(x, y, w, h); }
    }
    
    private void desenharHUDGlobal(Graphics2D g, boolean mostrarVoltar) {
        if(mostrarVoltar) desenharBotaoHover(g, "VOLTAR", 20, 640, 150, 40, true);
        desenharBotaoHover(g, "SAIR", 1100, 20, 150, 40, true);
    }

    private void desenharMenu(Graphics2D g) {
        if (bgMenu != null) g.drawImage(bgMenu, 0, 0, 1280, 720, null); else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 1280, 720); }
        int btnY = 350 + (int)(Math.sin(tempoAnimacao) * 10);
        desenharBotaoHover(g, "INICIAR JOGO", 540, btnY, 200, 60, true); desenharBotaoHover(g, "OPÇÕES", 540, btnY + 80, 200, 60, true);
        desenharBotaoHover(g, "SAIR", 540, btnY + 160, 200, 60, true);
    }
    
    private void desenharOpcoes(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("MENU DE OPÇÕES", 450, 100); desenharHUDGlobal(g, true);
        
        desenharBotaoHover(g, "TUTORIAL", 500, 200, 280, 60, true);
        desenharBotaoHover(g, "RESOLUÇÃO ▼", 500, 300, 280, 60, true);
        
        if (dropResolucao) {
            desenharBotaoHover(g, "800x600", 500, 360, 280, 40, false);
            desenharBotaoHover(g, "1280x720", 500, 400, 280, 40, false);
            desenharBotaoHover(g, "1920x1080", 500, 440, 280, 40, false);
        }
        
        if (mostrandoTutorial) {
            g.setColor(new Color(0,0,0,230)); g.fillRect(200, 150, 880, 450); g.setColor(Color.WHITE); g.drawRect(200, 150, 880, 450);
            g.setFont(new Font("Arial", Font.BOLD, 26)); g.drawString("TUTORIAL - MECÂNICAS DO JOGO", 400, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("• HARDWARE: Dano Físico. A defesa contra ele é a sua Manutenção.", 250, 250);
            g.drawString("• SOFTWARE: Dano Mágico. A defesa contra ele é o seu Firewall.", 250, 290);
            g.drawString("• ITENS DA LOJA: Máx de 3 Equipamentos instalados ao mesmo tempo.", 250, 330);
            g.drawString("• CONSUMÍVEIS: Você só pode carregar 1 consumível de uso por vez.", 250, 370);
            g.drawString("• FUGIR: Você pula o turno e perde -50% de Ataque. Fujões não vão na loja.", 250, 410);
            
            desenharBotaoHover(g, "FECHAR TUTORIAL", 500, 500, 280, 60, true);
        }
    }
    
    private void desenharModoJogo(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("QUANTOS JOGADORES?", 400, 200); desenharHUDGlobal(g, true);
        desenharBotaoHover(g, "1 JOGADOR", 540, 300, 200, 60, true); desenharBotaoHover(g, "2 JOGADORES", 540, 400, 200, 60, true);
        desenharBotaoHover(g, "3 JOGADORES", 540, 500, 200, 60, true);
    }

    private void desenharSelecaoPersonagem(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); desenharHUDGlobal(g, true);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SELECIONE O PLAYER " + (party.size() + 1), 400, 100);
        
        Image[] imgs = {imgMatheus, imgLucas, imgElvis}; String[] nomes = {"Matheus", "Lucas", "Elvis"}; int[] posX = {250, 550, 850};
        for(int i=0; i<3; i++) {
            boolean hover = (!bloqueiaClique && mouseX > posX[i] && mouseX < posX[i]+159 && mouseY > 200 && mouseY < 500);
            int ofs = hover ? (int)(Math.sin(tempoAnimacao)*5) : 0;
            if (imgs[i] != null) g.drawImage(imgs[i], posX[i], 200 - ofs, 159, 300, null); else { g.setColor(Color.BLUE); g.fillRect(posX[i], 200 - ofs, 159, 300); }
            if (heroiSelecionadoUI == i) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4)); g.drawRect(posX[i], 200 - ofs, 159, 300); }
            g.setFont(new Font("Arial", Font.BOLD, 24)); g.drawString(nomes[i], posX[i]+30, 530 - ofs);
        }
        
        if (heroiSelecionadoUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            if(heroiSelecionadoUI == 0) { g.drawString("HP: 80 | Hard: 5 | Soft: 25", 220, 560); g.drawString("Passiva: Alterna Buffs", 220, 590); }
            if(heroiSelecionadoUI == 1) { g.drawString("HP: 120 | Hard: 15 | Soft: 2", 520, 560); g.drawString("Passiva: Buff Defesa Início", 520, 590); }
            if(heroiSelecionadoUI == 2) { g.drawString("HP: 100 | Hard: 20 | Soft: 5", 820, 560); g.drawString("Passiva: Sobrevive a 0 HP", 820, 590); }
            desenharBotaoHover(g, "AVANÇAR", 1000, 600, 200, 60, true);
        }
    }
    
    private void desenharSelecaoClasse(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); desenharHUDGlobal(g, true);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("ESCOLHA SUA CLASSE", 420, 100);
        
        desenharBotaoHover(g, "Infra", 200, 200, 250, 60, true); desenharBotaoHover(g, "Java Champion", 500, 200, 250, 60, true);
        desenharBotaoHover(g, "HackerMan", 800, 200, 250, 60, true); desenharBotaoHover(g, "LanHouse", 350, 300, 250, 60, true);
        desenharBotaoHover(g, "Professor", 650, 300, 250, 60, true);

        if (classeSelecionadaUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 20)); ClasseRPG cTemp = null;
            if(classeSelecionadaUI==0) cTemp = new Infra(); if(classeSelecionadaUI==1) cTemp = new JavaChampion();
            if(classeSelecionadaUI==2) cTemp = new HackerMan(); if(classeSelecionadaUI==3) cTemp = new DonoLanHouse();
            if(classeSelecionadaUI==4) cTemp = new Professor();
            
            g.drawString("Atributos: " + cTemp.descAtributos, 200, 450); g.drawString("Skill Ativa: " + cTemp.descSkill, 200, 490);
            desenharBotaoHover(g, "CONFIRMAR", 1000, 600, 200, 60, true);
        }
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(100, 0, 0)); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 60)); g.drawString("GAME OVER", 450, 300);
        desenharBotaoHover(g, "VOLTAR AO MENU", 500, 500, 280, 60, true);
    }
    
    private void desenharMiniInventario(Graphics2D g, HeroiGUI h) {
        g.setColor(Color.DARK_GRAY); g.fillRect(20, 540, 60, 60); g.fillRect(90, 540, 60, 60); g.fillRect(160, 540, 60, 60);
        g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2)); g.drawRect(20, 540, 60, 60); g.drawRect(90, 540, 60, 60); g.drawRect(160, 540, 60, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 10)); g.drawString("ARMA", 30, 615); g.drawString("ARMAD.", 95, 615); g.drawString("ATIVO", 170, 615);
        
        if (h.armaEquipada != null && h.armaEquipada.icone != null) g.drawImage(h.armaEquipada.icone, 22, 542, 56, 56, null); 
        if (h.armaduraEquipada != null && h.armaduraEquipada.icone != null) g.drawImage(h.armaduraEquipada.icone, 92, 542, 56, 56, null); 
        
        Item cons = h.mochila.stream().filter(it -> it.tipo == 0).findFirst().orElse(null);
        if (cons != null && cons.icone != null) g.drawImage(cons.icone, 162, 542, 56, 56, null);
    }

    private void desenharLogLateral(Graphics2D g) {
        if(logBatalha.isEmpty()) return;
        g.setColor(new Color(0, 0, 0, 200)); g.fillRect(800, 50, 460, 160);
        g.setColor(Color.WHITE); g.drawRect(800, 50, 460, 160); g.setFont(new Font("Arial", Font.PLAIN, 12)); 
        int yText = 75;
        for(String msg : logBatalha) { g.drawString("> " + msg, 810, yText); yText += 25; }
    }

    private void desenharCombate(Graphics2D g) {
        int bgIndex = ((andarTotal - 1) / 4) % 3; Image bgAtual = bgsCombate[bgIndex];
        if (bgAtual != null) g.drawImage(bgAtual, 0, 0, 1280, 720, null); 
        else { g.setColor(bgIndex==0?new Color(50,0,0) : bgIndex==1?new Color(0,50,0) : new Color(0,0,50)); g.fillRect(0, 0, 1280, 720); }

        for(int i=0; i < inimigos.size(); i++) {
            InimigoGUI ini = inimigos.get(i);
            int x = 1280/(inimigos.size()+1) * (i+1) - 140;
            
            boolean mouseHoverIni = (escolhendoAlvo && mouseX > x && mouseX < x+280 && mouseY > 150 && mouseY < 430);
            int floatY = mouseHoverIni ? (int)(Math.sin(tempoAnimacao) * 5) : 0;
            int animAtaqueX = (ini.timerAtacar > 0) ? (int)(Math.sin(ini.timerAtacar) * 15) : 0; 
            
            if (ini.timerPiscar == 0 || (ini.timerPiscar / 5) % 2 == 0) {
                Image spr = imgInimigos[ini.spriteId];
                if (spr != null) g.drawImage(spr, (x + animAtaqueX), (150 - floatY), 280, 280, null); 
                else { g.setColor(Color.RED); g.fillRoundRect((x + animAtaqueX), 150 - floatY, 280, 280, 20, 20); }
            }
            
            g.setColor(Color.BLACK); g.fillRect(x + animAtaqueX, 100 - floatY, 280, 40);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 16));
            String tipoDano = (ini.tipoAtaque == 1) ? "[SOFTWARE]" : "[HARDWARE]";
            g.drawString(tipoDano + " " + ini.nome + " ("+ini.status.hp+" HP)", (x + animAtaqueX) + 10, 125 - floatY);
            if(escolhendoAlvo) { g.setColor(Color.YELLOW); g.setStroke(new BasicStroke(3)); g.drawRect(x, 150 - floatY, 280, 280); }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        desenharLogLateral(g); 
        
        if (!turnoInimigo && !party.isEmpty() && !bloqueiaClique) {
            HeroiGUI h = party.get(jogadorTurnoAtual);
            
            g.setColor(new Color(0, 0, 0, 180)); g.fillRect(10, 10, 350, 100);
            g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 20)); g.drawString("Vez de: " + h.nome, 20, 40);
            g.setColor(Color.WHITE); g.drawString("HP: " + h.status.hp + " | " + h.classe.nomeClasse, 20, 70);
            if (h.fugiuNaUltima) { g.setColor(Color.RED); g.drawString("DEBUFF: -50% Dano!", 20, 95); }

            desenharMiniInventario(g, h);

            if (escolhendoAlvo) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("SELECIONE O ALVO!", 350, 630);
                desenharBotaoHover(g, "CANCELAR", 1050, 560, 180, 80, true);
            }
            else if (menuItensAberto) {
                for(int i=0; i<Math.min(h.mochila.size(), 4); i++) desenharBotaoHover(g, h.mochila.get(i).nome, 250 + (i*160), 540, 150, 60, true);
                
                if (subMenuItem && itemFocado != null) {
                    g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16)); g.drawString("Efeito: " + itemFocado.descricao, 250, 630);
                    boolean isEquipado = (itemFocado == h.armaEquipada || itemFocado == h.armaduraEquipada);
                    String msgAcao = (itemFocado.tipo == 0) ? "USAR" : (isEquipado ? "DESEQUIPAR" : "EQUIPAR");
                    
                    desenharBotaoHover(g, msgAcao, 250, 650, 150, 40, true); desenharBotaoHover(g, "DESCARTAR", 420, 650, 150, 40, true);
                    desenharBotaoHover(g, "CANCELAR", 590, 650, 150, 40, true);
                }
                desenharBotaoHover(g, "VOLTAR", 1050, 560, 180, 80, true);
            } 
            else if (menuStatusAberto) {
                g.setColor(new Color(0, 0, 0, 220)); g.fillRect(300, 100, 680, 400); g.setColor(Color.WHITE); g.drawRect(300, 100, 680, 400);
                if (h.sprite != null) g.drawImage(h.sprite, 320, 150, 159, 300, null);
                g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("STATUS DE " + h.nome.toUpperCase(), 500, 150);
                g.setFont(new Font("Arial", Font.PLAIN, 24)); g.drawString("Classe: " + h.classe.nomeClasse, 500, 200); g.drawString("HP: " + h.status.hp + " / " + h.status.hpMax, 500, 240);
                g.drawString("Hardware: " + h.status.hardware + (h.armaEquipada!=null?"(+"+h.armaEquipada.poder+")":""), 500, 280); g.drawString("Software: " + h.status.software, 500, 320);
                g.drawString("Manutenção: " + h.status.manutencao + (h.armaduraEquipada!=null?"(+"+h.armaduraEquipada.poder+")":""), 500, 360); g.drawString("Firewall: " + h.status.firewall, 500, 400);
                desenharBotaoHover(g, "FECHAR STATUS", 540, 560, 240, 80, true);
            } 
            else {
                desenharBotaoHover(g, "ATACAR", 250, 560, 180, 80, true); desenharBotaoHover(g, "MOCHILA", 450, 560, 180, 80, true);
                if(!h.skillUsadaNoAndar) desenharBotaoHover(g, "SKILL", 650, 560, 180, 80, true); else { g.setColor(Color.DARK_GRAY); g.fillRect(650, 560, 180, 80); }
                desenharBotaoHover(g, "STATUS", 850, 560, 180, 80, true); 
                
                if(h.tentouFugirNoAndar) { g.setColor(Color.DARK_GRAY); g.fillRect(1050, 560, 180, 80); } 
                else desenharBotaoHover(g, "FUGIR", 1050, 560, 180, 80, true);
            }
        }
    }

    private void desenharLoja(Graphics2D g) {
        if (bgLoja != null) g.drawImage(bgLoja, 0, 0, 1280, 720, null); else { g.setColor(new Color(0, 50, 50)); g.fillRect(0, 0, 1280, 720); }
        Image npcImg = lojaLendaria ? spriteDiegao : spriteMarcao;
        if (npcImg != null) g.drawImage(npcImg, 100, 180, 159, 300, null);

        g.setColor(Color.WHITE); g.fillRect(450, 30, 400, 80); g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 25));
        g.drawString(lojaLendaria ? "LOJA LENDÁRIA DO DIEGÃO" : "LOJA DO MARCÃO", 480, 80);

        for(int i=0; i<3; i++) {
            int x = 450 + (i*200);
            
            // Puxando icone diretamente do Item
            desenharBotaoSprite(g, (itensLojaAtual[i] != null) ? itensLojaAtual[i].icone : null, x, 150, 150, 150, false);
            
            g.setColor(Color.BLACK); g.fillRect(x, 310, 150, 100); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14));
            if(itensLojaAtual[i] != null) {
                g.drawString(itensLojaAtual[i].nome, x+10, 335); g.setFont(new Font("Arial", Font.PLAIN, 12)); g.drawString(itensLojaAtual[i].descricao, x+10, 360);
                if(jogadorTurnoAtual < party.size() && !party.get(jogadorTurnoAtual).fugiuDestaBatalha) { 
                    desenharBotaoHover(g, "PEGAR", x, 420, 150, 40, true); 
                }
            }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        
        if (jogadorTurnoAtual < party.size()) { 
            if(party.get(jogadorTurnoAtual).fugiuDestaBatalha) g.drawString(party.get(jogadorTurnoAtual).nome + " fugiu e não pode pegar itens! Pressione Próximo Heroi.", 50, 560);
            else g.drawString("Aperte PEGAR em UM item para o Herói: " + party.get(jogadorTurnoAtual).nome, 50, 560); 
            
            desenharBotaoHover(g, "MOCHILA ("+party.get(jogadorTurnoAtual).nome+")", 50, 600, 250, 60, true);
            
            String txtProximo = (jogadorTurnoAtual < party.size() - 1) ? "PRÓXIMO HERÓI" : "PRÓXIMO ANDAR";
            desenharBotaoHover(g, txtProximo, 350, 600, 200, 60, true);
        } 
        else { g.drawString("Todos os Heróis aptos já agiram na loja!", 50, 560); }
        
        if (menuItensAberto && jogadorTurnoAtual < party.size()) {
            HeroiGUI p = party.get(jogadorTurnoAtual);
            g.setColor(new Color(0,0,0,200)); g.fillRect(200, 100, 800, 300);
            for(int i=0; i<Math.min(p.mochila.size(), 4); i++) desenharBotaoHover(g, p.mochila.get(i).nome, 250 + (i*160), 120, 150, 60, true);
            if (subMenuItem && itemFocado != null) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16)); g.drawString("Efeito: " + itemFocado.descricao, 250, 220);
                boolean isEquipado = (itemFocado == p.armaEquipada || itemFocado == p.armaduraEquipada);
                String msgAcao = (itemFocado.tipo == 0) ? "USAR" : (isEquipado ? "DESEQUIPAR" : "EQUIPAR");
                desenharBotaoHover(g, msgAcao, 250, 240, 150, 40, true); desenharBotaoHover(g, "DESCARTAR", 420, 240, 150, 40, true);
                desenharBotaoHover(g, "CANCELAR", 590, 240, 150, 40, true);
            }
            desenharBotaoHover(g, "FECHAR MOCHILA", 400, 320, 200, 60, true);
        }
        
        desenharBotaoHover(g, "Sair do Jogo", 1050, 560, 150, 60, true);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (bloqueiaClique) return; 
        int mx = (int)(e.getX() / scaleX); int my = (int)(e.getY() / scaleY);

        if (estadoAtual == Estado.OPCOES) {
            if (mostrandoTutorial) {
                if (mx > 500 && mx < 780 && my > 500 && my < 560) mostrandoTutorial = false; // FECHAR
                return;
            }
            if (dropResolucao) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (mx > 500 && mx < 780 && my > 360 && my < 400) { frame.setSize(800, 600); frame.setLocationRelativeTo(null); dropResolucao = false; }
                else if (mx > 500 && mx < 780 && my > 400 && my < 440) { frame.setSize(1280, 720); frame.setLocationRelativeTo(null); dropResolucao = false; }
                else if (mx > 500 && mx < 780 && my > 440 && my < 480) { frame.setSize(1920, 1080); frame.setLocationRelativeTo(null); dropResolucao = false; }
                else dropResolucao = false; 
                return;
            }
            
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { estadoAtual = Estado.MENU; dropResolucao = false; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            
            if (mx > 500 && mx < 780 && my > 200 && my < 260) mostrandoTutorial = true;
            if (mx > 500 && mx < 780 && my > 300 && my < 360) dropResolucao = !dropResolucao;
            return;
        }

        if (estadoAtual == Estado.MENU) {
            if (mx > 540 && mx < 740 && my > 350 && my < 410) estadoAtual = Estado.MODO_JOGO;
            if (mx > 540 && mx < 740 && my > 430 && my < 490) estadoAtual = Estado.OPCOES;
            if (mx > 540 && mx < 740 && my > 510 && my < 570) System.exit(0);
        }
        else if (estadoAtual == Estado.MODO_JOGO) {
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { estadoAtual = Estado.MENU; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            if (mx > 500 && mx < 780 && my > 300 && my < 360) { qtdJogadores = 1; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 400 && my < 460) { qtdJogadores = 2; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { qtdJogadores = 3; estadoAtual = Estado.SELECAO_PERSONAGEM; party.clear(); }
        }
        else if (estadoAtual == Estado.SELECAO_PERSONAGEM) {
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { estadoAtual = Estado.MODO_JOGO; party.clear(); heroiSelecionadoUI = -1;} 
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
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { party.remove(party.size() - 1); estadoAtual = Estado.SELECAO_PERSONAGEM; classeSelecionadaUI = -1; } 
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
                    boolean isEquipado = (itemFocado == p.armaEquipada || itemFocado == p.armaduraEquipada);
                    if (mx > 250 && mx < 370 && my > 650 && my < 690) { 
                        if (itemFocado.tipo == 0) { p.status.hp = Math.min(p.status.hpMax, p.status.hp + itemFocado.poder); p.mochila.remove(itemFocado); addLog(p.nome + " bebeu " + itemFocado.nome +"!", () -> avancarTurno()); }
                        else if (isEquipado) { if(itemFocado.tipo == 1 || itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null; subMenuItem=false; repaint(); }
                        else { if(itemFocado.tipo == 1 || itemFocado.tipo == 3) p.armaEquipada = itemFocado; else p.armaduraEquipada = itemFocado; subMenuItem=false; repaint(); }
                    }
                    if (mx > 390 && mx < 510 && my > 650 && my < 690) { 
                        if(isEquipado) { if(itemFocado.tipo == 1 || itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null; }
                        p.mochila.remove(itemFocado); itemFocado = null; subMenuItem = false; repaint(); 
                    }
                    if (mx > 530 && mx < 650 && my > 650 && my < 690) { itemFocado = null; subMenuItem = false; repaint(); }
                }
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { menuItensAberto = false; itemFocado = null; subMenuItem = false; }
                return;
            }

            if (escolhendoAlvo) {
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) escolhendoAlvo = false; 
                for(int i=0; i < inimigos.size(); i++) {
                    int x = 1280/(inimigos.size()+1) * (i+1) - 140;
                    if (mx > x && mx < x+280 && my > 150 && my < 430) { 
                        if(p.classe instanceof DonoLanHouse && p.skillUsadaNoAndar) turnoExtraLanHouse = true; // Se for dono de lanhouse com skill ligada, duplo turno!
                        addLog(p.atacarBasico(inimigos.get(i)), () -> avancarTurno()); 
                    }
                }
                return;
            }

            if (mx > 250 && mx < 430 && my > 560 && my < 640) escolhendoAlvo = true; 
            if (mx > 450 && mx < 630 && my > 560 && my < 640) { if(p.mochila.size()>0) menuItensAberto = true; else addLog("Mochila Vazia!", null); }
            if (!p.skillUsadaNoAndar && mx > 650 && mx < 830 && my > 560 && my < 640) { 
                p.skillUsadaNoAndar = true; 
                if (p.classe instanceof DonoLanHouse) turnoExtraLanHouse = true; 
                addLog(p.classe.usarSkill(p, inimigos), () -> avancarTurno()); 
            }
            if (mx > 850 && mx < 1030 && my > 560 && my < 640) menuStatusAberto = true; 
            
            if (!p.tentouFugirNoAndar && mx > 1050 && mx < 1230 && my > 560 && my < 640) { 
                p.fugiuDestaBatalha = true; p.tentouFugirNoAndar = true; 
                addLog(p.nome + " fugiu! Pula a vez e -50% ATK depois!", () -> avancarTurno());
            }
        }
        else if (estadoAtual == Estado.LOJA) {
            if (menuItensAberto && jogadorTurnoAtual < party.size()) {
                HeroiGUI p = party.get(jogadorTurnoAtual);
                for(int i=0; i<Math.min(p.mochila.size(), 4); i++) {
                    if (mx > 250+(i*160) && mx < 400+(i*160) && my > 120 && my < 180) { itemFocado = p.mochila.get(i); subMenuItem = true; }
                }
                if (subMenuItem && itemFocado != null) {
                    boolean isEquipado = (itemFocado == p.armaEquipada || itemFocado == p.armaduraEquipada);
                    if (mx > 250 && mx < 400 && my > 240 && my < 280) { 
                        if (itemFocado.tipo == 0) { p.status.hp = Math.min(p.status.hpMax, p.status.hp + itemFocado.poder); p.mochila.remove(itemFocado); }
                        else if (isEquipado) { if(itemFocado.tipo == 1 || itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null;}
                        else { if(itemFocado.tipo == 1 || itemFocado.tipo == 3) p.armaEquipada = itemFocado; else p.armaduraEquipada = itemFocado;}
                        subMenuItem=false; repaint();
                    }
                    if (mx > 420 && mx < 570 && my > 240 && my < 280) { itemFocado = null; subMenuItem = false; repaint(); } 
                }
                if (mx > 400 && mx < 600 && my > 320 && my < 380) { menuItensAberto = false; itemFocado = null; subMenuItem = false; }
                return;
            }

            if (jogadorTurnoAtual < party.size()) {
                HeroiGUI pAtual = party.get(jogadorTurnoAtual);
                
                if (mx > 50 && mx < 300 && my > 600 && my < 660) { menuItensAberto = true; }
                if (mx > 350 && mx < 550 && my > 600 && my < 660) { 
                    jogadorTurnoAtual++; 
                    if(jogadorTurnoAtual < party.size()) { gerarItensLoja(); prepararLoja(); }
                    else { gerarAndarDeCombate(); }
                }

                if(!pAtual.fugiuDestaBatalha) {
                    for(int i=0; i<3; i++) {
                        int x = 450 + (i*200);
                        if (itensLojaAtual[i] != null && mx > x && mx < x+150 && my > 420 && my < 460) {
                            Item itemDesejado = itensLojaAtual[i];
                            int qtdUsaveis = 0, qtdEquip = 0;
                            for(Item it : pAtual.mochila) { if(it.tipo == 0) qtdUsaveis++; else qtdEquip++; }
                            
                            if (itemDesejado.tipo == 0 && qtdUsaveis >= 1) { addLog("Limite de Consumíveis (1)! Descarte na Mochila.", null); } 
                            else if (itemDesejado.tipo != 0 && qtdEquip >= 3) { addLog("Limite de Equipamentos (3)! Descarte na Mochila.", null); } 
                            else { 
                                pAtual.mochila.add(itemDesejado); 
                                itensLojaAtual[i] = null; 
                                jogadorTurnoAtual++;
                                if(jogadorTurnoAtual < party.size()) { gerarItensLoja(); prepararLoja(); }
                                else { addLog("Equipe abastecida! Partindo...", () -> gerarAndarDeCombate()); }
                            }
                        }
                    }
                }
            }
            if (mx > 1050 && mx < 1200 && my > 560 && my < 620) { System.exit(0); }
        }
    }
    public void mouseClicked(MouseEvent e) {} public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}
}
package tichampions;

import java.awt.*;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;

public class Renderizador {
    MotorGrafico m;
    
    public Renderizador(MotorGrafico m) { this.m = m; }

    public void renderizar(Graphics2D g) {
        switch (m.estadoAtual) {
            case MENU: desenharMenu(g); break; 
            case OPCOES: desenharOpcoes(g); break;
            case MODO_JOGO: desenharModoJogo(g); break; 
            case SELECAO_PERSONAGEM: desenharSelecaoPersonagem(g); break;
            case SELECAO_CLASSE: desenharSelecaoClasse(g); break; 
            case COMBATE: desenharCombate(g); break;
            case LOJA: desenharLoja(g); break; 
            case GAME_OVER: desenharGameOver(g); break;
        }
    }

    public void desenharBotaoHover(Graphics2D g, String t, int x, int y, int w, int h, boolean flutuar) {
        boolean hover = (!m.bloqueiaClique && m.mouseX >= x && m.mouseX <= x + w && m.mouseY >= y && m.mouseY <= y + h);
        int ofsY = (hover && flutuar) ? (int)(Math.sin(m.tempoAnimacao) * 5) : 0;
        
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

    private void desenharHUDTurno(Graphics2D g, HeroiGUI h) {
        g.setColor(new Color(0, 0, 0, 180)); g.fillRect(10, 10, 350, 115);
        g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 20)); g.drawString("Vez de: " + h.nome, 20, 40);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 18)); g.drawString(h.classe.nomeClasse, 20, 65);
        
        g.setColor(Color.RED); g.fillRect(20, 75, 200, 15);
        g.setColor(Color.GREEN); 
        int hpWidth = (int)(200 * ((double)h.status.hp / h.status.hpMax));
        if(hpWidth < 0) hpWidth = 0;
        g.fillRect(20, 75, hpWidth, 15);
        
        g.setColor(Color.BLACK); g.setStroke(new BasicStroke(2)); g.drawRect(20, 75, 200, 15);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(h.status.hp + " / " + h.status.hpMax, 95, 87);
        
        if (h.fugiuNaUltima) { g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 16)); g.drawString("DEBUFF: -50% Dano!", 20, 110); }
    }

    private void desenharMenu(Graphics2D g) {
        if (Recursos.bgMenu != null) g.drawImage(Recursos.bgMenu, 0, 0, 1280, 720, null); else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 1280, 720); }
        
        String titulo = "TI CHAMPIONS";
        g.setFont(new Font("Impact", Font.ITALIC, 100)); 
        FontMetrics fm = g.getFontMetrics();
        int titleX = (1280 - fm.stringWidth(titulo)) / 2;
        int titleY = 220 + (int)(Math.sin(m.tempoAnimacao) * 5); 
        
        g.setColor(new Color(0, 0, 0, 180)); g.drawString(titulo, titleX + 8, titleY + 8);
        g.setColor(Color.WHITE); g.drawString(titulo, titleX, titleY);

        int btnY = 380; 
        desenharBotaoHover(g, "INICIAR JOGO", 540, btnY, 200, 60, true); 
        desenharBotaoHover(g, "OPÇÕES", 540, btnY + 80, 200, 60, true);
        desenharBotaoHover(g, "SAIR", 540, btnY + 160, 200, 60, true);
    }
    
    private void desenharOpcoes(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("MENU DE OPÇÕES", 450, 100); desenharHUDGlobal(g, true);
        
        desenharBotaoHover(g, "TUTORIAL", 500, 200, 280, 60, true);
        desenharBotaoHover(g, "RESOLUÇÃO ▼", 500, 300, 280, 60, true);
        
        if (m.dropResolucao) {
            desenharBotaoHover(g, "800x600", 500, 360, 280, 40, false);
            desenharBotaoHover(g, "1280x720", 500, 400, 280, 40, false);
            desenharBotaoHover(g, "1920x1080", 500, 440, 280, 40, false);
        }
        
        if (m.mostrandoTutorial) {
            g.setColor(new Color(0,0,0,230)); g.fillRect(200, 150, 880, 450); g.setColor(Color.WHITE); g.drawRect(200, 150, 880, 450);
            g.setFont(new Font("Arial", Font.BOLD, 26)); g.drawString("TUTORIAL - MECÂNICAS DO JOGO", 400, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("• HARDWARE: Dano Físico. A defesa contra ele é a sua Manutenção.", 250, 250);
            g.drawString("• SOFTWARE: Dano Mágico. A defesa contra ele é o seu Firewall.", 250, 290);
            g.drawString("• ITENS DA LOJA: Máx de 1 Arma, 1 Armadura e 1 Ativo na Mochila.", 250, 330);
            g.drawString("• CONSUMÍVEIS: Podem ser usados diretamente no menu Mochila.", 250, 370);
            g.drawString("• FUGIR: Você pula o turno e perde -50% de Ataque. Fujões não vão na loja.", 250, 410);
            g.drawString("• PRECISÃO: Todos tem 10% de chance de errar e 10% de Crítico.", 250, 450);
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
        g.drawString("SELECIONE O PLAYER " + (m.party.size() + 1), 400, 100);
        
        Image[] imgs = {Recursos.imgMatheus, Recursos.imgLucas, Recursos.imgElvis}; 
        String[] nomes = {"Matheus", "Lucas", "Elvis"}; int[] posX = {250, 550, 850};
        for(int i=0; i<3; i++) {
            boolean hover = (!m.bloqueiaClique && m.mouseX > posX[i] && m.mouseX < posX[i]+159 && m.mouseY > 200 && m.mouseY < 500);
            int ofs = hover ? (int)(Math.sin(m.tempoAnimacao)*5) : 0;
            if (imgs[i] != null) g.drawImage(imgs[i], posX[i], 200 - ofs, 159, 300, null); else { g.setColor(Color.BLUE); g.fillRect(posX[i], 200 - ofs, 159, 300); }
            if (m.heroiSelecionadoUI == i) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(4)); g.drawRect(posX[i], 200 - ofs, 159, 300); }
            g.setFont(new Font("Arial", Font.BOLD, 24)); g.drawString(nomes[i], posX[i]+30, 530 - ofs);
        }
        
        if (m.heroiSelecionadoUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            if(m.heroiSelecionadoUI == 0) { g.drawString("HP: 80 | Hard: 5 | Soft: 25", 220, 560); g.drawString("Passiva: Alterna Buffs", 220, 590); }
            if(m.heroiSelecionadoUI == 1) { g.drawString("HP: 120 | Hard: 15 | Soft: 2", 520, 560); g.drawString("Passiva: Buff Defesa Início", 520, 590); }
            if(m.heroiSelecionadoUI == 2) { g.drawString("HP: 100 | Hard: 20 | Soft: 5", 820, 560); g.drawString("Passiva: Sobrevive a 0 HP", 820, 590); }
            desenharBotaoHover(g, "AVANÇAR", 1000, 600, 200, 60, true);
        }
    }
    
    private void desenharSelecaoClasse(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720); desenharHUDGlobal(g, true);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("ESCOLHA SUA CLASSE", 420, 100);
        
        desenharBotaoHover(g, "Infra", 200, 200, 250, 60, true); desenharBotaoHover(g, "Java Champion", 500, 200, 250, 60, true);
        desenharBotaoHover(g, "HackerMan", 800, 200, 250, 60, true); desenharBotaoHover(g, "LanHouse", 350, 300, 250, 60, true);
        desenharBotaoHover(g, "Professor", 650, 300, 250, 60, true);

        if (m.classeSelecionadaUI != -1) {
            g.setFont(new Font("Arial", Font.PLAIN, 20)); ClasseRPG cTemp = null;
            if(m.classeSelecionadaUI==0) cTemp = new Infra(); if(m.classeSelecionadaUI==1) cTemp = new JavaChampion();
            if(m.classeSelecionadaUI==2) cTemp = new HackerMan(); if(m.classeSelecionadaUI==3) cTemp = new DonoLanHouse();
            if(m.classeSelecionadaUI==4) cTemp = new Professor();
            
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
        if(m.logBatalha.isEmpty()) return;
        g.setColor(new Color(0, 0, 0, 200)); g.fillRect(800, 50, 460, 160);
        g.setColor(Color.WHITE); g.drawRect(800, 50, 460, 160); g.setFont(new Font("Arial", Font.PLAIN, 12)); 
        int yText = 75;
        for(String msg : m.logBatalha) { g.drawString("> " + msg, 810, yText); yText += 25; }
    }

    private void desenharCombate(Graphics2D g) {
        int bgIndex = ((m.andarTotal - 1) / 4) % 3; Image bgAtual = Recursos.bgsCombate[bgIndex];
        if (bgAtual != null) g.drawImage(bgAtual, 0, 0, 1280, 720, null); 
        else { g.setColor(bgIndex==0?new Color(50,0,0) : bgIndex==1?new Color(0,50,0) : new Color(0,0,50)); g.fillRect(0, 0, 1280, 720); }

        for(int i=0; i < m.inimigos.size(); i++) {
            InimigoGUI ini = m.inimigos.get(i);
            int x = 1280/(m.inimigos.size()+1) * (i+1) - 140;
            
            boolean mouseHoverIni = ((m.escolhendoAlvo || m.examinandoAlvo) && m.mouseX > x && m.mouseX < x+280 && m.mouseY > 150 && m.mouseY < 430);
            int floatY = mouseHoverIni ? (int)(Math.sin(m.tempoAnimacao) * 5) : 0;
            int animAtaqueX = (ini.timerAtacar > 0) ? (int)(Math.sin(ini.timerAtacar) * 15) : 0; 
            
            if (ini.timerPiscar == 0 || (ini.timerPiscar / 5) % 2 == 0) {
                Image spr = Recursos.imgInimigos[ini.spriteId];
                if (spr != null) g.drawImage(spr, (x + animAtaqueX), (150 - floatY), 280, 280, null); 
                else { g.setColor(Color.RED); g.fillRoundRect((x + animAtaqueX), 150 - floatY, 280, 280, 20, 20); }
            }
            
            g.setColor(Color.BLACK); g.fillRect(x + animAtaqueX, 100 - floatY, 280, 40);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 16));
            String tipoDano = (ini.tipoAtaque == 1) ? "[SOFTWARE]" : "[HARDWARE]";
            g.drawString(tipoDano + " " + ini.nome + " ("+ini.status.hp+" HP)", (x + animAtaqueX) + 10, 125 - floatY);
            if(m.escolhendoAlvo || m.examinandoAlvo) { g.setColor(Color.YELLOW); g.setStroke(new BasicStroke(3)); g.drawRect(x, 150 - floatY, 280, 280); }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200);
        desenharLogLateral(g); 
        
        if (!m.turnoInimigo && !m.party.isEmpty() && !m.bloqueiaClique) {
            HeroiGUI h = m.party.get(m.jogadorTurnoAtual);
            
            desenharHUDTurno(g, h);
            desenharMiniInventario(g, h);
            
            desenharBotaoHover(g, "EXAMINAR", 20, 660, 200, 40, true);

            if (m.inimigoExame != null) {
                g.setColor(new Color(0, 0, 0, 230)); g.fillRect(400, 150, 480, 350); 
                g.setColor(Color.WHITE); g.drawRect(400, 150, 480, 350);
                g.setFont(new Font("Arial", Font.BOLD, 26)); g.drawString("DADOS DO INIMIGO", 510, 190);
                
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Nome: " + m.inimigoExame.nome, 430, 240);
                g.drawString("HP: " + m.inimigoExame.status.hp + " / " + m.inimigoExame.status.hpMax, 430, 280);
                
                String tipoAtkNome = (m.inimigoExame.tipoAtaque == 1) ? "Software" : "Hardware";
                int valorAtk = (m.inimigoExame.tipoAtaque == 1) ? m.inimigoExame.status.software : m.inimigoExame.status.hardware;
                
                g.drawString("Ataque (" + tipoAtkNome + "): " + valorAtk, 430, 320);
                g.drawString("Defesa (Manutenção): " + m.inimigoExame.status.manutencao, 430, 360);
                g.drawString("Defesa (Firewall): " + m.inimigoExame.status.firewall, 430, 400);
                
                desenharBotaoHover(g, "FECHAR", 540, 430, 200, 50, true);
            }
            else if (m.examinandoAlvo) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("SELECIONE O ALVO PARA EXAMINAR!", 350, 630);
                desenharBotaoHover(g, "CANCELAR", 1050, 560, 180, 80, true);
            }
            else if (m.escolhendoAlvo) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("SELECIONE O ALVO PARA ATACAR!", 350, 630);
                desenharBotaoHover(g, "CANCELAR", 1050, 560, 180, 80, true);
            }
            else if (m.menuAtaqueAberto) {
                desenharBotaoHover(g, "DANO HARDWARE", 250, 560, 200, 80, true);
                desenharBotaoHover(g, "DANO SOFTWARE", 470, 560, 200, 80, true);
                desenharBotaoHover(g, "CANCELAR", 690, 560, 180, 80, true);
            }
            else if (m.menuItensAberto) {
                for(int i=0; i<Math.min(h.mochila.size(), 4); i++) desenharBotaoHover(g, h.mochila.get(i).nome, 250 + (i*160), 540, 150, 60, true);
                
                if (m.subMenuItem && m.itemFocado != null) {
                    g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16)); g.drawString("Efeito: " + m.itemFocado.descricao, 250, 630);
                    boolean isEquipado = (m.itemFocado == h.armaEquipada || m.itemFocado == h.armaduraEquipada);
                    String msgAcao = (m.itemFocado.tipo == 0) ? "USAR" : (isEquipado ? "DESEQUIPAR" : "EQUIPAR");
                    
                    desenharBotaoHover(g, msgAcao, 250, 650, 150, 40, true); desenharBotaoHover(g, "DESCARTAR", 420, 650, 150, 40, true);
                    desenharBotaoHover(g, "CANCELAR", 590, 650, 150, 40, true);
                }
                desenharBotaoHover(g, "VOLTAR", 1050, 560, 180, 80, true);
            } 
            else if (m.menuStatusAberto) {
                g.setColor(new Color(0, 0, 0, 220)); g.fillRect(300, 100, 680, 400); g.setColor(Color.WHITE); g.drawRect(300, 100, 680, 400);
                if (h.sprite != null) g.drawImage(h.sprite, 320, 150, 159, 300, null);
                g.setFont(new Font("Arial", Font.BOLD, 30)); g.drawString("STATUS DE " + h.nome.toUpperCase(), 500, 150);
                g.setFont(new Font("Arial", Font.PLAIN, 24)); 
                g.setColor(Color.YELLOW); g.drawString("Classe: " + h.classe.nomeClasse, 500, 200); g.setColor(Color.WHITE);
                g.drawString("HP: " + h.status.hp + " / " + h.status.hpMax, 500, 240);
                g.drawString("Hardware: " + h.status.hardware + (h.armaEquipada!=null&&h.armaEquipada.tipo==1?"(+"+h.armaEquipada.poder+")":""), 500, 280); 
                g.drawString("Software: " + h.status.software + (h.armaEquipada!=null&&h.armaEquipada.tipo==3?"(+"+h.armaEquipada.poder+")":""), 500, 320);
                g.drawString("Manutenção: " + h.status.manutencao + (h.armaduraEquipada!=null&&h.armaduraEquipada.tipo==2?"(+"+h.armaduraEquipada.poder+")":""), 500, 360); 
                g.drawString("Firewall: " + h.status.firewall + (h.armaduraEquipada!=null&&h.armaduraEquipada.tipo==4?"(+"+h.armaduraEquipada.poder+")":""), 500, 400);
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
        if (Recursos.bgLoja != null) g.drawImage(Recursos.bgLoja, 0, 0, 1280, 720, null); else { g.setColor(new Color(0, 50, 50)); g.fillRect(0, 0, 1280, 720); }
        Image npcImg = m.lojaLendaria ? Recursos.spriteDiegao : Recursos.spriteMarcao;
        if (npcImg != null) g.drawImage(npcImg, 100, 180, 159, 300, null);

        g.setColor(Color.WHITE); g.fillRect(450, 30, 400, 80); g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 25));
        g.drawString(m.lojaLendaria ? "LOJA LENDÁRIA DO DIEGÃO" : "LOJA DO MARCÃO", 480, 80);

        for(int i=0; i<3; i++) {
            int x = 450 + (i*200);
            desenharBotaoSprite(g, (m.itensLojaAtual[i] != null) ? m.itensLojaAtual[i].icone : null, x, 150, 150, 150, false);
            
            g.setColor(Color.BLACK); g.fillRect(x, 310, 150, 100); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14));
            if(m.itensLojaAtual[i] != null) {
                g.drawString(m.itensLojaAtual[i].nome, x+10, 335); g.setFont(new Font("Arial", Font.PLAIN, 12)); g.drawString(m.itensLojaAtual[i].descricao, x+10, 360);
                if(m.jogadorTurnoAtual < m.party.size() && !m.party.get(m.jogadorTurnoAtual).fugiuDestaBatalha && !m.comprouItemLoja) { 
                    desenharBotaoHover(g, "PEGAR", x, 420, 150, 40, true); 
                }
            }
        }

        g.setColor(Color.BLACK); g.fillRect(0, 520, 1280, 200); g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 18));
        
        if (m.jogadorTurnoAtual < m.party.size()) { 
            HeroiGUI h = m.party.get(m.jogadorTurnoAtual);
            desenharHUDTurno(g, h);
            
            if(m.party.get(m.jogadorTurnoAtual).fugiuDestaBatalha) g.drawString(h.nome + " fugiu e não pode pegar itens! Pressione Passar a Vez.", 360, 560);
            else if(m.comprouItemLoja) g.drawString(h.nome + " já pegou um item. Acesse a mochila ou passe a vez.", 360, 560);
            else g.drawString("Aperte PEGAR em UM item para o Herói: " + h.nome, 360, 560); 
            
            desenharBotaoHover(g, "MOCHILA ("+h.nome+")", 360, 600, 250, 60, true);
            String txtProximo = (m.jogadorTurnoAtual < m.party.size() - 1) ? "PRÓXIMO HERÓI" : "PRÓXIMO ANDAR";
            desenharBotaoHover(g, txtProximo, 630, 600, 200, 60, true);
            
            if(m.party.size() > 1) desenharBotaoHover(g, "SAIR DA PARTY", 850, 600, 200, 60, true);
        } 
        else { g.drawString("Todos os Heróis aptos já agiram na loja!", 400, 560); }
        
        if (m.menuItensAberto && m.jogadorTurnoAtual < m.party.size()) {
            HeroiGUI p = m.party.get(m.jogadorTurnoAtual);
            g.setColor(new Color(0,0,0,200)); g.fillRect(200, 100, 800, 300);
            for(int i=0; i<Math.min(p.mochila.size(), 4); i++) desenharBotaoHover(g, p.mochila.get(i).nome, 250 + (i*160), 120, 150, 60, true);
            if (m.subMenuItem && m.itemFocado != null) {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.PLAIN, 16)); g.drawString("Efeito: " + m.itemFocado.descricao, 250, 220);
                boolean isEquipado = (m.itemFocado == p.armaEquipada || m.itemFocado == p.armaduraEquipada);
                String msgAcao = (m.itemFocado.tipo == 0) ? "USAR" : (isEquipado ? "DESEQUIPAR" : "EQUIPAR");
                desenharBotaoHover(g, msgAcao, 250, 240, 150, 40, true); desenharBotaoHover(g, "DESCARTAR", 420, 240, 150, 40, true);
                desenharBotaoHover(g, "CANCELAR", 590, 240, 150, 40, true);
            }
            desenharBotaoHover(g, "FECHAR MOCHILA", 400, 320, 200, 60, true);
        }
    }
}
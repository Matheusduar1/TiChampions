package tichampions;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.Timer;

public class Mecanicas {
    MotorGrafico m; 
    public Mecanicas(MotorGrafico m) { this.m = m; }

    public void addLog(String msg, Runnable proximaAcao) {
        m.logBatalha.add(msg);
        if (m.logBatalha.size() > 5) m.logBatalha.remove(0); 
        
        m.bloqueiaClique = true;
        m.menuItensAberto = false; m.menuStatusAberto = false; m.subMenuItem = false; m.menuAtaqueAberto = false;
        
        if (m.timerEsperaAcao != null && m.timerEsperaAcao.isRunning()) m.timerEsperaAcao.stop();
        m.timerEsperaAcao = new Timer(1500, e -> {
            m.bloqueiaClique = false; if (proximaAcao != null) proximaAcao.run(); m.repaint();
        });
        m.timerEsperaAcao.setRepeats(false); m.timerEsperaAcao.start();
    }

    public void gerarItensLoja() {
        int power = 5 + (m.andarTotal * 2);
        ArrayList<Item> pool = new ArrayList<>();
        pool.add(new Item("Café Forte", "Cura " + (20+power*2) + " HP", 0, 20+power*2, Recursos.imgItens[0]));
        pool.add(new Item("Placa RTX", "+" + power + " Hardware", 1, power, Recursos.imgItens[1]));
        pool.add(new Item("Nobreak", "+" + power + " Manutenção", 2, power, Recursos.imgItens[2]));
        pool.add(new Item("Ferro de Solda", "+" + (power+3) + " Hardware", 1, power+3, Recursos.imgItens[3]));
        pool.add(new Item("Camisa de Evento", "+" + power + " Firewall", 4, power, Recursos.imgItens[4]));
        pool.add(new Item("Memória Velha", "+" + (power+5) + " Software", 3, power+5, Recursos.imgItens[5]));

        Collections.shuffle(pool, m.rng);
        m.itensLojaAtual[0] = pool.get(0); m.itensLojaAtual[1] = pool.get(1); m.itensLojaAtual[2] = pool.get(2);
        
        m.itemLojaComprado[0] = false; m.itemLojaComprado[1] = false; m.itemLojaComprado[2] = false;
        m.comprouItemLoja = false; 
    }

    public void gerarAndarDeCombate() {
        m.inimigos.clear(); m.batalhasSeguidas++;
        int mult = m.party.size();
        
        if (m.batalhasSeguidas >= 5) {
            m.inimigos.add(new InimigoGUI(m.andarTotal, new Status((150*mult)+(m.andarTotal*10), (20*mult)+m.andarTotal, 0, 15*mult, 15*mult), 3, 0));
            m.batalhasSeguidas = 0;
        } else {
            int qInimigos = (m.rng.nextInt(100) < 10) ? m.party.size() + m.rng.nextInt(2) + 1 : m.party.size();
            for(int i=0; i < qInimigos; i++) {
                int idSpr = m.rng.nextInt(3); int tAtaque = (idSpr == 1) ? 1 : 0; 
                int hpIni = 50 + (m.andarTotal * 15);
                int atkHard = 20 + (m.andarTotal * 4);
                int atkSoft = 20 + (m.andarTotal * 4);
                int defHard = 8 + (m.andarTotal * 2);
                int defSoft = 8 + (m.andarTotal * 2);
                m.inimigos.add(new InimigoGUI(m.andarTotal, new Status(hpIni, atkHard, atkSoft, defHard, defSoft), idSpr, tAtaque));
            }
        }
        m.logBatalha.clear(); m.turnoExtraLanHouse = false;
        for(HeroiGUI h : m.party) { 
            h.skillUsadaNoAndar = false; h.fugiuDestaBatalha = false; h.tentouFugirNoAndar = false; 
            m.logBatalha.add(h.aplicarPassivaTurno()); 
        }
        m.jogadorTurnoAtual = 0; m.turnoInimigo = false; m.estadoAtual = MotorGrafico.Estado.COMBATE; verificarTurnoValido();
    }

    public void avancarTurno() {
        m.inimigos.removeIf(i -> i.status.hp <= 0);
        m.escolhendoAlvo = false; m.menuAtaqueAberto = false;
        
        if (m.inimigos.isEmpty()) {
            m.turnoExtraLanHouse = false; 
            for(HeroiGUI h : m.party) if(!h.fugiuDestaBatalha) h.fugiuNaUltima = false; 
            m.andarTotal++; m.lojaLendaria = (m.batalhasSeguidas == 0); 
            gerarItensLoja();
            addLog("Batalha Vencida! Avançando...", () -> { iniciarLoja(); m.estadoAtual = MotorGrafico.Estado.LOJA; }); return;
        }
        
        int vivos = 0, fugiram = 0;
        for(HeroiGUI h : m.party) { if(h.status.hp > 0 && !h.fugiuDestaBatalha) vivos++; if(h.fugiuDestaBatalha) fugiram++; }
        
        if (vivos == 0) {
            m.turnoExtraLanHouse = false;
            if (fugiram > 0) addLog("Todos fugiram! Indo à Loja...", () -> { iniciarLoja(); m.estadoAtual = MotorGrafico.Estado.LOJA; });
            else {
                GerenciadorAudio.pararMusica();
                GerenciadorAudio.tocarEfeito(GerenciadorAudio.gameOver); // TOCA O GAME OVER
                addLog("GAME OVER! A equipe foi derrotada.", () -> m.estadoAtual = MotorGrafico.Estado.GAME_OVER);
            }
            return;
        }

        if (!m.turnoInimigo) {
            if (m.turnoExtraLanHouse) {
                m.turnoExtraLanHouse = false; 
                addLog(m.party.get(m.jogadorTurnoAtual).nome + " ganhou +1 Ficha! Jogue de Novo!", null);
            } else {
                m.jogadorTurnoAtual++;
                if (m.jogadorTurnoAtual >= m.party.size()) { m.turnoInimigo = true; executarTurnoInimigo(); } 
                else verificarTurnoValido();
            }
        } else { m.turnoInimigo = false; m.jogadorTurnoAtual = 0; verificarTurnoValido(); }
    }

    public void iniciarLoja() {
        m.jogadorTurnoAtual = 0; 
        while(m.jogadorTurnoAtual < m.party.size() && m.party.get(m.jogadorTurnoAtual).fugiuDestaBatalha) { m.jogadorTurnoAtual++; }
        if (m.jogadorTurnoAtual < m.party.size()) { gerarItensLoja(); } else { gerarAndarDeCombate(); }
    }

    public void avancarJogadorLoja() {
        m.jogadorTurnoAtual++;
        while(m.jogadorTurnoAtual < m.party.size() && m.party.get(m.jogadorTurnoAtual).fugiuDestaBatalha) { m.jogadorTurnoAtual++; }
        if (m.jogadorTurnoAtual < m.party.size()) { gerarItensLoja(); } else { gerarAndarDeCombate(); }
    }

    public void verificarTurnoValido() {
        if(m.turnoInimigo) return;
        HeroiGUI atual = m.party.get(m.jogadorTurnoAtual);
        if (atual.status.hp <= 0 || atual.fugiuDestaBatalha) avancarTurno();
    }

    public void executarTurnoInimigo() {
        ArrayList<HeroiGUI> alvosVivos = new ArrayList<>();
        for(HeroiGUI h : m.party) { if(h.status.hp > 0 && !h.fugiuDestaBatalha) alvosVivos.add(h); }
        if(alvosVivos.isEmpty()) { avancarTurno(); return; }

        for(InimigoGUI atacante : m.inimigos) {
            if(alvosVivos.isEmpty()) break;
            HeroiGUI alvo = alvosVivos.get(m.rng.nextInt(alvosVivos.size()));
            atacante.ativarAtaqueAnim(); 
            m.logBatalha.add(atacante.atacar(alvo));
            if(m.logBatalha.size() > 5) m.logBatalha.remove(0);
            if(alvo.status.hp <= 0) alvosVivos.remove(alvo);
        }
        
        m.bloqueiaClique = true;
        if (m.timerEsperaAcao != null && m.timerEsperaAcao.isRunning()) m.timerEsperaAcao.stop();
        m.timerEsperaAcao = new Timer(2500, e -> { m.bloqueiaClique = false; avancarTurno(); m.repaint(); });
        m.timerEsperaAcao.setRepeats(false); m.timerEsperaAcao.start();
    }
}
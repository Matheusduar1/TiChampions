package tichampions;

import java.awt.event.*;
import javax.swing.*;

public class ControladorMouse extends MouseAdapter {
    MotorGrafico m;
    
    public ControladorMouse(MotorGrafico motor) { this.m = motor; }

    @Override
    public void mouseMoved(MouseEvent e) {
        m.mouseX = (int)(e.getX() / m.scaleX); 
        m.mouseY = (int)(e.getY() / m.scaleY); 
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (m.bloqueiaClique) return; 
        int mx = (int)(e.getX() / m.scaleX); int my = (int)(e.getY() / m.scaleY);

        if (m.estadoAtual == MotorGrafico.Estado.OPCOES) {
            if (m.mostrandoTutorial) {
                if (mx > 500 && mx < 780 && my > 500 && my < 560) m.mostrandoTutorial = false; 
                return;
            }
            if (m.dropResolucao) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(m);
                if (mx > 500 && mx < 780 && my > 360 && my < 400) { frame.setSize(800, 600); frame.setLocationRelativeTo(null); m.dropResolucao = false; }
                else if (mx > 500 && mx < 780 && my > 400 && my < 440) { frame.setSize(1280, 720); frame.setLocationRelativeTo(null); m.dropResolucao = false; }
                else if (mx > 500 && mx < 780 && my > 440 && my < 480) { frame.setSize(1920, 1080); frame.setLocationRelativeTo(null); m.dropResolucao = false; }
                else m.dropResolucao = false; 
                return;
            }
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { m.estadoAtual = MotorGrafico.Estado.MENU; m.dropResolucao = false; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            
            if (mx > 500 && mx < 780 && my > 200 && my < 260) m.mostrandoTutorial = true;
            if (mx > 500 && mx < 780 && my > 300 && my < 360) m.dropResolucao = !m.dropResolucao;
            return;
        }

        if (m.estadoAtual == MotorGrafico.Estado.MENU) {
            if (mx > 540 && mx < 740 && my > 380 && my < 440) {
                GerenciadorAudio.tocarEfeito(GerenciadorAudio.start); // TOCA O START
                m.estadoAtual = MotorGrafico.Estado.MODO_JOGO;
            }
            if (mx > 540 && mx < 740 && my > 460 && my < 520) m.estadoAtual = MotorGrafico.Estado.OPCOES;
            if (mx > 540 && mx < 740 && my > 540 && my < 600) System.exit(0);
        }
        else if (m.estadoAtual == MotorGrafico.Estado.MODO_JOGO) {
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { m.estadoAtual = MotorGrafico.Estado.MENU; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            if (mx > 500 && mx < 780 && my > 300 && my < 360) { m.qtdJogadores = 1; m.estadoAtual = MotorGrafico.Estado.SELECAO_PERSONAGEM; m.party.clear(); }
            if (mx > 500 && mx < 780 && my > 400 && my < 460) { m.qtdJogadores = 2; m.estadoAtual = MotorGrafico.Estado.SELECAO_PERSONAGEM; m.party.clear(); }
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { m.qtdJogadores = 3; m.estadoAtual = MotorGrafico.Estado.SELECAO_PERSONAGEM; m.party.clear(); }
        }
        else if (m.estadoAtual == MotorGrafico.Estado.SELECAO_PERSONAGEM) {
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { m.estadoAtual = MotorGrafico.Estado.MODO_JOGO; m.party.clear(); m.heroiSelecionadoUI = -1;} 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); } 
            
            if (mx > 250 && mx < 409 && my > 200 && my < 500) m.heroiSelecionadoUI = 0;
            if (mx > 550 && mx < 709 && my > 200 && my < 500) m.heroiSelecionadoUI = 1;
            if (mx > 850 && mx < 1009 && my > 200 && my < 500) m.heroiSelecionadoUI = 2;
            
            if (m.heroiSelecionadoUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI h = (m.heroiSelecionadoUI == 0) ? new Matheus() : (m.heroiSelecionadoUI == 1) ? new Lucas() : new Elvis();
                h.sprite = (m.heroiSelecionadoUI == 0) ? Recursos.imgMatheus : (m.heroiSelecionadoUI == 1) ? Recursos.imgLucas : Recursos.imgElvis;
                m.party.add(h); m.heroiSelecionadoUI = -1; m.estadoAtual = MotorGrafico.Estado.SELECAO_CLASSE; 
            }
        }
        else if (m.estadoAtual == MotorGrafico.Estado.SELECAO_CLASSE) {
            if (mx > 20 && mx < 170 && my > 640 && my < 680) { m.party.remove(m.party.size() - 1); m.estadoAtual = MotorGrafico.Estado.SELECAO_PERSONAGEM; m.classeSelecionadaUI = -1; } 
            if (mx > 1100 && mx < 1250 && my > 20 && my < 60) { System.exit(0); }
            
            if (mx > 200 && mx < 450 && my > 200 && my < 260) m.classeSelecionadaUI = 0;
            if (mx > 500 && mx < 750 && my > 200 && my < 260) m.classeSelecionadaUI = 1;
            if (mx > 800 && mx < 1050 && my > 200 && my < 260) m.classeSelecionadaUI = 2;
            if (mx > 350 && mx < 600 && my > 300 && my < 360) m.classeSelecionadaUI = 3;
            if (mx > 650 && mx < 900 && my > 300 && my < 360) m.classeSelecionadaUI = 4;
            
            if (m.classeSelecionadaUI != -1 && mx > 1000 && mx < 1200 && my > 600 && my < 660) {
                HeroiGUI heroiAtual = m.party.get(m.party.size() - 1); 
                switch(m.classeSelecionadaUI) {
                    case 0: heroiAtual.setClasse(new Infra()); break; case 1: heroiAtual.setClasse(new JavaChampion()); break;
                    case 2: heroiAtual.setClasse(new HackerMan()); break; case 3: heroiAtual.setClasse(new DonoLanHouse()); break;
                    case 4: heroiAtual.setClasse(new Professor()); break;
                }
                m.classeSelecionadaUI = -1;
                if (m.party.size() >= m.qtdJogadores) {
                    GerenciadorAudio.pararMusica(); // PARA A MÚSICA QUANDO ENTRA NA BATALHA
                    m.mecanicas.gerarAndarDeCombate(); 
                } else {
                    m.estadoAtual = MotorGrafico.Estado.SELECAO_PERSONAGEM; 
                }
            }
        }
        else if (m.estadoAtual == MotorGrafico.Estado.GAME_OVER) {
            if (mx > 500 && mx < 780 && my > 500 && my < 560) { 
                m.party.clear(); m.andarTotal = 1; m.batalhasSeguidas = 0; 
                m.estadoAtual = MotorGrafico.Estado.MENU; 
                GerenciadorAudio.tocarMusica(GerenciadorAudio.title); // VOLTA A MÚSICA DO MENU
            }
        }
        else if (m.estadoAtual == MotorGrafico.Estado.COMBATE && !m.turnoInimigo) {
            HeroiGUI p = m.party.get(m.jogadorTurnoAtual);

            if (m.inimigoExame != null) {
                if (mx > 540 && mx < 740 && my > 430 && my < 480) { m.inimigoExame = null; m.repaint(); }
                return;
            }

            if (m.menuStatusAberto) { if (mx > 540 && mx < 780 && my > 560 && my < 640) m.menuStatusAberto = false; return; }
            if (m.menuItensAberto) {
                for(int i=0; i<Math.min(p.mochila.size(), 4); i++) {
                    if (mx > 250+(i*160) && mx < 400+(i*160) && my > 540 && my < 600) { m.itemFocado = p.mochila.get(i); m.subMenuItem = true; }
                }
                if (m.subMenuItem && m.itemFocado != null) {
                    boolean isEquipado = (m.itemFocado == p.armaEquipada || m.itemFocado == p.armaduraEquipada);
                    if (mx > 250 && mx < 370 && my > 650 && my < 690) { 
                        if (m.itemFocado.tipo == 0) { p.status.hp = Math.min(p.status.hpMax, p.status.hp + m.itemFocado.poder); p.mochila.remove(m.itemFocado); m.mecanicas.addLog(p.nome + " bebeu " + m.itemFocado.nome +"!", () -> m.mecanicas.avancarTurno()); }
                        else if (isEquipado) { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null; m.subMenuItem=false; m.repaint(); }
                        else { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = m.itemFocado; else p.armaduraEquipada = m.itemFocado; m.subMenuItem=false; m.repaint(); }
                    }
                    if (mx > 420 && mx < 570 && my > 650 && my < 690) { 
                        if(isEquipado) { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null; }
                        p.mochila.remove(m.itemFocado); m.itemFocado = null; m.subMenuItem = false; m.repaint(); 
                    }
                    if (mx > 590 && mx < 740 && my > 650 && my < 690) { m.itemFocado = null; m.subMenuItem = false; m.repaint(); }
                }
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { m.menuItensAberto = false; m.itemFocado = null; m.subMenuItem = false; }
                return;
            }
            
            if (m.menuAtaqueAberto) {
                if (mx > 250 && mx < 450 && my > 560 && my < 640) { m.tipoAtaqueEscolhido = 0; m.menuAtaqueAberto = false; m.escolhendoAlvo = true; }
                if (mx > 470 && mx < 670 && my > 560 && my < 640) { m.tipoAtaqueEscolhido = 1; m.menuAtaqueAberto = false; m.escolhendoAlvo = true; }
                if (mx > 690 && mx < 870 && my > 560 && my < 640) { m.menuAtaqueAberto = false; }
                return;
            }

            if (m.examinandoAlvo) {
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) { m.examinandoAlvo = false; m.repaint(); } 
                for(int i=0; i < m.inimigos.size(); i++) {
                    int x = 1280/(m.inimigos.size()+1) * (i+1) - 140;
                    if (mx > x && mx < x+280 && my > 150 && my < 430) { 
                        m.inimigoExame = m.inimigos.get(i); 
                        m.examinandoAlvo = false; 
                        m.repaint(); 
                    }
                }
                return;
            }

            if (m.escolhendoAlvo) {
                if (mx > 1050 && mx < 1230 && my > 560 && my < 640) m.escolhendoAlvo = false; 
                for(int i=0; i < m.inimigos.size(); i++) {
                    int x = 1280/(m.inimigos.size()+1) * (i+1) - 140;
                    if (mx > x && mx < x+280 && my > 150 && my < 430) { 
                        m.mecanicas.addLog(p.atacarBasico(m.inimigos.get(i), m.tipoAtaqueEscolhido), () -> m.mecanicas.avancarTurno()); 
                    }
                }
                return;
            }

            if (!m.escolhendoAlvo && !m.menuAtaqueAberto && !m.menuItensAberto && !m.menuStatusAberto && !m.examinandoAlvo) {
                 if (mx > 20 && mx < 220 && my > 660 && my < 700) { m.examinandoAlvo = true; m.repaint(); return; }
            }

            if (mx > 250 && mx < 430 && my > 560 && my < 640) m.menuAtaqueAberto = true; 
            if (mx > 450 && mx < 630 && my > 560 && my < 640) { if(p.mochila.size()>0) m.menuItensAberto = true; else m.mecanicas.addLog("Mochila Vazia!", null); }
            if (!p.skillUsadaNoAndar && mx > 650 && mx < 830 && my > 560 && my < 640) { 
                p.skillUsadaNoAndar = true; 
                if (p.classe instanceof DonoLanHouse) m.turnoExtraLanHouse = true; 
                m.mecanicas.addLog(p.classe.usarSkill(p, m.inimigos), () -> m.mecanicas.avancarTurno()); 
            }
            if (mx > 850 && mx < 1030 && my > 560 && my < 640) m.menuStatusAberto = true; 
            
            if (!p.tentouFugirNoAndar && mx > 1050 && mx < 1230 && my > 560 && my < 640) { 
                p.fugiuDestaBatalha = true; p.tentouFugirNoAndar = true; p.fugiuNaUltima = true; 
                m.mecanicas.addLog(p.nome + " fugiu! Pula a vez e -50% ATK depois!", () -> m.mecanicas.avancarTurno());
            }
        }
        else if (m.estadoAtual == MotorGrafico.Estado.LOJA) {
            if (m.menuItensAberto && m.jogadorTurnoAtual < m.party.size()) {
                HeroiGUI p = m.party.get(m.jogadorTurnoAtual);
                for(int i=0; i<Math.min(p.mochila.size(), 4); i++) {
                    if (mx > 250+(i*160) && mx < 400+(i*160) && my > 120 && my < 180) { m.itemFocado = p.mochila.get(i); m.subMenuItem = true; }
                }
                if (m.subMenuItem && m.itemFocado != null) {
                    boolean isEquipado = (m.itemFocado == p.armaEquipada || m.itemFocado == p.armaduraEquipada);
                    if (mx > 250 && mx < 400 && my > 240 && my < 280) { 
                        if (m.itemFocado.tipo == 0) { p.status.hp = Math.min(p.status.hpMax, p.status.hp + m.itemFocado.poder); p.mochila.remove(m.itemFocado); }
                        else if (isEquipado) { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null;}
                        else { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = m.itemFocado; else p.armaduraEquipada = m.itemFocado;}
                        m.subMenuItem=false; m.repaint();
                    }
                    if (mx > 420 && mx < 570 && my > 240 && my < 280) { 
                        if(isEquipado) { if(m.itemFocado.tipo == 1 || m.itemFocado.tipo == 3) p.armaEquipada = null; else p.armaduraEquipada = null; }
                        p.mochila.remove(m.itemFocado); m.itemFocado = null; m.subMenuItem = false; m.repaint(); 
                    }
                    if (mx > 590 && mx < 740 && my > 240 && my < 280) { m.itemFocado = null; m.subMenuItem = false; m.repaint(); } 
                }
                if (mx > 400 && mx < 600 && my > 320 && my < 380) { m.menuItensAberto = false; m.itemFocado = null; m.subMenuItem = false; }
                return;
            }

            if (m.jogadorTurnoAtual < m.party.size()) {
                HeroiGUI pAtual = m.party.get(m.jogadorTurnoAtual);
                
                if (mx > 360 && mx < 610 && my > 600 && my < 660) { m.menuItensAberto = true; }
                
                if (mx > 630 && mx < 830 && my > 600 && my < 660) { 
                    m.mecanicas.avancarJogadorLoja();
                }
                
                if (m.party.size() > 1 && mx > 850 && mx < 1050 && my > 600 && my < 660) {
                    m.mecanicas.addLog(pAtual.nome + " abandonou a equipe!", null);
                    m.party.remove(m.jogadorTurnoAtual);
                    m.qtdJogadores = m.party.size(); 
                    
                    while(m.jogadorTurnoAtual < m.party.size() && m.party.get(m.jogadorTurnoAtual).fugiuDestaBatalha) {
                        m.jogadorTurnoAtual++;
                    }
            
                    if(m.jogadorTurnoAtual < m.party.size()) {
                        m.comprouItemLoja = false;
                        m.mecanicas.gerarItensLoja();
                    } else {
                        m.mecanicas.gerarAndarDeCombate();
                    }
                    return; 
                }

                if(!pAtual.fugiuDestaBatalha && !m.comprouItemLoja) {
                    for(int i=0; i<3; i++) {
                        int x = 450 + (i*200);
                        if (!m.itemLojaComprado[i] && m.itensLojaAtual[i] != null && mx > x && mx < x+150 && my > 420 && my < 460) {
                            Item itemDesejado = m.itensLojaAtual[i];
                            int qtdUsaveis = 0;
                            Item equipAntigo = null;
                            
                            for(Item it : pAtual.mochila) { 
                                if(it.tipo == 0) qtdUsaveis++; 
                                else if ((itemDesejado.tipo == 1 || itemDesejado.tipo == 3) && (it.tipo == 1 || it.tipo == 3)) equipAntigo = it;
                                else if ((itemDesejado.tipo == 2 || itemDesejado.tipo == 4) && (it.tipo == 2 || it.tipo == 4)) equipAntigo = it;
                            }
                            
                            if (itemDesejado.tipo == 0 && qtdUsaveis >= 1) { 
                                m.mecanicas.addLog("Limite de Consumíveis (1)! Descarte na Mochila.", null); 
                            } 
                            else if (equipAntigo != null) { 
                                if (itemDesejado.poder > equipAntigo.poder) {
                                    if (pAtual.armaEquipada == equipAntigo) pAtual.armaEquipada = itemDesejado;
                                    if (pAtual.armaduraEquipada == equipAntigo) pAtual.armaduraEquipada = itemDesejado;
                                    pAtual.mochila.remove(equipAntigo);
                                    pAtual.mochila.add(itemDesejado);
                                    m.mecanicas.addLog("Substituiu " + equipAntigo.nome + " por " + itemDesejado.nome + "!", null);
                                    m.comprouItemLoja = true;
                                    m.itemLojaComprado[i] = true; 
                                } else {
                                    m.mecanicas.addLog("Você já possui um equipamento igual ou melhor!", null);
                                }
                            } 
                            else { 
                                pAtual.mochila.add(itemDesejado); 
                                m.comprouItemLoja = true;
                                m.itemLojaComprado[i] = true; 
                            }
                        }
                    }
                }
            }
        }
    }
}
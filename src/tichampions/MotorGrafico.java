package tichampions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MotorGrafico extends JPanel implements ActionListener {

    public enum Estado { MENU, OPCOES, MODO_JOGO, SELECAO_PERSONAGEM, SELECAO_CLASSE, COMBATE, LOJA, GAME_OVER }
    public Estado estadoAtual = Estado.MENU;

    Mecanicas mecanicas; Renderizador renderizador; ControladorMouse controladorMouse;

    Timer timerGameLoop; 
    double tempoAnimacao = 0;
    double scaleX = 1.0, scaleY = 1.0; 
    int mouseX = 0, mouseY = 0;

    ArrayList<HeroiGUI> party = new ArrayList<>();
    ArrayList<InimigoGUI> inimigos = new ArrayList<>();
    int qtdJogadores = 1, jogadorTurnoAtual = 0; boolean turnoInimigo = false;
    
    int heroiSelecionadoUI = -1, classeSelecionadaUI = -1;
    boolean escolhendoAlvo = false, menuItensAberto = false, menuStatusAberto = false, subMenuItem = false;
    boolean menuAtaqueAberto = false; int tipoAtaqueEscolhido = 0; 
    boolean examinandoAlvo = false; InimigoGUI inimigoExame = null;
    
    boolean mostrandoTutorial = false, dropResolucao = false, turnoExtraLanHouse = false;
    boolean comprouItemLoja = false; Item itemFocado = null; 
    
    ArrayList<String> logBatalha = new ArrayList<>();
    Timer timerEsperaAcao; boolean bloqueiaClique = false;

    int andarTotal = 1, batalhasSeguidas = 0; boolean lojaLendaria = false;
    Random rng = new Random();

    Item[] itensLojaAtual = new Item[3];
    boolean[] itemLojaComprado = {false, false, false}; 

    public MotorGrafico() {
        this.setPreferredSize(new Dimension(1280, 720));
        mecanicas = new Mecanicas(this);
        renderizador = new Renderizador(this);
        controladorMouse = new ControladorMouse(this);
        this.addMouseListener(controladorMouse);
        this.addMouseMotionListener(controladorMouse);
        
        Recursos.carregar();
        GerenciadorAudio.carregar(); // CARREGA OS SONS
        GerenciadorAudio.tocarMusica(GerenciadorAudio.title); // INICIA A MÚSICA DO MENU
        
        timerGameLoop = new Timer(16, this); timerGameLoop.start();
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
        renderizador.renderizar(g2d);
    }
}
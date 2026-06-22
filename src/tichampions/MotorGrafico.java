package tichampions;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class MotorGrafico extends JPanel implements ActionListener, MouseListener {

    // Estados do Jogo
    enum Estado { MENU, SELECAO, COMBATE, LOJA }
    Estado estadoAtual = Estado.MENU;

    // Timer para rodar o jogo a ~60 FPS
    Timer timer;
    
    // Variável para o efeito de "flutuar" dos botões
    double tempoAnimacao = 0;
    int mouseX = 0, mouseY = 0;

    // Dados do Jogo
    ArrayList<HeroiGUI> party = new ArrayList<>();
    InimigoGUI inimigoAtual;
    int andarAtual = 1;

    // Imagens Carregadas
    Image bgMenu, bgCombate, bgLoja, spriteMarcao;

    public MotorGrafico() {
        this.setPreferredSize(new Dimension(1280, 720));
        this.addMouseListener(this);
        
        // Pega a posição do mouse em tempo real para os botões
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        carregarRecursos();

        // Inicia o Game Loop (16ms = ~60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    // Sistema Safe-Load de Imagens (Não quebra se a pasta estiver vazia)
    private void carregarRecursos() {
        try {
            bgMenu = ImageIO.read(new File("sprites/backgrounds/bg_menu.png"));
            bgCombate = ImageIO.read(new File("sprites/backgrounds/bg_combate.png"));
            bgLoja = ImageIO.read(new File("sprites/backgrounds/bg_loja.png"));
            spriteMarcao = ImageIO.read(new File("sprites/npc/marcao.png"));
        } catch (Exception e) {
            System.out.println("Aviso: Imagens não encontradas nas pastas. Usando cores sólidas.");
        }
    }

    // GAME LOOP: Atualiza lógica
    @Override
    public void actionPerformed(ActionEvent e) {
        tempoAnimacao += 0.05; // Incrementa para o efeito Math.sin()
        repaint(); // Manda desenhar a tela novamente
    }

    // RENDERIZAÇÃO GRÁFICA
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (estadoAtual) {
            case MENU:
                desenharMenu(g2d);
                break;
            case SELECAO:
                desenharSelecao(g2d);
                break;
            case COMBATE:
                desenharCombate(g2d);
                break;
            case LOJA:
                desenharLoja(g2d);
                break;
        }
    }

    // --- MÉTODOS DE DESENHO (DRAWING) ---

    private void desenharMenu(Graphics2D g) {
        if (bgMenu != null) g.drawImage(bgMenu, 0, 0, 1280, 720, null);
        else { g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, 1280, 720); }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("TI CHAMPIONS", 330, 200);

        // Botão Iniciar (Com efeito flutuante)
        int btnY = 350 + (int)(Math.sin(tempoAnimacao) * 10);
        desenharBotao(g, "INICIAR JOGO", 540, btnY, 200, 60);
        desenharBotao(g, "OPÇÕES", 540, btnY + 80, 200, 60);
        desenharBotao(g, "SAIR", 540, btnY + 160, 200, 60);
    }

    private void desenharSelecao(Graphics2D g) {
        g.setColor(Color.BLACK); g.fillRect(0, 0, 1280, 720);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SELECIONE SEUS HERÓIS", 400, 100);
        
        desenharBotao(g, "Matheus", 300, 300, 200, 200);
        desenharBotao(g, "Lucas", 540, 300, 200, 200);
        desenharBotao(g, "Elvis", 780, 300, 200, 200);

        desenharBotao(g, "Confirmar Equipe", 540, 600, 200, 60);
    }

    private void desenharCombate(Graphics2D g) {
        if (bgCombate != null) g.drawImage(bgCombate, 0, 0, 1280, 720, null);
        else { g.setColor(new Color(50, 0, 0)); g.fillRect(0, 0, 1280, 720); }

        // Desenhar Inimigo (Baseado na sua imagem do Paint - no Centro)
        g.setColor(new Color(150, 0, 0));
        g.fillRoundRect(500, 150, 280, 280, 20, 20); // Placeholder do inimigo
        g.setColor(Color.WHITE);
        g.drawString("Inimigo (Andar " + andarAtual + ")", 560, 140);

        // Painel Inferior
        g.setColor(Color.BLACK);
        g.fillRect(0, 520, 1280, 200);
        g.setColor(Color.WHITE);
        g.drawRect(2, 522, 1275, 195);

        // Botões de Ação
        desenharBotao(g, "ATACAR", 50, 560, 180, 80);
        desenharBotao(g, "ITEM", 250, 560, 180, 80);
        desenharBotao(g, "SKILL", 450, 560, 180, 80);
        desenharBotao(g, "STATUS", 650, 560, 180, 80);
        desenharBotao(g, "FUGIR", 1000, 560, 180, 80); // Afastado na direita
    }

    private void desenharLoja(Graphics2D g) {
        if (bgLoja != null) g.drawImage(bgLoja, 0, 0, 1280, 720, null);
        else { g.setColor(new Color(0, 50, 50)); g.fillRect(0, 0, 1280, 720); }

        // NPC Mercador
        if (spriteMarcao != null) g.drawImage(spriteMarcao, 100, 150, 200, 400, null);
        else { g.setColor(Color.BLUE); g.fillRect(100, 150, 200, 400); }

        // Placa da Loja
        g.setColor(Color.WHITE);
        g.fillRect(450, 30, 400, 80);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("LOJA DO MARCÃO", 520, 80);

        // 3 Cartas de Itens no centro
        desenharBotao(g, "Item 1", 450, 200, 150, 250);
        desenharBotao(g, "Item 2", 650, 200, 150, 250);
        desenharBotao(g, "Item 3", 850, 200, 150, 250);

        // Barra inferior (Inventário + Sair)
        g.setColor(Color.BLACK);
        g.fillRect(0, 520, 1280, 200);
        desenharBotao(g, "Ataque Eq.", 50, 550, 80, 80);
        desenharBotao(g, "Defesa Eq.", 150, 550, 80, 80);
        desenharBotao(g, "Ativo Eq.", 250, 550, 80, 80);
        
        desenharBotao(g, "Próximo Andar", 800, 560, 200, 60);
        desenharBotao(g, "Sair do Jogo", 1050, 560, 150, 60);
    }

    // --- UTILIDADE PARA DESENHAR BOTÕES INTERATIVOS ---
    private void desenharBotao(Graphics2D g, String texto, int x, int y, int w, int h) {
        // Checa se o mouse está em cima (Hover)
        boolean hover = (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h);

        g.setColor(hover ? new Color(70, 70, 70) : new Color(30, 30, 30));
        g.fillRect(x, y, w, h);
        
        // Borda Branca se estiver em Hover
        g.setColor(hover ? Color.WHITE : Color.GRAY);
        g.setStroke(new BasicStroke(hover ? 4 : 2));
        g.drawRect(x, y, w, h);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int strX = x + (w - fm.stringWidth(texto)) / 2;
        int strY = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(texto, strX, strY);
    }

    // --- LÓGICA DE CLIQUES DO MOUSE (CONTROLE DE ESTADOS) ---
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (estadoAtual == Estado.MENU) {
            // Lógica rudimentar de colisão de botão Iniciar
            if (mx > 540 && mx < 740 && my > 350 && my < 410) {
                estadoAtual = Estado.SELECAO;
            }
        } 
        else if (estadoAtual == Estado.SELECAO) {
            // Clicou no Confirmar Equipe
            if (mx > 540 && mx < 740 && my > 600 && my < 660) {
                // TODO: Lógica de adicionar quem foi selecionado
                party.add(new Matheus()); 
                estadoAtual = Estado.COMBATE;
            }
        }
        else if (estadoAtual == Estado.COMBATE) {
            // Clicou no ATACAR
            if (mx > 50 && mx < 230 && my > 560 && my < 640) {
                System.out.println("Herói Atacou!");
                // Após atacar, se matar o monstro, vai pra loja
                estadoAtual = Estado.LOJA;
            }
            // Clicou em FUGIR
            if (mx > 1000 && mx < 1180 && my > 560 && my < 640) {
                estadoAtual = Estado.LOJA; // Foge pra loja com debuff
            }
        }
        else if (estadoAtual == Estado.LOJA) {
            // Clicou no PRÓXIMO ANDAR
            if (mx > 800 && mx < 1000 && my > 560 && my < 620) {
                andarAtual++;
                estadoAtual = Estado.COMBATE;
            }
        }
    }

    // Métodos obrigatórios da interface MouseListener que não usamos
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
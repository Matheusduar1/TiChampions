package tichampions;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TiChampionsMain {
    public static void main(String[] args) {
        // Boas práticas do Java Swing: Rodar na thread de interface
        SwingUtilities.invokeLater(() -> {
            // Atualizamos o título da janela do jogo
            JFrame janela = new JFrame("TI Champions V5.0 - GUI Edition");
            
            MotorGrafico motor = new MotorGrafico();
            
            janela.add(motor);
            janela.pack(); // Ajusta a janela pro tamanho do painel (1280x720)
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setLocationRelativeTo(null); // Centraliza no monitor
            janela.setResizable(false); // Por enquanto bloqueado, opções de resolução virão no SubMenu
            janela.setVisible(true);
        });
    }
}
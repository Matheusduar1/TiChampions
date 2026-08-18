package tichampions;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TiChampionsMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("TI Champions - Versao Final(Agora vai)");
            MotorGrafico motor = new MotorGrafico();
            janela.add(motor);
            janela.pack(); 
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setLocationRelativeTo(null); 
            janela.setResizable(true); 
            janela.setVisible(true);
        });
    }
}
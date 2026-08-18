package tichampions;

import javax.sound.sampled.*;
import java.io.File;

public class GerenciadorAudio {
    public static Clip title, hit, hurt, special, gameOver, start;
    private static Clip musicaAtual;

    public static void carregar() {
        title = carregarClip("src/audios/Title.wav");
        hit = carregarClip("src/audios/Hit.wav");
        hurt = carregarClip("src/audios/Hurt.wav");
        special = carregarClip("src/audios/Special.wav");
        gameOver = carregarClip("src/audios/GameOver.wav");
        start = carregarClip("src/audios/Start.wav");
    }

    private static Clip carregarClip(String caminho) {
        try {
            File arquivo = new File(caminho);
            if (arquivo.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(arquivo);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            } else {
                System.out.println("Áudio não encontrado: " + caminho);
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar áudio (Verifique se é .wav e está na pasta certa): " + caminho);
        }
        return null;
    }

    public static void tocarEfeito(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0); // Reinicia o som se já tocou antes
            clip.start();
        }
    }

    public static void tocarMusica(Clip clip) {
        if (musicaAtual == clip && clip != null && clip.isRunning()) return; 
        if (musicaAtual != null && musicaAtual.isRunning()) musicaAtual.stop();
        
        musicaAtual = clip;
        if (musicaAtual != null) {
            musicaAtual.setFramePosition(0);
            musicaAtual.loop(Clip.LOOP_CONTINUOUSLY); 
        }
    }

    public static void pararMusica() {
        if (musicaAtual != null && musicaAtual.isRunning()) {
            musicaAtual.stop();
        }
    }
}
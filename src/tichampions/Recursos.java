package tichampions;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Recursos {
    public static Image bgMenu, bgLoja, spriteMarcao, spriteDiegao;
    public static Image[] bgsCombate = new Image[3]; 
    public static Image imgMatheus, imgLucas, imgElvis;
    public static Image[] imgInimigos = new Image[5]; 
    public static Image[] imgItens = new Image[6]; 

    public static void carregar() {
        try {
            bgMenu = ImageIO.read(new File("src/sprites/backgrounds/bg_menu.png")); 
            bgLoja = ImageIO.read(new File("src/sprites/backgrounds/bg_loja.png")); 
            bgsCombate[0] = ImageIO.read(new File("src/sprites/backgrounds/bg_combate1.png"));
            bgsCombate[1] = ImageIO.read(new File("src/sprites/backgrounds/bg_combate2.png"));
            bgsCombate[2] = ImageIO.read(new File("src/sprites/backgrounds/bg_combate3.png"));
            
            spriteMarcao = ImageIO.read(new File("src/sprites/npc/marcao.png")); 
            spriteDiegao = ImageIO.read(new File("src/sprites/npc/diegao.png")); 
            imgMatheus = ImageIO.read(new File("src/sprites/personagens/matheus.png")); 
            imgLucas = ImageIO.read(new File("src/sprites/personagens/lucas.png")); 
            imgElvis = ImageIO.read(new File("src/sprites/personagens/elvis.png"));
            
            imgInimigos[0] = ImageIO.read(new File("src/sprites/inimigos/estagiario.png")); 
            imgInimigos[1] = ImageIO.read(new File("src/sprites/inimigos/sql_injection.png"));
            imgInimigos[2] = ImageIO.read(new File("src/sprites/inimigos/hardware_curto.png")); 
            imgInimigos[3] = ImageIO.read(new File("src/sprites/inimigos/boss_arquiteto.png"));
            
            imgItens[0] = ImageIO.read(new File("src/sprites/itens/cafe.png")); 
            imgItens[1] = ImageIO.read(new File("src/sprites/itens/placa_video.png"));
            imgItens[2] = ImageIO.read(new File("src/sprites/itens/nobreak.png"));
            imgItens[3] = ImageIO.read(new File("src/sprites/itens/ferro_solda.png"));
            imgItens[4] = ImageIO.read(new File("src/sprites/itens/camisa_evento.png"));
            imgItens[5] = ImageIO.read(new File("src/sprites/itens/memoria_enferrujada.png"));
        } catch (Exception e) {
            System.out.println("Erro ao carregar os recursos visuais. Verifique se a pasta sprites está dentro de src!");
        }
    }
}
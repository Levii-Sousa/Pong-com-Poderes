import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Pong extends JPanel implements ActionListener, KeyListener {

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // Raquetes
    private final int RAQUETE_LARGURA = 15;
    private int jogador1Y = HEIGHT / 2 - 50;
    private int jogador2Y = HEIGHT / 2 - 50;
    private int jogador1RaqueteAltura = 100;
    private int jogador2RaqueteAltura = 100;

    // Bola
    private int bolaTamanho = 20;
    private final int VELOCIDADE_INICIAL = 3;
    private final int MAX_VELOCIDADE = 12;
    ArrayList<Bola> bolas = new ArrayList<>();

    class Bola {
        int x, y, velX, velY;
        public Bola(int x, int y, int velX, int velY) {
            this.x = x; this.y = y; this.velX = velX; this.velY = velY;
        }
    }

    // PowerUps
    enum TipoPowerUp { BOLA_GIGANTE, RAQUETE_GIGANTE, BOLA_RAPIDA }

    class PowerUp {
        int x, y, tamanho = 50;
        boolean ativo = true;
        TipoPowerUp tipo;
        int rebatidasDesdeSpawn = 0; 
        int rodadasDesdeSpawn = 0;

        public PowerUp(int x, int y, TipoPowerUp tipo) { this.x = x; this.y = y; this.tipo = tipo; }

        public void desenhar(Graphics g) {
            if (!ativo) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            switch(tipo){
                case BOLA_GIGANTE -> g2.setColor(Color.RED);
                case RAQUETE_GIGANTE -> g2.setColor(Color.BLUE);
                case BOLA_RAPIDA -> g2.setColor(Color.GREEN);
            }
            g2.fillRoundRect(x,y,tamanho,tamanho,15,15);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x,y,tamanho,tamanho,15,15); // contorno branco
        }

        public boolean colidiu(int bx,int by,int bTamanho){
            return ativo &&
                   bx < x + tamanho &&
                   bx + bTamanho > x &&
                   by < y + tamanho &&
                   by + bTamanho > y;
        }

        public void reposicionar() {
            int[][] posicoes = {
                {60, 60}, {WIDTH-110, 60}, {60, HEIGHT-110}, {WIDTH-110, HEIGHT-110}, {WIDTH/2-25, HEIGHT/2-25}
            };
            Random rand = new Random();
            int escolha = rand.nextInt(posicoes.length);
            this.x = posicoes[escolha][0];
            this.y = posicoes[escolha][1];
            this.rebatidasDesdeSpawn = 0;
        }
    }

    private PowerUp powerUp = null;
    Random rand = new Random();

    private int scoreLeft = 0;
    private int scoreRight = 0;
    private final int PONTOS_MAX = 10;

    private boolean gameOver = false;
    private boolean modo2Jogadores;

    Timer timer;
    float gradienteOffset = 0f;

    public Pong(boolean modo2Jogadores) {
        this.modo2Jogadores = modo2Jogadores;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if(gameOver){
                    scoreLeft=0; scoreRight=0; gameOver=false;
                    resetarBolas();
                }
            }
        });

        bolas.add(new Bola(WIDTH/2, HEIGHT/2, VELOCIDADE_INICIAL, VELOCIDADE_INICIAL));
        timer = new Timer(10,this);
        timer.start();
    }

    private void spawnPowerUp() {
        if(powerUp == null && rand.nextInt(1000)<5){
            int[][] posicoes = {
                {60, 60}, {WIDTH-110, 60}, {60, HEIGHT-110}, {WIDTH-110, HEIGHT-110}, {WIDTH/2-25, HEIGHT/2-25}
            };
            int escolha = rand.nextInt(posicoes.length);
            int x = posicoes[escolha][0];
            int y = posicoes[escolha][1];
            TipoPowerUp tipo = TipoPowerUp.values()[rand.nextInt(TipoPowerUp.values().length)];
            powerUp = new PowerUp(x, y, tipo);
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Fundo animado com gradiente
        gradienteOffset += 0.005f;
        GradientPaint gp = new GradientPaint(0,0,Color.DARK_GRAY,0,HEIGHT,new Color(20,20,(int)(100+155*Math.abs(Math.sin(gradienteOffset)))),true);
        g2.setPaint(gp);
        g2.fillRect(0,0,WIDTH,HEIGHT);

        if(gameOver){
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial",Font.BOLD,36));
            String winner = (scoreLeft==PONTOS_MAX)?"Jogador Esquerdo venceu!":"Jogador Direito venceu!";
            g.drawString(winner, WIDTH/2-180, HEIGHT/2-50);
            g.setFont(new Font("Arial",Font.PLAIN,20));
            g.drawString("Clique para reiniciar", WIDTH/2-100, HEIGHT/2);
            return;
        }

        // Raquetes com glow
        g2.setColor(Color.CYAN);
        g2.fillRoundRect(30,jogador1Y,RAQUETE_LARGURA,jogador1RaqueteAltura,10,10);
        g2.setColor(new Color(0,255,255,100));
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(30,jogador1Y,RAQUETE_LARGURA,jogador1RaqueteAltura,10,10);

        g2.setColor(Color.MAGENTA);
        g2.fillRoundRect(WIDTH-50,jogador2Y,RAQUETE_LARGURA,jogador2RaqueteAltura,10,10);
        g2.setColor(new Color(255,0,255,100));
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(WIDTH-50,jogador2Y,RAQUETE_LARGURA,jogador2RaqueteAltura,10,10);

        // Bola com brilho
        g2.setColor(Color.ORANGE);
        for(Bola b:bolas) {
            g2.fillOval(b.x,b.y,bolaTamanho,bolaTamanho);
            g2.setColor(new Color(255,200,0,100));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(b.x,b.y,bolaTamanho,bolaTamanho);
            g2.setColor(Color.ORANGE);
        }

        // PowerUp e texto maior
        if(powerUp!=null){
            powerUp.desenhar(g2);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,20));
            String nomePoder = switch(powerUp.tipo){
                case BOLA_GIGANTE -> "Bola Gigante";
                case RAQUETE_GIGANTE -> "Raquete Gigante";
                case BOLA_RAPIDA -> "Bola Rápida";
            };
            g2.drawString(nomePoder, powerUp.x-10, powerUp.y - 10);
        }

        // Placar central com sombra
        g2.setFont(new Font("Arial",Font.BOLD,50));
        g2.setColor(Color.BLACK);
        g2.drawString(scoreLeft + " : " + scoreRight, WIDTH/2-52,52);
        g2.setColor(Color.YELLOW);
        g2.drawString(scoreLeft + " : " + scoreRight, WIDTH/2-50,50);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if(gameOver) return;
        spawnPowerUp();

        ArrayList<Bola> bolasParaRemover = new ArrayList<>();

        for(Bola b:bolas){
            b.x+=b.velX; b.y+=b.velY;

            if(b.y<=0 || b.y>=HEIGHT-bolaTamanho) b.velY*=-1;

            boolean bateu = false;
            if(b.x<=30+RAQUETE_LARGURA && b.y+bolaTamanho>=jogador1Y && b.y<=jogador1Y+jogador1RaqueteAltura){
                b.velX*=-1; aumentarVelocidade(b); bateu = true; ativarPowerUpSeColidiu(b,true);
            }
            if(b.x+bolaTamanho>=WIDTH-50 && b.y+bolaTamanho>=jogador2Y && b.y<=jogador2Y+jogador2RaqueteAltura){
                b.velX*=-1; aumentarVelocidade(b); bateu = true; ativarPowerUpSeColidiu(b,false);
            }

            if(bateu && powerUp != null){
                powerUp.rebatidasDesdeSpawn++;
                if(powerUp.rebatidasDesdeSpawn >= 5){
                    powerUp.reposicionar();
                }
            }

            if(b.x<=0){ 
                scoreRight++; bolasParaRemover.add(b); 
                if(powerUp != null) atualizarPowerUpRodada();
            }
            else if(b.x>=WIDTH-bolaTamanho){ 
                scoreLeft++; bolasParaRemover.add(b); 
                if(powerUp != null) atualizarPowerUpRodada();
            }
        }

        bolas.removeAll(bolasParaRemover);
        if(bolas.isEmpty()) bolas.add(new Bola(WIDTH/2, HEIGHT/2, VELOCIDADE_INICIAL, VELOCIDADE_INICIAL));
        if(scoreLeft>=PONTOS_MAX || scoreRight>=PONTOS_MAX) gameOver=true;

        // Bot
        if(!modo2Jogadores){
            if(!bolas.isEmpty()){
                Bola b = bolas.get(0);
                if(b.y > jogador2Y+jogador2RaqueteAltura/2) jogador2Y+=3;
                else if(b.y < jogador2Y+jogador2RaqueteAltura/2) jogador2Y-=3;
            }
        }

        repaint();
    }

    private void atualizarPowerUpRodada(){
        powerUp.rodadasDesdeSpawn++;
        if(powerUp.rodadasDesdeSpawn >= 2){
            TipoPowerUp[] tipos = TipoPowerUp.values();
            powerUp.tipo = tipos[rand.nextInt(tipos.length)];
            powerUp.reposicionar();
            powerUp.rodadasDesdeSpawn = 0;
        }
    }

    private void aumentarVelocidade(Bola b){
        if(b.velX>0 && b.velX<MAX_VELOCIDADE) b.velX++;
        else if(b.velX<0 && b.velX>-MAX_VELOCIDADE) b.velX--;
        if(b.velY>0 && b.velY<MAX_VELOCIDADE) b.velY++;
        else if(b.velY<0 && b.velY>-MAX_VELOCIDADE) b.velY--;
    }

    private void resetarBolas(){
        bolas.clear();
        bolas.add(new Bola(WIDTH/2, HEIGHT/2, VELOCIDADE_INICIAL, VELOCIDADE_INICIAL));
        bolaTamanho=20;
        jogador1RaqueteAltura=100;
        jogador2RaqueteAltura=100;
        powerUp=null;
    }

    private void ativarPowerUpSeColidiu(Bola b, boolean jogadorEsquerdo){
        if(powerUp!=null && powerUp.colidiu(b.x,b.y,bolaTamanho)){
            powerUp.ativo=false;
            PowerUp p = powerUp;
            powerUp = null;
            ativarPowerUp(p,b,jogadorEsquerdo);
        }
    }

    private void ativarPowerUp(PowerUp p, Bola b, boolean jogadorEsquerdo){
        switch(p.tipo){
            case BOLA_GIGANTE -> new Thread(() -> {
                int original = bolaTamanho;
                bolaTamanho=50;
                try{ Thread.sleep(5000); } catch(Exception ignored){}
                bolaTamanho=original;
            }).start();
            case RAQUETE_GIGANTE -> new Thread(() -> {
                if(jogadorEsquerdo) jogador1RaqueteAltura=200;
                else jogador2RaqueteAltura=200;
                try{ Thread.sleep(5000); } catch(Exception ignored){}
                if(jogadorEsquerdo) jogador1RaqueteAltura=100;
                else jogador2RaqueteAltura=100;
            }).start();
            case BOLA_RAPIDA -> new Thread(() -> {
                int velX=b.velX, velY=b.velY;
                b.velX*=2; b.velY*=2;
                try{ Thread.sleep(5000); } catch(Exception ignored){}
                b.velX=velX; b.velY=velY;
            }).start();
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        int t=e.getKeyCode();
        if(t==KeyEvent.VK_W && jogador1Y>0) jogador1Y-=20;
        if(t==KeyEvent.VK_S && jogador1Y<HEIGHT-jogador1RaqueteAltura) jogador1Y+=20;
        if(modo2Jogadores){
            if(t==KeyEvent.VK_UP && jogador2Y>0) jogador2Y-=20;
            if(t==KeyEvent.VK_DOWN && jogador2Y<HEIGHT-jogador2RaqueteAltura) jogador2Y+=20;
        }
    }
    @Override public void keyReleased(KeyEvent e){}
    @Override public void keyTyped(KeyEvent e){}

    public static void main(String[] args){
        int opcao = JOptionPane.showOptionDialog(null,"Escolha o modo de jogo:","Pong",
                JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,
                new String[]{"1 Jogador","2 Jogadores"},"1 Jogador");

        boolean modo2Players = opcao==1;
        JFrame frame=new JFrame("Pong com Poderes");
        Pong pong = new Pong(modo2Players);
        frame.add(pong);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

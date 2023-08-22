/* Name: Kristen Balisi   341426344
 * Date: 29/05/2023
 * Program Name: Space Shooter Game
 * Program Description: This Space Shooter Game is a multi-threaded application where 
 * the player controls a spaceship that must shoot an alien spaceship and avoid alien 
 * missiles. The user can control the player spaceship’s vertical movement using arrow keys  
 * and fire missiles using the space bar. The alien is computer-controlled, with randomized 
 * movement and firing of missiles. This program incorporates collision detection to
 * track missile hits and updates the on-screen player and alien scores accordingly.
 */

import java.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.*;
import sun.audio.*;
import java.io.*;

public class BalisiCPT {
  private static int FRAME_WIDTH = 800;
  private static int FRAME_HEIGHT = 800;
  private static int PLAYER_SHIP_HEIGHT = 135;
  private static int PLAYER_SHIP_WIDTH = 75;
  private static int ALIEN_SHIP_HEIGHT = 120;
  private static int ALIEN_SHIP_WIDTH = 75;
  private static int MISSILE_SPEED = 100;
  private static int DEFAULT_GAME_OBJECT_SPEED = 20;
  
  private static BufferedImage playerImg;
  private static BufferedImage playerMissileImg;
  private static BufferedImage alienImg;
  private static BufferedImage alienMissileImg;
  
  private static JFrame gameFrame;
  private static GamePanel gamePanel;
  
  private static GameObject player;
  private static GameObject alien;
  private static GameObject playerMissile;
  private static GameObject alienMissile;
  
  private static List<GameObject> playerMissiles;
  private static List<GameObject> alienMissiles;
  
  public static void main(String[] args) {
    gameFrame = new JFrame("BalisiCPT - Space Shooter Game");
    gameFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    gameFrame.setResizable(false);  
    gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameFrame.setLocationRelativeTo(null);
    gameFrame.setIconImage(new ImageIcon("balisiAlien.png").getImage());
    
    gamePanel = new GamePanel();
    gameFrame.add(gamePanel);
    gameFrame.setVisible(true);
    
    // position player on the left, vertically-centered
    int playerShipX = 20;
    int playerShipY = (FRAME_HEIGHT - PLAYER_SHIP_HEIGHT)/2;
    
    // position alien on the right, vertically-centered
    int alienShipX = FRAME_WIDTH - ALIEN_SHIP_WIDTH - 20;
    int alienShipY = (FRAME_HEIGHT - ALIEN_SHIP_HEIGHT)/2;
    
    // create objects belonging to the GameObject class
    player = new GameObject("player", playerShipX, playerShipY, playerImg, DEFAULT_GAME_OBJECT_SPEED);
    playerMissile = new GameObject("playerMissile", playerShipX, playerShipY, playerMissileImg, MISSILE_SPEED);
    alien = new GameObject("alien", alienShipX, alienShipY, alienImg, DEFAULT_GAME_OBJECT_SPEED);
    alienMissile = new GameObject("alienMissile", alienShipX, alienShipY, alienMissileImg, MISSILE_SPEED);
    
    playerMissiles = new ArrayList<>();
    alienMissiles = new ArrayList<>();
    
    // create and start threads, which take in a GameObject as an argument
    GameThread playerThread = new GameThread(player);
    GameThread playerMissileThread = new GameThread(playerMissile);
    GameThread alienThread = new GameThread(alien);
    GameThread alienMissileThread = new GameThread(alienMissile);
    
    playerThread.start();
    playerMissileThread.start();
    alienThread.start();
    alienMissileThread.start();
    
    // play background music
    playAudio("balisiBackgroundMusic.wav");
  }
  
  private static void playAudio(String audioFile) {
    try {
      InputStream inputStream = new FileInputStream(audioFile);
      AudioStream audioStream = new AudioStream(inputStream);
      AudioPlayer.player.start(audioStream);
    }
    
    catch (Exception e) {
      System.out.println("Exception in loading audio file: " + e.toString());
    }
  }
  
  private static class GamePanel extends JPanel implements KeyListener {
    GamePanel() {
      gameFrame.addKeyListener(this);
      this.setBackground(Color.black);
      
      try {
        playerImg = ImageIO.read(new File("balisiPlayer.png"));
        alienImg = ImageIO.read(new File("balisiAlien.png"));
        playerMissileImg = ImageIO.read(new File("balisiPlayerMissile.png"));
        alienMissileImg = ImageIO.read(new File("balisiAlienMissile.png"));
      }
      
      catch (Exception e) {
        System.out.println("Exception in loading image: " + e.toString());
      }
    }
    
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      // display player score
      String playerLabel = "Player";
      String playerScore = "00" + Integer.toString(player.getScore());
      
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.setColor(Color.CYAN);
      g.drawString(playerLabel, 100, 50);
      g.setFont(new Font("Arial", Font.BOLD, 48));
      g.drawString(playerScore.substring(playerScore.length() - 2), 103, 100);
      
      // display alien score
      String alienLabel = "Alien";
      String alienScore = "00" + Integer.toString(alien.getScore());
      
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.setColor(Color.GREEN);
      g.drawString(alienLabel, 600, 50);
      g.setFont(new Font("Arial", Font.BOLD, 48));
      g.drawString(alienScore.substring(alienScore.length() - 2), 603, 100);
      
      // draw alien missiles
      for (GameObject alienMissile : alienMissiles) {
        g.drawImage(alienMissile.getImage(), alienMissile.getX(), alienMissile.getY(), null);
      }
      
      // draw player missiles
      for (GameObject playerMissile : playerMissiles) {
        g.drawImage(playerMissile.getImage(), playerMissile.getX(), playerMissile.getY(), null);
      }
      
      // draw player
      g.drawImage(player.getImage(), player.getX(), player.getY(), null);
      
      // draw alien
      g.drawImage(alien.getImage(), alien.getX(), alien.getY(), null);
    }
    
    public void keyPressed(KeyEvent e) {
      int direction = e.getKeyCode();
      
      switch (direction) {
        case KeyEvent.VK_DOWN:
          player.moveDown();
          break;
        case KeyEvent.VK_UP:
          player.moveUp();
          break;
        case KeyEvent.VK_SPACE:
          // generate new player missile with appropriate thread id
          int missileX = player.getX() + PLAYER_SHIP_WIDTH;
          int missileY = player.getY() + 46;
          GameObject missile = new GameObject("playerMissile", missileX, missileY, playerMissileImg, MISSILE_SPEED);
          playerMissiles.add(missile);
          playAudio(player.getFireAudio());
      }
      
      gamePanel.repaint();
    }
    
    public void keyReleased(KeyEvent e) {}
    
    public void keyTyped(KeyEvent e) {}
  }
  
  /* the GameObject class defines game object properties, 
   * such as x and y coordinates, and game object behaviors, 
   * such as move up and move down
   */
  private static class GameObject {
    private String id;
    private int x, y;
    private BufferedImage image;
    private int imageWidth, imageHeight;
    private int distance;
    private int score;
    private String moveAudioFile, fireAudioFile;
    
    // parameterized constructor sets the default values of certain instance variables
    GameObject(String id, int x, int y, BufferedImage image, int distance) {
      this.id = id;
      this.x = x;
      this.y = y;
      this.image = image;
      this.distance = distance;
      this.score = 0;
    }
    
    // accessor and mutator methods to get/set the values of instance variables
    public int getX() {
      return x;
    }
    
    public void setX(int x) {
      this.x = x;
    }
    
    public int getY() {
      return y;
    }
    
    public void setY(int y) {
      this.y = y;
    }
    
    public BufferedImage getImage() {
      return image;
    }
    
    public int getScore() {
      return score;
    }
    
    public void setScore(int score) {
      this.score = score;
    }
    
    public String getFireAudio() {
      return fireAudioFile;
    }
    
    public void setFireAudio(String fireAudioFile) {
      this.fireAudioFile = fireAudioFile;
    }
    
    public String getMoveAudio() {
      return moveAudioFile;
    }
    
    public void setMoveAudio(String moveAudioFile) {
      this.moveAudioFile = moveAudioFile;
    }
    
    // methods related to game object movement
    public void moveUp() {
      // prevent sprite from moving beyond game panel score display
      if (y <= 100) {
        y = 100;
      }
      y -= distance;
    }
    
    public void moveDown() {
      // prevent sprite from moving out of bounds
      if (y >= (FRAME_HEIGHT - PLAYER_SHIP_HEIGHT)) {
        y = FRAME_HEIGHT - PLAYER_SHIP_HEIGHT;
      }
      y += distance;
    }
    
    public void moveRight() {
      x += distance;
    }
    
    public void moveLeft() {
      x -= distance;
    }
    
    // method used to randomize alien movement
    public void moveRandom() {
      int max = FRAME_HEIGHT - ALIEN_SHIP_HEIGHT;
      int min = 100;
      int range = max - min;
      
      y = (int) (Math.random() * range) + min;
      
      playAudio(moveAudioFile);
    }
    
    // methods related to collision detection
    public void getImageDimensions() {
      imageWidth = image.getWidth();
      imageHeight = image.getHeight();
    }
    
    public Rectangle getBounds() {
      // create a rectangle based on the game object's 
      // x and y coordinates, image width, and image height
      return new Rectangle(x, y, imageWidth, imageHeight);
    }
    
    public boolean isHit(GameObject sprite, GameObject missile) {
      sprite.getImageDimensions();
      missile.getImageDimensions();
      
      Rectangle spriteCollisionBox = sprite.getBounds();
      Rectangle missileCollisionBox = missile.getBounds();
      
      // check if the missile's rectangle is colliding
      // with the sprite's (player or alien) rectangle 
      return (missileCollisionBox.intersects(spriteCollisionBox));
    }
  }
  
  /* each GameThread has a GameObject (e.g., player, alien, player
   * missile, or alien missile) attribute
   */
  private static class GameThread implements Runnable {
    private Thread thread;
    private GameObject gameObject;
    
    GameThread(GameObject gameObject) {
      this.gameObject = gameObject;
    }
    
    public void run() {
      
      while(true) {
        
        try {
          
          // perform specific actions based on the game object's id
          if (gameObject.id == "player") {
            runPlayer();
            Thread.sleep(100);
          }
          
          if (gameObject.id == "alien") {
            runAlien();
            Thread.sleep(1500);
          }
          
          if (gameObject.id == "playerMissile") {
            runPlayerMissile();
            Thread.sleep(100);
          }
          
          if (gameObject.id == "alienMissile") {
            runAlienMissile();
            Thread.sleep(500);
          }
          
          gamePanel.repaint();
        }
        
        catch (Exception e) {
          System.out.println("Exception in running thread: " + e.toString());
        }
      }
    }
    
    public void start() {
      if (thread == null) {
        thread = new Thread(this);
        thread.start();
      }
    }
    
    public void runPlayer() {
      player.setFireAudio("balisiPlayerMissile.wav");
    }
    
    public void runAlien() {
      alien.setMoveAudio("balisiAlienMove.wav");
      alien.setFireAudio("balisiAlienMissile.wav");
      alien.moveRandom();
    }
    
    public void runPlayerMissile() {
      // create an ArrayList to store out-of-bounds player missiles that will be removed from the screen
      List<GameObject> playerMissilesToRemove = new ArrayList<>();
      
      for (GameObject playerMissile : playerMissiles) {
        // move existing missile
        playerMissile.moveRight();
        
        // check for enemy collision and update player score accordingly
        if (alien.isHit(alien, playerMissile)) {
          player.setScore(player.getScore() + 1);
        }
      }
      
      // remove off-screen missiles
      if (playerMissile.getX() > FRAME_WIDTH) {
        playerMissilesToRemove.add(playerMissile);
      }
      
      playerMissiles.removeAll(playerMissilesToRemove);
    }
    
    public void runAlienMissile() {
      int missileX = alien.getX() - ALIEN_SHIP_WIDTH;
      int missileY = alien.getY() + 46;
      
      // randomize creation of alien missiles
      // if the randomly-generated number in the range of 0-4 is odd, generate an alien missile
      int rand = new Random().nextInt(5);
      
      if (rand % 2 == 1) {
        GameObject missile = new GameObject("alienMissile", missileX, missileY, alienMissileImg, MISSILE_SPEED);
        alienMissiles.add(missile);
        playAudio(alien.getFireAudio());
      }
      
      // create an ArrayList to store out-of-bounds alien missiles that will be removed from the screen
      List<GameObject> alienMissilesToRemove = new ArrayList<>();
      
      for (GameObject alienMissile : alienMissiles) {
        // move existing missile
        alienMissile.moveLeft();
        
        // check for player collision and update alien score accordingly
        if (player.isHit(player, alienMissile)) {
          alien.setScore(alien.getScore() + 1);
        }
        
        // remove off-screen missiles
        if (alienMissile.getX() < 0) {
          alienMissilesToRemove.add(alienMissile);
        }
      }
      
      alienMissiles.removeAll(alienMissilesToRemove);
    }
  }
}


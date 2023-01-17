import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics; 
import java.awt.AlphaComposite;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.FontMetrics;
import java.math.BigInteger;
public class MergePlane extends JPanel implements ActionListener, MouseListener
{
    private static final long serialVersionUID = 1L;
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Merge Plane Game Simulation");
        Container c = frame.getContentPane();
        MergePlane plane = new MergePlane();
        c.add(plane);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /*Attributes
     * 
     */  
    //Booleans 
    public static boolean dropTheBox = false;    //check when to drop the box (when to start drawing the box)
    public static boolean slotsAreFull = false;  //check when slots are full (to know when box can drop again after merging occur at full slot)
    public static boolean flyPlane = false;      //check when there is at least one plane flying
    public boolean boxHasDropped = false;        //check when the box has dropped (to know when user can open it)
    public static boolean startMergeAnimation = false; //check when to do merging animation (when to start drawing merging animation)
    private boolean bigTrashBin = false;         //check when to show bigger image of the trash bin
    public static List<Boolean> checkSlot = new ArrayList<Boolean>(13); //keeping what slot are empty or occupied
    
    //Font 
    Font levelFont = new Font("Verdana", Font.PLAIN, 12);
    Font goldFont = new Font("Verdana", Font.PLAIN, 23);
    
    //Int 
    private final int panelWidth = 460;
    private final int panelHeight = 820;
    public static final int planeWidth = 75;
    public static final int planeHeight= 75;
    private int dropLocationX = 0;          //x location for dropping the box
    private int initialDropLocationY = 0;   //initial y location for dropping the box    
    private int finalDropLocationY = 0;     //final y location for dropping the box
    private int scalingPlaneStep = 0;     //step for scaling plane for switching animation
    private int scaleAmount = planeWidth; 
    public static int randomSpot = 0;     //the spot/slot the box will be dropped at
    public static List<Integer> allPlanesLv = new ArrayList<Integer>(); //keeping the landing plane current levels
    public static int currentSpawnPlaneLv = 0;                 //current plane level that spawns from box  
    public static List<Integer> flyingPlaneDisplacement = new ArrayList<Integer>(13);      //list of flying plane speed
    public static List<Integer> flyingPlaneInterval = new ArrayList<Integer>(13);          //list of flying plane interval/time to move
    public static List<Integer> distanceTake = new ArrayList<Integer>();      //list of distance traveled by each plane lv
    public static List<Integer> timeTake = new ArrayList<Integer>();          //list of time traveled by each plane lv
    public static final int straightDelay = 0;        //time wait before planes start flying
    public static List<BigInteger> planeGoldEarn = new ArrayList<BigInteger>(); //Gold Earn of all planes
    private BigInteger totalGold = new BigInteger("0");
    private int goldTextYLoc = 100;
    
    /**
     current location of all flying plane
     allPlaneLoc.get(#); - get which plane 
     allPlaneLoc.get(#).get(0); - get the # plane's x location
     allPlaneLoc.get(#).get(1); - get the # plane's y location
     */
    List<List<Integer>> allPlaneLoc = new ArrayList<>(13); 
    
    //String
    public static String[] allLandingPlanes = getAllLandingPlanes(); //all picture names of landing planes
    public static String[] allFlyingPlanes = getAllFlyingPlanes();   //all picture names of flying planes
    
     //Images
    ImageIcon background = new ImageIcon("Pics/background.jpg");
    ImageIcon planeBox = new ImageIcon("Pics/planeBox.png");
    ImageIcon trashBinIcon = new ImageIcon("Pics/TrashBin.JPG");
    ImageIcon currentSpawnPlane = new ImageIcon(new ImageIcon(allLandingPlanes[currentSpawnPlaneLv]).getImage().getScaledInstance(planeWidth, planeHeight, Image.SCALE_SMOOTH));   
    List<ImageIcon> flyingPlaneImages = new ArrayList<ImageIcon>(10);
    List<List<ImageIcon>> planeScalingIcons = new ArrayList<>(); //List of all planes being scaled 5 times for merging animation
    
    //Label
    JLabel levelLabel = new JLabel("1");
    JLabel goldLabel = new JLabel("0");
    public static List<JLabel> transLandingPlanes = new ArrayList<JLabel>(13); //transparent image of landing planes
    public static List<JLabel> landingPlanes = new ArrayList<JLabel>(13);      //landing planes
    
    //Points
    public static List<Point> slotNumber = new ArrayList<Point>(13); //keep the location of the slots
    Point finishLinePoint = new Point(425, 409);
    Point startRightPoint = new Point(42, 200);
    Point halfRightPoint = new Point(83, 115);
    Point endRightPoint = new Point(177, 75); 
    
    Point startDownPoint = new Point(283+20, 75);
    Point halfDownPoint = new Point(376+15, 115);
    Point endDownPoint = new Point(425+8, 200);
    
    Point startLeftPoint = new Point(425+8, 620+7);
    Point halfLeftPoint = new Point(376+15, 713+7);
    Point endLeftPoint = new Point(283+20, 755);
    
    Point startUpPoint = new Point(177, 755);
    Point halfUpPoint = new Point(83, 713+7);
    Point endUpPoint = new Point(42, 620+7);
    
    //MouseHandler 
    List<MouseHandler> handler = new ArrayList<MouseHandler>(13); //Mouse handler for each plane
    
    //Timer 
    Timer boxSpawnTime = new Timer(1000, this);     //time for box to drop
    Timer boxDropTime = new Timer(0, this);         //time takes for dropping the box
    Timer openBoxTime = new Timer(9000, this);      //time takes for the box to open on it own /9
    public static List<java.util.Timer> flyingPlaneSpeed = new ArrayList<java.util.Timer>(10); //Timer for all flying planes' speed

    Point draggedSwitchPoint = new Point();
    Point targetSwitchPoint = new Point();
    private boolean startSwitchAnimation = false;
    private int switchAniXLoc = 300;  //plane x location in switching animation
    private int switchAniYLoc = 700;  //plane y location in switching animation
    private int switchDraggedPlane = 0;
    private int switchTargetPlane = 0;
    private int switchDistanceXTake = 0;
    private int switchDistanceYTake = 0; 
    ImageIcon currentSwitchDraggedPlane = new ImageIcon();
    ImageIcon currentSwitchTargetPlane = new ImageIcon();
    Timer switchAnimationTime = new Timer(15, this);

    Timer shakeBoxTime = new Timer(50, this);
    Timer squashBoxTime = new Timer(5, this);
    private boolean shakeBox = false;
    private int shakeBoxStep = 0;
    private int planeBoxHeightSize = planeHeight+15;
    ImageIcon tiltedBox = rotatePlaneImage("Pics/planeBox.png", 345);

    /*Constructor
     * 
     */
    public MergePlane() 
    {
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setLayout(null); 
        addMouseListener(this);
        
        calculateSpeed();    //calculate the displacement and interval for all plane 
        calculateGoldEarn(); //calculate gold earn of all plane and store them in an array of BigInteger
        initializeGoldEarnLabels();
        initializeRotatedImageList();
        initializeFlyingSpace();
        calculateNextPlanePercentage();
    
        //level label
        levelLabel.setFont(levelFont);
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setBounds(18, -3, 50, 50);       
        add(levelLabel);
        
        //gold label
        goldLabel.setFont(goldFont);
        goldLabel.setForeground(Color.WHITE);
        goldLabel.setBounds(getGoldTextLocation(180, "0"), 100, 200, 60);
        add(goldLabel);

        //Intitialize point slot array
        slotNumber.add(new Point(147, 233));
        slotNumber.add(new Point(233, 233));
        slotNumber.add(new Point(320, 233));
        slotNumber.add(new Point(147, 320));
        slotNumber.add(new Point(233, 320));
        slotNumber.add(new Point(320, 320));
        slotNumber.add(new Point(147, 407));
        slotNumber.add(new Point(233, 407)); 
        slotNumber.add(new Point(320, 407));
        slotNumber.add(new Point(167, 493));
        slotNumber.add(new Point(287, 493));
        slotNumber.add(new Point(167, 580));
        slotNumber.add(new Point(287, 580));
             
        for (int j = 0; j < 13; j++) { 
            landingPlanes.add(new JLabel());      //Initialize landing plane labels
            transLandingPlanes.add(new JLabel()); //Initialize transparent landing plane labels
            checkSlot.add(false); //Knowing what slot is empty or occupied (empty at initialization)
            allPlanesLv.add(-1);  //Keeping the level of all current landing planes (also as flying planes)
            
            //Adding empty landing planes and transparent landing planes
            transLandingPlanes.get(j).setBounds(slotNumber.get(j).x - planeWidth/2, slotNumber.get(j).y - planeHeight/2, planeWidth, planeHeight);
            transLandingPlanes.get(j).setVisible(false);
            transLandingPlanes.get(j).addMouseListener(this); //make transparent plane listen to this class mouse listener
            landingPlanes.get(j).setBounds(slotNumber.get(j).x - planeWidth/2, slotNumber.get(j).y - planeHeight/2, planeWidth, planeHeight);
            landingPlanes.get(j).setVisible(false);
            add(landingPlanes.get(j));
            add(transLandingPlanes.get(j));
            
            //Initilize mousehandler and add them to each plane 
            //Allowing planes to be dragged and released 
            handler.add(new MouseHandler(this));
            landingPlanes.get(j).addMouseMotionListener(handler.get(j));
            landingPlanes.get(j).addMouseListener(handler.get(j));   
            
            flyingPlaneImages.add(new ImageIcon());  //Initialize flying plane images
            flyingPlaneDisplacement.add(0);          //Initialize all planes displacement
            flyingPlaneInterval.add(0);              //Initialize all planes interval 
            flyingPlaneSpeed.add(new java.util.Timer()); //Initilize all flying planes speed          
            
            //Initialize all flying plane location
            allPlaneLoc.add(new ArrayList<>());
            allPlaneLoc.get(j).add(35);  //Initial x location
            allPlaneLoc.get(j).add(350); //Initial y location
        }

        numberOfPlaneLabel.setText("0/10");
        numberOfPlaneLabel.setBounds(16,234,60,50);
        numberOfPlaneLabel.setForeground(Color.ORANGE);
        numberOfPlaneLabel.setFont(new Font("Verdana", Font.PLAIN, 15));
        add(numberOfPlaneLabel);

        nextPlanePercentage.setBounds(222,160,50,50);
        nextPlanePercentage.setForeground(Color.white);
        nextPlanePercentage.setFont(new Font("Verdana", Font.PLAIN, 15));
        add(nextPlanePercentage);

        startLightingFlightPath();

        boxSpawnTime.start();
        this.validate();
    }

    /*Methods
     * 
     */
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(background.getImage(), 0, 0, this.getWidth(), this.getHeight(), this); 

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setStroke(new java.awt.BasicStroke(3)); // thickness of 3.0f
        g2.setColor(Color.white);
        g2.drawRoundRect(150, 176, percentageContainerLength, 20, 9, 9);
        g2.setColor(new Color(253, 191, 26));
        g2.fillRoundRect(151, 178, percentageBarLength, 17, 5, 5);
        g.drawImage(new ImageIcon(allLandingPlanes[currentHighestPlane]).getImage(), 110, 173, 35, 35, this);
        g.drawImage(new ImageIcon(allLandingPlanes[currentHighestPlane+1]).getImage(), 320, 170, 35, 35, this);

        int flySpaceX = -50;
        int flySpaceY = 316;
        for (int j = 0; j < 10; j++) {
            if (flyingSpaceIcons.get(j) == 0)  //0 = empty
                g.drawImage(empty.getImage(), flySpaceX, flySpaceY, 166, 280, this);
            else if (flyingSpaceIcons.get(j) == 1)
                g.drawImage(occupied.getImage(), flySpaceX+2, flySpaceY+6, 162, 280, this);
            else      
                g.drawImage(opaqueOccupied.getImage(), flySpaceX+2, flySpaceY+6, 162, 280, this);
            flySpaceY -= 19;
        }
        g.drawImage(finishLine.getImage(), finishLinePoint.x-finishLineSize/2-5, finishLinePoint.y-finishLineSize/2, finishLineSize, finishLineSize, this);
        if (showCoinEarn == true) 
            for(int j = 0; j < coinEarnLoc.size(); j++)
                g.drawImage(coinEarnPic.getImage(), coinEarnLoc.get(j).x-coinEarnSize/2, coinEarnLoc.get(j).y-coinEarnSize/2, coinEarnSize, coinEarnSize, this);
        if (bigTrashBin == true) 
            g.drawImage(trashBinIcon.getImage(), 206, 767, 47, 57, this);
        if (dropTheBox == true) 
            if (shakeBox == false) {
                if (planeBoxHeightSize < planeHeight+15) {
                    Image plane = new ImageIcon(allLandingPlanes[currentSpawnPlaneLv]).getImage();
                    g.drawImage(plane, slotNumber.get(randomSpot).x-planeWidth/2, slotNumber.get(randomSpot).y-planeHeight/2, planeWidth, planeHeight, this);
                }
                g.drawImage(planeBox.getImage(), dropLocationX-7, initialDropLocationY-20, planeWidth+15, planeBoxHeightSize, this); //drawing the box
            }
            else    
                g.drawImage(tiltedBox.getImage(), dropLocationX-7, initialDropLocationY-20, planeWidth+15, planeHeight+15, this); //drawing the titled box
        if (flyPlane == true) 
            for (int j = 0; j < flyingPlaneInterval.size(); j++) 
                if (flyingPlaneInterval.get(j) != 0) {//if there is a plane flying
                    //drawing flying planes
                    g.drawImage(flyingPlaneImages.get(j).getImage(), allPlaneLoc.get(j).get(0)-75/2, allPlaneLoc.get(j).get(1)-75/2, 60, 60, this);
                }
        if (startMergeAnimation == true) {
            if (mergeAnimationPlane.get(0))
                if (!mergeAnimationScalePart.get(0)) {
                    g.drawImage(mergeAnimationImage.get(0).getImage(), mergeAnimationLoc.get(0).get(1), mergeAnimationLoc.get(0).get(0), planeWidth, planeHeight, this);
                    g.drawImage(mergeAnimationImage.get(0).getImage(), mergeAnimationLoc.get(0).get(2), mergeAnimationLoc.get(0).get(0), planeWidth, planeHeight, this);
                }      
                else 
                    g.drawImage(mergeAnimationImage.get(0).getImage(), mergeAnimationTarget.get(0).x-mergeAnimationScale.get(0)/2, 
                                                                       mergeAnimationTarget.get(0).y-mergeAnimationScale.get(0)/2, mergeAnimationScale.get(0), mergeAnimationScale.get(0), this);
            if (mergeAnimationPlane.get(1))
                if (!mergeAnimationScalePart.get(1)) {
                    g.drawImage(mergeAnimationImage.get(1).getImage(), mergeAnimationLoc.get(1).get(1), mergeAnimationLoc.get(1).get(0), planeWidth, planeHeight, this);
                    g.drawImage(mergeAnimationImage.get(1).getImage(), mergeAnimationLoc.get(1).get(2), mergeAnimationLoc.get(1).get(0), planeWidth, planeHeight, this);
                }      
                else 
                    g.drawImage(mergeAnimationImage.get(1).getImage(), mergeAnimationTarget.get(1).x-mergeAnimationScale.get(1)/2, 
                                                                       mergeAnimationTarget.get(1).y-mergeAnimationScale.get(1)/2, mergeAnimationScale.get(1), mergeAnimationScale.get(1), this);
        }
        if (startSwitchAnimation == true) {
            g.drawImage(currentSwitchDraggedPlane.getImage(), targetSwitchPoint.x-scaleAmount/2, targetSwitchPoint.y-scaleAmount/2, scaleAmount, scaleAmount, this);
            g.drawImage(currentSwitchTargetPlane.getImage(), switchAniXLoc, switchAniYLoc, scaleAmount, scaleAmount, this);
        }
        if (drawTheDust == true) 
            for (int j = 0; j < dustLoc.length; j++) {
                g.drawImage(whiteTrashDust.getImage(), dustLoc[j].x-dustSize/2, dustLoc[j].y-dustSize/2, dustSize, dustSize, this);
        }
        if (drawTrashCoin == true) {
            for (int j = 0; j < sellingCoins.length; j++) {
                ImageIcon cur = coinSpinPics[sellingCoins[j]]; 
                int x = sellingCoinInitLoc[j].x;
                int y = sellingCoinInitLoc[j].y;
                g.drawImage(cur.getImage(), x-cur.getIconWidth()/12, y-cur.getIconHeight()/12, cur.getIconWidth()/6, cur.getIconHeight()/6, this);
            }
        }
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        if (e.getSource() == boxSpawnTime) { //when box spawn time come
            List<Integer> emptySlot = new ArrayList<Integer>();
            for (int j = 0; j < checkSlot.size(); j++) { //find empty slots to drop the box on 
                if (checkSlot.get(j) == false)    
                    emptySlot.add(j);
            }
            if (emptySlot.size() > 0) { //if there is at least one empty slot
                randomSpot = emptySlot.get((int)(Math.random() * emptySlot.size())); //choose a random spot in all empty slot(s)
                finalDropLocationY = slotNumber.get(randomSpot).y - planeHeight/2;   //get the random spot final y location
                dropLocationX = slotNumber.get(randomSpot).x - planeWidth/2;         //get the random spot x location
                dropTheBox = true;   //agree to start dropping the box
                boxSpawnTime.stop(); //end box spawning time
                boxDropTime.start(); //start box dropping time
            }
            else boxSpawnTime.stop();   //if there is no empty slot - stop spawning box 
        }
        if (e.getSource() == boxDropTime) { //when dropping time come
            initialDropLocationY += 5;      //increase y location as to drop it
            if (initialDropLocationY >= finalDropLocationY) { //when box has dropped at final y location 
                boxDropTime.stop();    //stop dropping the box
                boxHasDropped = true;  //agree that the box has finished dropping
                openBoxTime.start();   //start counting time when to open the box
            } 
        }
        if (e.getSource() == openBoxTime) { //when time to open the box come
            boxHasDropped = false; //when box's opened, new box has not dropped
            openBoxTime.stop();
            shakeBoxTime.start();  //start shaking the box before opening
        }
        if (e.getSource() == shakeBoxTime) { //when time to open the box- shake the box first
            int totalShakeStep = 8;
            if (shakeBoxStep < totalShakeStep) { //shake the box 8 times only
                if (shakeBox == false) shakeBox = true; //shakeBox by tilting the box and then returning back to normal 
                else shakeBox = false;
                shakeBoxStep += 1;
            }
            else if (shakeBoxStep == totalShakeStep) { //after shaking the box 8 times
                shakeBoxTime.stop();
                squashBoxTime.start();
            }
        }
        if (e.getSource() == squashBoxTime) {
            int squashAmount = 10;  //squash the box in size 
            planeBoxHeightSize -= squashAmount;
            initialDropLocationY += squashAmount/2+2;
            if (planeBoxHeightSize <= 10) {
                shakeBox = false;
                planeBoxHeightSize = planeHeight+15; //reset plane box height
                shakeBoxStep = 0;   //reset shaking box step
                squashBoxTime.stop();
                openTheBox(); 
            }
        }
        if (e.getSource() == switchAnimationTime) {
            if (scalingPlaneStep < 10) {
                if (scalingPlaneStep < 5) scaleAmount += 7;
                else scaleAmount -= 7;
            }
            scalingPlaneStep += 1;
            switchAniXLoc += switchDistanceXTake;
            switchAniYLoc += switchDistanceYTake;
            if (scalingPlaneStep == 11) {
                int tempLv = allPlanesLv.get(switchDraggedPlane); 
                setPlaneLv(switchDraggedPlane, allPlanesLv.get(switchTargetPlane));
                setPlaneLv(switchTargetPlane, tempLv);
                landingPlanes.get(switchDraggedPlane).setVisible(true);
                landingPlanes.get(switchTargetPlane).setVisible(true);
                scalingPlaneStep = 0;
                startSwitchAnimation = false;
                switchAnimationTime.stop();
            }
        }
        if (e.getSource() == dustPermeateTime) {
            for (int j = 0; j < dustLoc.length; j++) {
                dustLoc[j].x += (initialDustLoc[j].x-trashBinPoint.x);
                dustLoc[j].y += (initialDustLoc[j].y-trashBinPoint.y);
            }
            if (dustSize > 0) dustSize -= 6;
            else {
                dustPermeateTime.stop();
                drawTheDust = false;
                dustSize = 30;
            }
        }
        if (e.getSource() == coinSpinTime) {
            for (int j = 0; j < sellingCoins.length; j++)
                if (sellingCoins[j] < coinSpinPics.length-1) sellingCoins[j]++;
                else sellingCoins[j] = 0;
        }
        if (e.getSource() == coinPopTime) {
            falldowntime++;
            for (int j = 0; j < sellingCoins.length; j++) 
                if (falldowntime < 9) sellingCoinInitLoc[j].y -= (j+1)*2;
                else sellingCoinInitLoc[j].y += 15;
            if (sellingCoinInitLoc[sellingCoinInitLoc.length-1].y > panelHeight+50) {
                coinSpinTime.stop();
                coinPopTime.stop();
                falldowntime = 0;
                drawTrashCoin = false;
            }
        }
        this.revalidate();
        repaint();     
    }

    //calculate the percentage of next plane
    private int percentageContainerLength = 165;
    private int percentageBarLength = 0;
    JLabel nextPlanePercentage = new JLabel("0.00%");
    private int currentHighestPlane = 0;
    public int getCurrentHighestLv() {
        return currentHighestPlane;
    }
    public void setCurrentHighestPlane(int lv) {
        currentHighestPlane = lv;
    }
    public void calculateNextPlanePercentage() {
        long num = 0;
        for (int j = 0; j < allPlanesLv.size(); j++) 
            if (checkSlot.get(j)) 
                num += (long)Math.pow(2, allPlanesLv.get(j));
        long goal = (long)Math.pow(2, currentHighestPlane+1);
        double percent = (double)(num)/goal*100;
        if (percent >= 100) percent = 100;
        String percentText = String.format("%.2f", percent)+"%";
        FontMetrics textMeasurement = getFontMetrics(new Font("Verdana", Font.PLAIN, 15));
        int x = 222 + textMeasurement.stringWidth("50%")/2 - textMeasurement.stringWidth(percentText)/2;
        percentageBarLength = (int)((percentageContainerLength-1)*percent/100);
        nextPlanePercentage.setText(percentText);
        nextPlanePercentage.setBounds(x, 160, 200, 50);
    }

    JLabel[] goldEarnLabels = new JLabel[13];
    java.util.Timer[] goldEarnLabelsPopTime = new java.util.Timer[13];
    private void initializeGoldEarnLabels() {
        ImageIcon planeCoinIcon = new ImageIcon(new ImageIcon("Pics/PlaneCoin.JPG").getImage().getScaledInstance(25,25, Image.SCALE_SMOOTH));
        for (int j = 0; j < goldEarnLabels.length; j++) {
            goldEarnLabelsPopTime[j] = new java.util.Timer();
            goldEarnLabels[j] = new JLabel();
            goldEarnLabels[j].setForeground(Color.WHITE);
            goldEarnLabels[j].setIcon(planeCoinIcon);
            goldEarnLabels[j].setHorizontalTextPosition(SwingConstants.LEFT);
            goldEarnLabels[j].setFont(new Font("Verdana", Font.PLAIN, 10));
            this.add(goldEarnLabels[j]);
        }
    }
    private void showGoldEarn(int whatPlane, String goldEarnText) {
        goldEarnLabels[whatPlane].setVisible(true);
        goldEarnLabelsPopTime[whatPlane].cancel();
        goldEarnLabelsPopTime[whatPlane] = new java.util.Timer();
        goldEarnLabelsPopTime[whatPlane].schedule(new GoldEarnPopUp(whatPlane, goldEarnText), 0, 90);
    }
    public class GoldEarnPopUp extends TimerTask { //330 392
        private int whatPlane, x, y, width, height;
        private int increment = 2;
        private int steps = 0;
        private int size = 10;
        public GoldEarnPopUp(int whatPlane, String goldEarnText) {
            this.whatPlane = whatPlane;
            y = 330 + (int)(Math.random() * 63);
            x = getGoldTextLocation(340, getTotalGoldText(new BigInteger(goldEarnText)));
            width = 200; 
            height = 50;
            goldEarnLabels[whatPlane].setBounds(x, y, width, height);
            goldEarnLabels[whatPlane].setText(goldEarnText);
        }

        @Override 
        public void run() {
            y -= increment;
            steps++;
            size += increment;
            goldEarnLabels[whatPlane].setFont(new Font("Verdana", Font.PLAIN, size));
            goldEarnLabels[whatPlane].setBounds(x, y, width, height);
            if (steps == 7) {
                goldEarnLabelsPopTime[whatPlane].cancel();
                goldEarnLabelsPopTime[whatPlane] = new java.util.Timer();
                goldEarnLabels[whatPlane].setVisible(false);
            }
        }
    }

    ImageIcon coinEarnPic = new ImageIcon("Pics/coinEarn.PNG");
    List<Point> coinEarnLoc = new ArrayList<>();
    private int coinEarnSize = 100;
    private boolean readyToPopCoinEarn = true;
    private boolean showCoinEarn = false;
    
    public class RunCoinEarnPopUp implements Runnable {
        private Thread t;
        private int coinEarnCount = 0;
        private int coinEarnAppearDelay = 30;
        private int coinEarnScatterDelay = 50;
        private int distance = 5;
        
        public RunCoinEarnPopUp() {
            readyToPopCoinEarn = false;
            showCoinEarn = true;
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while(!t.isInterrupted()) 
                if (coinEarnCount < 25) {
                    for (int j = 0; j < 7; j++) {
                        int xLoc = (int)(Math.random() * 45) + finishLinePoint.x-23;
                        int yLoc = (int)(Math.random() * 45) + finishLinePoint.y-23;
                        coinEarnLoc.add(new Point(xLoc, yLoc));
                        repaint();
                    }
                    try {
                        Thread.sleep(coinEarnAppearDelay);
                    }
                    catch(InterruptedException e) {
                        System.out.println("CoinPopUpThreadSleep");
                    }
                    coinEarnCount += 7;
                }
                else {
                    for (int j = 0; j < coinEarnLoc.size(); j++) {
                        if (coinEarnLoc.get(j).x > finishLinePoint.x) coinEarnLoc.get(j).x += distance;
                        else if (coinEarnLoc.get(j).x < finishLinePoint.x) coinEarnLoc.get(j).x -= distance;
                        if (coinEarnLoc.get(j).y > finishLinePoint.y) coinEarnLoc.get(j).y += distance;
                        else if (coinEarnLoc.get(j).y < finishLinePoint.y) coinEarnLoc.get(j).y -= distance;
                    }
                    coinEarnSize -= 20;
                    repaint();
                    try {
                        Thread.sleep(coinEarnScatterDelay);
                    }
                    catch(InterruptedException e) {
                        System.out.println("CoinEarnScatterThreadSleep");
                    }
                    if (coinEarnSize <= 0) {
                        t.interrupt();  
                        coinEarnLoc.clear();
                        coinEarnSize = 100;
                        showCoinEarn = false;
                        readyToPopCoinEarn = true;
                    }
                }
        }
    }

    ImageIcon finishLine = new ImageIcon("Pics/finishLine.PNG");
    java.util.Timer finishLinePopUpTime = new java.util.Timer();
    private int finishLineSize = 180;
    private boolean readyToPopFinishLine = true;
    private void popUpTheFinishLine() {
        if (readyToPopFinishLine == true) 
            finishLinePopUpTime.schedule(new FinishLinePopUp(), 0, 1);   
        if (readyToPopCoinEarn == true) {
            RunCoinEarnPopUp r = new RunCoinEarnPopUp();
        }
    }
    public class FinishLinePopUp extends TimerTask {
        private int increment = 6;
        private boolean maxSize = false;
        @Override 
        public void run() {
            if (maxSize == false) finishLineSize += increment;
            else finishLineSize -= increment;
            if (finishLineSize > 250) maxSize = true;
            if (finishLineSize < 180) {
                finishLineSize = 180;
                readyToPopFinishLine = false;
                finishLinePopUpTime.cancel();
                finishLinePopUpTime = new java.util.Timer();
                readyToPopFinishLine = true;
            }
        }
    }

    Integer[] sellingCoins = new Integer[5]; //integer as representing the state of spinning coin instead
    Point[] sellingCoinInitLoc = new Point[sellingCoins.length];
    private int falldowntime = 0;
    Timer coinSpinTime = new Timer(40, this);
    Timer coinPopTime = new Timer(50, this);
    private boolean drawTrashCoin = false;
    public void createTrashCoins() 
    {
        for (int j = 0; j < sellingCoins.length; j++) {
            sellingCoins[j] = (int)(Math.random() * coinSpinPics.length);
            int radius = 15;
            int x = trashBinPoint.x-radius+(int)(Math.random() * radius*2+1);
            int y = trashBinPoint.y-radius+(int)(Math.random() * radius*2+1);
            sellingCoinInitLoc[j] = new Point(x,y);
        }
        coinSpinTime.start();
        coinPopTime.start();
        drawTrashCoin = true;
    }

    ImageIcon[] coinSpinPics = getCoinSpinPics();
    private ImageIcon[] getCoinSpinPics() { 
        File coinFile = new File("Pics/Coins");
        String[] names = coinFile.list();
        ImageIcon[] states = new ImageIcon[names.length];
        for (int j = 0; j < names.length; j++) 
            states[j] = new ImageIcon("Pics/Coins/" + names[j]);
        return states;
    }

    Timer dustPermeateTime = new Timer(80, this);
    Point[] dustLoc = new Point[25];
    Point[] initialDustLoc = new Point[25];
    private int dustSize = 30;
    ImageIcon whiteTrashDust = new ImageIcon(new ImageIcon("Pics/WhiteDust.JPG").getImage().getScaledInstance(dustSize,dustSize, Image.SCALE_SMOOTH));
    Point trashBinPoint = new Point(229, 794); //center Point
    private boolean drawTheDust = false;
    public void createTrashDust() 
    {
        int radius = 18;
        for (int j = 0; j < dustLoc.length; j++) {
            int xPick = trashBinPoint.x-radius+(int)(Math.random() * (radius*2+1));
            int yPick = trashBinPoint.y-radius+(int)(Math.random() * (radius*2+1));
            dustLoc[j] = new Point();
            initialDustLoc[j] = new Point();
            dustLoc[j].x = xPick;
            dustLoc[j].y = yPick;
            initialDustLoc[j].x = xPick;
            initialDustLoc[j].y = yPick;
        }
        drawTheDust = true;
        dustPermeateTime.start();
    }
         
    public void openTheBox() 
    {
        dropTheBox = false; //agree to remove the box
        repaint(); //remove the box
        allPlanesLv.set(randomSpot, currentSpawnPlaneLv);
        currentSpawnPlane = new ImageIcon(new ImageIcon(allLandingPlanes[allPlanesLv.get(randomSpot)]).getImage().getScaledInstance(planeWidth, planeHeight, Image.SCALE_SMOOTH));
        landingPlanes.get(randomSpot).setIcon(currentSpawnPlane);
        checkSlot.set(randomSpot, true);
        landingPlanes.get(randomSpot).setVisible(true);
        calculateNextPlanePercentage();
        randomSpot = 15; //assigned to another number so that plane just being dropped can be merged before another box is dropped
        for (int j = 0; j < checkSlot.size(); j++) //checking if all slots are full or not to drop more box 
            if (checkSlot.get(j) == false) {
                initialDropLocationY = 0;
                boxSpawnTime.start();
                slotsAreFull = false;
                break;
            }
            else slotsAreFull = true;
    }

    public void startSwitchingAnimation(int slot, int targetSlot)
    {   
        draggedSwitchPoint = new Point(slotNumber.get(slot).x-planeWidth/2, slotNumber.get(slot).y-planeHeight/2);
        targetSwitchPoint = slotNumber.get(targetSlot);
        switchAniXLoc = targetSwitchPoint.x-planeWidth/2;
        switchAniYLoc = targetSwitchPoint.y-planeHeight/2;
        switchDraggedPlane = slot;
        switchTargetPlane = targetSlot;
        double xDif = Math.abs(switchAniXLoc-draggedSwitchPoint.x);
        double yDif = Math.abs(switchAniYLoc-draggedSwitchPoint.y);
        switchDistanceXTake = (int)Math.round(xDif/11.0);
        switchDistanceYTake = (int)Math.round(yDif/11.0);
        if (targetSwitchPoint.x == draggedSwitchPoint.x+planeWidth/2) switchDistanceXTake = 0;
        else if (targetSwitchPoint.x > draggedSwitchPoint.x+planeWidth/2) switchDistanceXTake *= -1;
        if (targetSwitchPoint.y == draggedSwitchPoint.y+planeHeight/2) switchDistanceYTake = 0;
        else if (targetSwitchPoint.y > draggedSwitchPoint.y+planeHeight/2) switchDistanceYTake *= -1;
        currentSwitchDraggedPlane = new ImageIcon(allLandingPlanes[allPlanesLv.get(switchDraggedPlane)]);
        currentSwitchTargetPlane = new ImageIcon(allLandingPlanes[allPlanesLv.get(switchTargetPlane)]);
        startSwitchAnimation = true;
        switchAnimationTime.start();
    }

    //0- Y loc
    //1- rightX loc
    //2- leftX loc    
    //List<Integer> mergeAnimationLoc1 = new ArrayList<>(Arrays.asList(0,0,0));
    //ImageIcon mergeAniPlaneImage = new ImageIcon();
    //Point targetMergePoint = new Point(); 

    List<List<Integer>> mergeAnimationLoc = new ArrayList<>(Arrays.asList(new ArrayList<>(Arrays.asList(0,0,0)), new ArrayList<>(Arrays.asList(0,0,0)) ));
    List<ImageIcon> mergeAnimationImage = new ArrayList<>(Arrays.asList(new ImageIcon(), new ImageIcon()));
    List<Point> mergeAnimationTarget = new ArrayList<>(Arrays.asList(new Point(), new Point()));
    List<Integer> mergeAnimationScale = new ArrayList<>(Arrays.asList(planeWidth, planeWidth));
    List<Boolean> mergeAnimationPlane = new ArrayList<>(Arrays.asList(false, false));
    List<Boolean> mergeAnimationScalePart = new ArrayList<>(Arrays.asList(false, false));
    public void startMergingAnimation(int slot, int targetSlot)
    {
        startMergeAnimation = true;
        int whichOne = 0;
        if (!mergeAnimationPlane.get(0)) mergeAnimationPlane.set(0, true);
        else {
            whichOne = 1;
            mergeAnimationPlane.set(1, true);
        }
        mergeAnimationTarget.set(whichOne, slotNumber.get(targetSlot));     //setting target point
        mergeAnimationLoc.get(whichOne).set(0, mergeAnimationTarget.get(whichOne).y - planeHeight/2);//setting Y loc
        mergeAnimationLoc.get(whichOne).set(1, mergeAnimationTarget.get(whichOne).x - planeWidth/2); //setting X right loc
        mergeAnimationLoc.get(whichOne).set(2, mergeAnimationLoc.get(whichOne).get(1));              //setting X left loc
        mergeAnimationImage.set(whichOne, new ImageIcon(allLandingPlanes[allPlanesLv.get(slot)]));
        MergeAnimation mer = new MergeAnimation(targetSlot, whichOne);
    }
    public class MergeAnimation implements Runnable {
        private boolean planeCombinePart = true;
        private boolean secondPart = false;
        private int gap = 10, distance = 5, scalingStep = 0;
        private int targetSlot, whichOne;
        Thread t;
        public MergeAnimation(int targetSlot, int whichOne) {
            this.targetSlot = targetSlot;
            this.whichOne = whichOne;
            t = new Thread(this);
            t.start();
        }
        public void run() {
            while(!t.isInterrupted()) {
                if (planeCombinePart) {
                    if (secondPart == false) {
                        mergeAnimationLoc.get(whichOne).set(1, mergeAnimationLoc.get(whichOne).get(1)+distance);
                        mergeAnimationLoc.get(whichOne).set(2, mergeAnimationLoc.get(whichOne).get(2)-distance);
                    }
                    else {
                        mergeAnimationLoc.get(whichOne).set(1, mergeAnimationLoc.get(whichOne).get(1)-distance);
                        mergeAnimationLoc.get(whichOne).set(2, mergeAnimationLoc.get(whichOne).get(2)+distance);
                    }
                    repaint();
                    if (mergeAnimationLoc.get(whichOne).get(1) >= mergeAnimationTarget.get(whichOne).x + gap) 
                        secondPart = true;
                    if (mergeAnimationLoc.get(whichOne).get(2) >= mergeAnimationLoc.get(whichOne).get(1)) {
                        secondPart = false;
                        planeCombinePart = false;
                        mergeAnimationImage.set(whichOne, new ImageIcon(allLandingPlanes[allPlanesLv.get(targetSlot)]));
                        mergeAnimationScalePart.set(whichOne, true);
                    }
                    try {
                        Thread.sleep(10);
                    }
                    catch(InterruptedException e) {
                        System.out.println("Plane combine sht");
                    }
                }
                else {
                    if (scalingStep < 5) mergeAnimationScale.set(whichOne, mergeAnimationScale.get(whichOne)+7);
                    else mergeAnimationScale.set(whichOne, mergeAnimationScale.get(whichOne)-7);
                    scalingStep += 1;
                    repaint();
                    try {
                        Thread.sleep(15);
                    }
                    catch(InterruptedException e) {
                        System.out.println("Plane scaling sht");
                    }
                    if (scalingStep == 10) {
                        mergeAnimationScalePart.set(whichOne, false);
                        mergeAnimationPlane.set(whichOne, false);
                        landingPlanes.get(targetSlot).setVisible(true);
                        t.interrupt();
                    }
                }
            }
        }
    }
    
    public class TrackPath extends TimerTask {
        private int straightSpeed = 0; //only even speed otherwise too much big difference
        private int firstCurveSpeed = 0;
        private int secondCurveSpeed = 0;
        
        //directions that the plane is flying (in order)
        private boolean up = true;
        private boolean rightCurve = false;
        private boolean right = false;
        private boolean downCurve = false;
        private boolean down = false;
        private boolean leftCurve = false;
        private boolean left = false;
        private boolean upCurve = false;
        
        private int whatPlane = 0;
        private int planeLv = 0;
        private long timeTake = 0;

        private int firstRightDif = (startRightPoint.y-halfRightPoint.y)/9;
        private int secondRightDif = (halfRightPoint.y-endRightPoint.y)/8;
        private int firstDownDif = (halfDownPoint.y-startDownPoint.y)/8;
        private int secondDownDif = (endDownPoint.y-halfDownPoint.y)/9;
        private int firstLeftDif = (halfLeftPoint.y-startLeftPoint.y)/9;
        private int secondLeftDif = (endLeftPoint.y-halfLeftPoint.y)/8;
        private int firstUpDif = (startUpPoint.y-halfUpPoint.y)/8;
        private int secondUpDif = (halfUpPoint.y-endUpPoint.y)/9;

        public TrackPath(int whatPlane)
        {   
            this.whatPlane = whatPlane;
            planeLv = allPlanesLv.get(whatPlane);
            straightSpeed = flyingPlaneDisplacement.get(whatPlane);
            firstCurveSpeed = straightSpeed/2;
            secondCurveSpeed = firstCurveSpeed*2;
            timeTake = System.currentTimeMillis();
        }
        
        @Override 
        public void run() {
            if (up == true) {
                allPlaneLoc.get(whatPlane).set(0, endUpPoint.x);
                allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) - straightSpeed);
                if (allPlaneLoc.get(whatPlane).get(1) <= startRightPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, startRightPoint.x);
                    up = false;
                    rightCurve = true;
                }
            }
            else if (rightCurve == true) {
                if (allPlaneLoc.get(whatPlane).get(0) < halfRightPoint.x || allPlaneLoc.get(whatPlane).get(1) > halfRightPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) + firstCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) - secondCurveSpeed);
                }
                else {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) + secondCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) - firstCurveSpeed);
                }
                rotatePlane(startRightPoint.y, halfRightPoint.y, -firstRightDif, -secondRightDif, 0, -secondCurveSpeed, -firstCurveSpeed, whatPlane, 9, 8);
                if (allPlaneLoc.get(whatPlane).get(0) >= endRightPoint.x || allPlaneLoc.get(whatPlane).get(1) <= endRightPoint.y) {
                    rightCurve = false;
                    right = true;     
                    flyingPlaneImages.set(whatPlane, rotatePlaneImage(allFlyingPlanes[planeLv], 90));
                }
            }   
            else if (right == true) {
                allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) + straightSpeed);
                if (allPlaneLoc.get(whatPlane).get(0) >= startDownPoint.x) {
                    allPlaneLoc.get(whatPlane).set(1, startDownPoint.y);
                    right = false;
                    downCurve = true;
                }
            }
            else if (downCurve == true) {
                if (allPlaneLoc.get(whatPlane).get(0) < halfDownPoint.x || allPlaneLoc.get(whatPlane).get(1) < halfDownPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) + secondCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) + firstCurveSpeed);
                }
                else {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) + firstCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) + secondCurveSpeed);
                }
                rotatePlane(startDownPoint.y, halfDownPoint.y, firstDownDif, secondDownDif, 17, firstCurveSpeed, secondCurveSpeed, whatPlane, 8, 9);
                if (allPlaneLoc.get(whatPlane).get(0) >= endDownPoint.x || allPlaneLoc.get(whatPlane).get(1) >= endDownPoint.y) {
                    downCurve = false;
                    down = true;
                    flyingPlaneImages.set(whatPlane, rotatePlaneImage(allFlyingPlanes[planeLv], 180));
                }
            }
            else if (down == true) {
                allPlaneLoc.get(whatPlane).set(0, endDownPoint.x);       
                allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) + straightSpeed);
                //when plane passed the finished line - earn gold
                if (allPlaneLoc.get(whatPlane).get(1) > finishLinePoint.y && allPlaneLoc.get(whatPlane).get(1) <= (finishLinePoint.y + distanceTake.get(allPlanesLv.get(whatPlane)))) {
                    timeTake = (System.currentTimeMillis() - timeTake)/1000;
                    String goldEarnText = updateGold(whatPlane, timeTake); //calculate and update the gold earn
                    popUpTheFinishLine();
                    showGoldEarn(whatPlane, goldEarnText);
                    timeTake = System.currentTimeMillis();
                }
                if (allPlaneLoc.get(whatPlane).get(1) >= startLeftPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, startLeftPoint.x);
                    down = false;
                    leftCurve = true;
                }
            }
            else if (leftCurve == true) {
                if (allPlaneLoc.get(whatPlane).get(0) > halfLeftPoint.x || allPlaneLoc.get(whatPlane).get(1) < halfLeftPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) - firstCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) + secondCurveSpeed);
                }
                else {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) - secondCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) + firstCurveSpeed);
                }
                rotatePlane(startLeftPoint.y, halfLeftPoint.y, firstLeftDif, secondLeftDif, 34, secondCurveSpeed, firstCurveSpeed, whatPlane, 9, 8);
                if (allPlaneLoc.get(whatPlane).get(0) <= endLeftPoint.x || allPlaneLoc.get(whatPlane).get(1) >= endLeftPoint.y) {
                    leftCurve = false;
                    left = true;
                    flyingPlaneImages.set(whatPlane, rotatePlaneImage(allFlyingPlanes[planeLv], 270));
                }
            }
            else if (left == true) {
                allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) - straightSpeed);
                if (allPlaneLoc.get(whatPlane).get(0) <= startUpPoint.x) {
                    allPlaneLoc.get(whatPlane).set(1, startUpPoint.y);
                    left = false;        
                    upCurve = true;
                }
            }
            else if (upCurve == true) {
                if (allPlaneLoc.get(whatPlane).get(0) > halfUpPoint.x || allPlaneLoc.get(whatPlane).get(1) > halfUpPoint.y) {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) - secondCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) - firstCurveSpeed);
                }
                else {
                    allPlaneLoc.get(whatPlane).set(0, allPlaneLoc.get(whatPlane).get(0) - firstCurveSpeed);
                    allPlaneLoc.get(whatPlane).set(1, allPlaneLoc.get(whatPlane).get(1) - secondCurveSpeed);
                }
                rotatePlane(startUpPoint.y, halfUpPoint.y, -firstUpDif, -secondUpDif, 51, -firstCurveSpeed, -secondCurveSpeed, whatPlane, 8, 9);
                if (allPlaneLoc.get(whatPlane).get(0) <= endUpPoint.x || allPlaneLoc.get(whatPlane).get(1) <= endUpPoint.y) {
                    upCurve = false;
                    up = true;
                    flyingPlaneImages.set(whatPlane, new ImageIcon(allFlyingPlanes[planeLv]));             
                }
            }
            repaint(); 
        }
    }    
    
    private boolean checkDuplicateRotatedPlane(int lvCheckDup) {
        int count = 0;
        for (int j = 0; j < rotatedPlaneLevel.size(); j++) 
            if (rotatedPlaneLevel.get(j) == lvCheckDup)
                count++;
        if (count > 0)
            return true;
        return false;
    }
    //allFlyingPlanes flyingPlaneImages
    List<List<ImageIcon>> rotatedFlyingPlaneImages = new ArrayList<>();
    List<Integer> rotatedPlaneLevel = new ArrayList<>();
    private void initializeRotatedImageList() {
        rotateFlyingPlaneImages(0, 0);
    }
    private int findLocOfRotatedImages(int lv) {
        for (int j = 0; j < rotatedPlaneLevel.size(); j++) 
            if (rotatedPlaneLevel.get(j) == lv)
                return j;
        return -1;
    }
    public void rotateFlyingPlaneImages(int whatPlane, int lv)
    {
        RotatePlaneImage r = new RotatePlaneImage(whatPlane, lv);
    }
    public class RotatePlaneImage implements Runnable {
        int whatPlane, lv;
        Thread t;
        public RotatePlaneImage(int whatPlane, int lv) {
            this.whatPlane = whatPlane;
            this.lv = lv;
            t = new Thread(this);
            t.start();
        }
        public void run() {
            if (checkDuplicateRotatedPlane(lv) == false) {
                int difAngle = 0;
                int increAngle = 0;
                List<ImageIcon> t = new ArrayList<>();
                for (int k = 0; k < 68; k++) {
                    difAngle += 5;
                    t.add(rotatePlaneImage(allFlyingPlanes[lv], difAngle+increAngle));
                    if ((k+1) % 17 == 0) increAngle += 90;
                    if (difAngle == 85) difAngle = 0;
                    if (increAngle == 360) increAngle = 0;
                }        
                if (rotatedPlaneLevel.size() < 14) {
                    rotatedFlyingPlaneImages.add(t);
                    rotatedPlaneLevel.add(lv);
                }
                else {
                    int emptyLoc = findEmptyInRotatedPlaneImagesList();
                    rotatedFlyingPlaneImages.set(emptyLoc, t);
                    rotatedPlaneLevel.set(emptyLoc, lv);
                }
            }
        }
        public boolean checkAlive() {
            return t.isAlive();
        }
    }
    private int findEmptyInRotatedPlaneImagesList() { 
        for (int j = 0; j < rotatedPlaneLevel.size(); j++) {
            boolean check = false;
            for (int k = 0; k < allPlanesLv.size(); k++)
                if (rotatedPlaneLevel.get(j) == allPlanesLv.get(k))
                    check = true;
            if (check == false)
                return j;
        }
        System.out.println("find empty fuked");
        return -1;
    }

    private void rotatePlane(int point1, int point2, int firstDif, int secondDif, int angle, int speed1, int speed2, int plane, int way1, int way2)
    {
        int loc = findLocOfRotatedImages(allPlanesLv.get(plane));
        if (speed1 < 0) {
            for (int j = 0; j < way1; j++) 
                if (allPlaneLoc.get(plane).get(1) < point1 + firstDif*(j+1) && allPlaneLoc.get(plane).get(1) >= point1 + firstDif*(j+1) + speed1) {
                    flyingPlaneImages.set(plane, rotatedFlyingPlaneImages.get(loc).get(angle+j));
                    break;
                }
            for (int j = 0; j < way2; j++) 
                if (allPlaneLoc.get(plane).get(1) < point2 + secondDif*(j+1) && allPlaneLoc.get(plane).get(1) >= point2 + secondDif*(j+1) + speed2) {
                    flyingPlaneImages.set(plane, rotatedFlyingPlaneImages.get(loc).get(angle+way1+j));
                    break;
                }
        }
        else {
            for (int j = 0; j < way1; j++) 
                if (allPlaneLoc.get(plane).get(1) > point1 + firstDif*(j+1) && allPlaneLoc.get(plane).get(1) <= point1 + firstDif*(j+1) + speed1) {
                    flyingPlaneImages.set(plane, rotatedFlyingPlaneImages.get(loc).get(angle+j));
                    break;
                }
            for (int j = 0; j < way2; j++) 
                if (allPlaneLoc.get(plane).get(1) > point2 + secondDif*(j+1) && allPlaneLoc.get(plane).get(1) <= point2 + secondDif*(j+1) + speed2) {
                    flyingPlaneImages.set(plane, rotatedFlyingPlaneImages.get(loc).get(angle+way1+j));
                    break;
                }
        }
    }

    private ImageIcon rotatePlaneImage(String planeFileName, int rotateAngle)
    {
        try {
            BufferedImage initImage = ImageIO.read(new File(planeFileName));              
            double radians = Math.toRadians(rotateAngle);
            double sin = Math.abs(Math.sin(0));
            double cos = Math.abs(Math.cos(0));
            int w = (int) Math.floor(initImage.getWidth() * cos + initImage.getHeight() * sin);
            int h = (int) Math.floor(initImage.getHeight() * cos + initImage.getWidth() * sin);
            BufferedImage rotatedImage = new BufferedImage(w, h, initImage.getType());
            AffineTransform at = new AffineTransform();
            at.translate(w/2, h/2);
            at.rotate(radians,0, 0);
            at.translate(-initImage.getWidth()/2, -initImage.getHeight()/2);
            AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            rotateOp.filter(initImage, rotatedImage);
            return (new ImageIcon(rotatedImage));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error when rotating plane");
        }
        return new ImageIcon();
    }

    public class TotalGoldEarnPopUp implements Runnable {
        private int size = 23;
        Thread t;
        public TotalGoldEarnPopUp() {
            t = new Thread(this);
            t.start(); 
        }
        public void run() {
            int count = 0;
            while (!t.isInterrupted()) {
                if (count < 4) size += 2;
                else size -= 2;
                count++;
                Font f = new Font("Verdana", Font.PLAIN, size);
                goldLabel.setFont(f);
                try {
                    Thread.sleep(10);
                }
                catch(Exception e) {
                    System.out.println("Total gold pop up fuk");
                }
                if (size < 23) 
                    t.interrupt();
            }
        }
    }

    //update gold for what level of plane 
    private String updateGold(int whatPlane, long timeTake) 
    {
        String earn = planeGoldEarn.get(allPlanesLv.get(whatPlane)).multiply(new BigInteger(timeTake+"")).toString();
        totalGold = totalGold.add(new BigInteger(earn));
        String text = getTotalGoldText(totalGold);
        goldLabel.setText(text);
        goldLabel.setBounds(getGoldTextLocation(180, text), goldTextYLoc, 200, 60); 
        TotalGoldEarnPopUp t = new TotalGoldEarnPopUp();
        return earn;
    }

    public void updateGold(BigInteger amount) 
    {
        totalGold = totalGold.add(amount);
        String text = getTotalGoldText(totalGold);
        goldLabel.setText(text);
        goldLabel.setBounds(getGoldTextLocation(180, text), goldTextYLoc, 200, 60);
        TotalGoldEarnPopUp t = new TotalGoldEarnPopUp();
    }
    
    private int getGoldTextLocation(int xLoc, String text) {
        //205, 100 - 6 nums with symbol
        String longestStrExample = "789,789MM";
        FontMetrics textMeasurement = goldLabel.getFontMetrics(goldFont);
        int capLength = xLoc + textMeasurement.stringWidth(longestStrExample);
        return xLoc + (capLength - xLoc - textMeasurement.stringWidth(text))/2; 
    }
    
    //first three symbols are skipped for the ease of coding
    private String[] goldQuantitySymbols = { 
        "1", "1", "1", "K", "M", "B", "T", "a", "ab", "ac", "ad"
    };
    
    //If gold earn for a plane is too big:
    //use quantity symbols to present the value
    private String getTotalGoldText(BigInteger num) { 
        String text = num.toString(); 
        int maxNumDisplay = 6; //only 6 numbers will display
        if (text.length() > maxNumDisplay) { //if total gold has more than 6 numbers
            double result = text.length()/3.0;
            int remainder = text.length()%3;
            int symLength = 0;
            for (int j = 3; j < 100; j++) 
                if (result <= j) {
                    text = text + goldQuantitySymbols[j]; //get money symbol to represent high value
                    symLength = goldQuantitySymbols[j].length();
                    break;
                }
            //adding comma to gold value if more than 3 numbers
            if (remainder == 1) text = text.substring(0, 1) + "," + text.substring(1,4) + text.substring(text.length()-symLength);
            else if (remainder == 2) text = text.substring(0, 2) + "," + text.substring(2, 5) + text.substring(text.length()-symLength);
            else text = text.substring(0, 3) + "," + text.substring(3, 6) + text.substring(text.length()-symLength);
        }
        //adding comma to gold value if more than 3 numbers
        else if (text.length() > 3) text = text.substring(0, text.length()-3) + "," + text.substring(text.length()-3);
        return text;
    }
    
    public void scheduleFlyingPlaneSpeed(int whatPlane) //schedule task for flying plane
    {
        flyingPlaneSpeed.get(whatPlane).scheduleAtFixedRate(new TrackPath(whatPlane), straightDelay, flyingPlaneInterval.get(whatPlane));
    }

    private int returnFlyPlaneDelay = 5;
    private int currentReturnPlane = 0;
    private int returnXDistance = 0;
    private int returnYDistance = 0;
    private int returnFlyingPlaneStep = 0;
    public class StartReturningFlyingPlaneRun implements Runnable {
        Thread t;
        public StartReturningFlyingPlaneRun() {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while(!t.isInterrupted()) {
                allPlaneLoc.get(currentReturnPlane).set(0, allPlaneLoc.get(currentReturnPlane).get(0) + returnXDistance);
                allPlaneLoc.get(currentReturnPlane).set(1, allPlaneLoc.get(currentReturnPlane).get(1) + returnYDistance);
                repaint();
                returnFlyingPlaneStep++;
                try {
                    Thread.sleep(returnFlyPlaneDelay);
                }
                catch(InterruptedException e) {
                    System.out.println("Return flying plane thread sleep");
                }
                if (returnFlyingPlaneStep == 12) {
                    returnFlyingPlaneStep = 0;
                    landingPlanes.get(currentReturnPlane).setVisible(true);        //show the plane
                    allPlaneLoc.get(currentReturnPlane).set(0, 35);                //set current flying x loc to starting x loc
                    allPlaneLoc.get(currentReturnPlane).set(1, 350);               //set current flying y loc to starting y loc
                    flyingPlaneImages.set(currentReturnPlane, new ImageIcon());    //erase the plane image that was flying
                    flyingPlaneInterval.set(currentReturnPlane, 0);                //set the plane interval to 0
                    transLandingPlanes.get(currentReturnPlane).setVisible(false);  //hind transparent plane   
                    repaint(); 
                    t.interrupt();      
                }
            }
        }
    }

    private void startReturningFlyingPlane(int plane) {
        currentReturnPlane = plane;
        int xGoal = slotNumber.get(plane).x;
        int yGoal = slotNumber.get(plane).y;
        int xStart = allPlaneLoc.get(plane).get(0);
        int yStart = allPlaneLoc.get(plane).get(1);
        double xDif = Math.abs(xGoal - xStart);
        double yDif = Math.abs(yGoal - yStart);
        double step = 12.0;
        if (yGoal == yStart) returnYDistance = 0;
        else if (yGoal > yStart) returnYDistance = (int)Math.round(yDif/step);
        else returnYDistance = -(int)Math.round(yDif/step);
        if (xGoal == xStart) returnXDistance = 0;
        else if (xGoal > xStart) returnXDistance = (int)Math.round(xDif/step);
        else returnXDistance = -(int)Math.round(xDif/step);
        StartReturningFlyingPlaneRun r = new StartReturningFlyingPlaneRun();
    }

    ImageIcon occupied = new ImageIcon("FlyingSpace/OccupiedLv10.PNG");
    ImageIcon empty = new ImageIcon("FlyingSpace/EmptyLv10.PNG");
    ImageIcon opaqueOccupied = getOpaqueImage();
    private ImageIcon getOpaqueImage() {
            BufferedImage opaqueImage = new BufferedImage(occupied.getIconWidth(), occupied.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = opaqueImage.createGraphics();
            float opacity = 0.5f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(occupied.getImage(), 0, 0, null);
            g.dispose();
            return new ImageIcon(opaqueImage);
    };
    FlightPathLighting flightPathLight;
    public static volatile boolean showFlightPathLight = true;
    public void startLightingFlightPath() {
        flightPathLight = new FlightPathLighting();
        showFlightPathLight = true;
    }
    public void stopLightingFlightPath() {
        showFlightPathLight = false;
    }
    public class FlightPathLighting implements Runnable {
        private int pos = 0;
        Thread t;
        public FlightPathLighting() {
            t = new Thread(this);
            t.start();
        }
        public void run() {
            while(!t.isInterrupted()) {
                if (pos < 10) flyingSpaceIcons.set(pos, 2);
                if (pos < 11 && pos > 0) flyingSpaceIcons.set(pos-1, 1);
                if (pos < 12 && pos > 1) flyingSpaceIcons.set(pos-2, 2);
                if (pos < 13 && pos > 2) flyingSpaceIcons.set(pos-3, 0);
                repaint();
                pos++;
                if (pos >= 13) pos = 0;
                try {
                    Thread.sleep(140);
                }
                catch (InterruptedException e) {
                    System.out.println("At flightPathlighting sht");
                }
                if (!showFlightPathLight) {
                    for (int j = 1; j < flyingSpaceIcons.size(); j++) 
                        flyingSpaceIcons.set(j, 0);
                    t.interrupt();
                }
            }
        }
    }
    public static JLabel numberOfPlaneLabel = new JLabel();
    public static List<Integer> flyingSpaceIcons = new ArrayList<>();
    private void initializeFlyingSpace() {
        for (int j = 0; j < 10; j++) 
            flyingSpaceIcons.add(0);
    }

    public void mouseClicked(MouseEvent e)
    {   
        //System.out.println(e.getX() + ", " + e.getY());
        if (e.getComponent() instanceof JLabel) { //return flying plane when clicked on its transparent image
            for (int j = 0; j < transLandingPlanes.size(); j++) 
                if (transLandingPlanes.get(j) == (JLabel)(e.getComponent())) { //if click on transparent plane
                    flyingPlaneDisplacement.set(j, 0);            //set the plane displacement to 0
                    flyingPlaneSpeed.get(j).cancel();             //cancel the flying task
                    flyingPlaneSpeed.set(j, new java.util.Timer()); //initialize a new timer as it was terminated
                    startReturningFlyingPlane(j);
                    break;
                }
            //show number of plane flying throw icons and labels
            int checkTotal = -1;
            for (int j = 0; j < flyingPlaneInterval.size(); j++) 
                //using flying plane interval to check if the plane is flying or not
                if (flyingPlaneInterval.get(j) != 0) checkTotal++;
            flyingSpaceIcons.set(checkTotal, 0);
            numberOfPlaneLabel.setText(checkTotal+"/10");
            if (checkTotal == 9) numberOfPlaneLabel.setBounds(16,234,60,50);    
            if (checkTotal == 0) startLightingFlightPath();
        }
        if (boxHasDropped == true) { //if click on a box that is not open - open the box
            if (e.getX() >= dropLocationX-7 && e.getX() <= dropLocationX-7+(planeWidth+15) &&
                e.getY() >= finalDropLocationY-20 && e.getY() <= finalDropLocationY-20+(planeWidth+15)) {
                openBoxTime.stop();
                shakeBoxTime.start();
                boxHasDropped = false;
            }
        }
    }
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    //Make mouse pressed plane images appear in front 
    public void moveToFront(JLabel label)
    {
        for (int j = 0; j < landingPlanes.size(); j++) {
            if (landingPlanes.get(j) != label) {
                remove(landingPlanes.get(j));
                int x = slotNumber.get(j).x - planeWidth/2;
                int y = slotNumber.get(j).y - planeHeight/2;
                landingPlanes.get(j).setBounds(x, y, currentSpawnPlane.getIconWidth(), currentSpawnPlane.getIconHeight());
                add(landingPlanes.get(j));
                if (checkSlot.get(j) == false) landingPlanes.get(j).setVisible(false);
                this.revalidate();
                this.repaint();
            }
        }   
    }
    
    public void setPlaneLv(int whichPlane, int level)
    {
        ImageIcon newImage = new ImageIcon(new ImageIcon(allLandingPlanes[level]).getImage().getScaledInstance(planeWidth, planeHeight, Image.SCALE_SMOOTH));
        landingPlanes.get(whichPlane).setIcon(newImage);
        allPlanesLv.set(whichPlane, level);
    }
  
    public static String[] getAllLandingPlanes()
    {
        File planeFile = new File("PlanePics");
        String[] planeFileNames = planeFile.list();
        for (int j = 0; j < planeFileNames.length; j++) 
            planeFileNames[j] = planeFile.getName() + "/" + planeFileNames[j];
        return planeFileNames;
    }
    
    public static String[] getAllFlyingPlanes()
    {
        File flyingPlaneFile = new File("FlyingPlanePics");
        String[] flyingPlaneFileNames = flyingPlaneFile.list();
        for (int j = 0; j < flyingPlaneFileNames.length; j++) 
            flyingPlaneFileNames[j] = flyingPlaneFile.getName() + "/" + flyingPlaneFileNames[j];
        return flyingPlaneFileNames;
    }
    
    public void startSpawnBoxTime(){
        initialDropLocationY = 0;
        slotsAreFull = false;
        boxSpawnTime.start();
    }

    private void calculateGoldEarn() //calculate amount of gold earn for all planes
    {
        planeGoldEarn.add(new BigInteger("4"));
        planeGoldEarn.add(new BigInteger("9"));
        BigInteger[] t = new BigInteger[1]; //store the value and remainder of the value
        for (int j = 1; j < 59; j++) {
             planeGoldEarn.add(new BigInteger("1")); //initialize planeGoldEarn
             t = planeGoldEarn.get(j).multiply(BigInteger.valueOf(40)).divideAndRemainder(BigInteger.valueOf(19)); //caculating the gold earn of a plane
             double compare = t[1].doubleValue()/19.0; 
             if (compare >= 0.5) //if remainder of the value is bigger than 0.5 - plus one to the value
                planeGoldEarn.set(j+1, t[0].add(BigInteger.ONE));
             else planeGoldEarn.set(j+1, t[0]);
             String last = planeGoldEarn.get(j+1).toString(); //find the first number of the gold earn amount 
             if (last.substring(last.length()-1).equals("4")) //if first number is four - plus one (just a rule of assigning gold earn for each plane)
                planeGoldEarn.set(j+1, planeGoldEarn.get(j+1).add(BigInteger.ONE));
        }
    }
    
    private void calculateSpeed() //calculate the speed of all planes - call once
    {
        List<Double> values = new ArrayList<Double>();
        List<Integer> distanceTravel = new ArrayList<Integer>();
        List<Integer> timeTravel = new ArrayList<Integer>();
        double maxSpeed = 18.0/7.0;
        double minSpeed = 2.0/6.0;
        for (int j = 2; j <= 18; j += 2) //pick all qualified speed
            for (int k = 5; k <= 16; k++) { 
                double distance = (double)(j);
                double time = (double)(k);
                double speed = distance / time;
                if (speed <= maxSpeed && speed >= minSpeed) {
                    values.add(speed);
                    distanceTravel.add((int)distance);
                    timeTravel.add((int)time);
                }
            }
        for (int j = 0; j < values.size(); j++) //remove duplicate speed
            for (int k = j + 1; k < values.size(); k++) 
                if (values.get(j).equals(values.get(k))) {
                    values.remove(k);
                    distanceTravel.remove(k);
                    timeTravel.remove(k);
                    k--;
                }
        for(int i = 0; i < values.size()-1;i++) { //sort (selection sort) the list to ascedening
            int m = i;
            for(int j = i + 1; j < values.size();j++)
                if (values.get(m) > values.get(j))
                    m = j;
            double temp = values.get(i);
            values.set(i, values.get(m));
            values.set(m, temp);
            int temp1 = distanceTravel.get(i);
            distanceTravel.set(i, distanceTravel.get(m));
            distanceTravel.set(m, temp1);
            temp1 = timeTravel.get(i);
            timeTravel.set(i, timeTravel.get(m));
            timeTravel.set(m, temp1); 
        }
        distanceTake = new ArrayList<Integer>(distanceTravel);
        timeTake = new ArrayList<Integer>(timeTravel);
    }
    
    public void setFlyingPlaneImages(int loc, ImageIcon icon) 
    {
        flyingPlaneImages.set(loc, icon);
    }

    public void setTransLandingPlaneVisible(int slot, boolean state)
    {
        transLandingPlanes.get(slot).setVisible(state);
    }

    public void setLandingPlaneVisible(int slot, boolean state)
    {
        landingPlanes.get(slot).setVisible(state);
    }

    public void showBigTrashBin(boolean state) {
        bigTrashBin = state;
    }
}
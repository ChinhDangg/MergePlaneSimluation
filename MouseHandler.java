import java.awt.Point;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D; 
import java.awt.AlphaComposite;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.math.BigInteger;
public class MouseHandler extends MouseAdapter
{
    private Point offset;
    MergePlane merge;
    
    public MouseHandler(MergePlane merge) 
    {
        this.merge = merge;
    }

    public void mousePressed(MouseEvent e) 
    {
        JLabel label = (JLabel) e.getComponent();
        merge.moveToFront((JLabel) e.getSource()); //bring pressed image forward
        offset = e.getPoint();
        merge.showBigTrashBin(true); //enlarge the trash bin icon when a plane is pressed
            
        //show a transparent image of the plane 
        //when the plane is being pressed
        int slot = getCurrentSlot(label);
        if (slot != -1) {
            merge.transLandingPlanes.get(slot).setIcon(new ImageIcon(transparent((ImageIcon) merge.landingPlanes.get(slot).getIcon())));
            merge.setTransLandingPlaneVisible(slot, true);
        }
    }

    public void mouseDragged(MouseEvent e) 
    {
        int x = e.getPoint().x - offset.x;
        int y = e.getPoint().y - offset.y;
        Component component = e.getComponent();
        Point location = component.getLocation();
        location.x += x;
        location.y += y;
        component.setLocation(location);
    } 
    
    public void mouseReleased(MouseEvent e) 
    {
        merge.showBigTrashBin(false);
        merge.repaint();
        int x = e.getPoint().x - offset.x;
        int y = e.getPoint().y - offset.y;
        Component component = e.getComponent();
        Point location = component.getLocation();
        int thisXCenter = location.x + x + MergePlane.planeWidth/2;
        int thisYCenter = location.y + y + MergePlane.planeHeight/2;
        System.out.println((thisXCenter-MergePlane.planeWidth/2) + ", " + (thisYCenter-MergePlane.planeHeight/2));
        
        int targetSlot = -1;
        int mergeAreaX = MergePlane.planeWidth/2;
        int mergeAreaY = MergePlane.planeWidth/2;
            
        int slot = getCurrentSlot((JLabel)component);
        if (slot != -1) {
            Point initialPos = MergePlane.slotNumber.get(slot);
            merge.setTransLandingPlaneVisible(slot, false);
    
            for (int j = 0; j < MergePlane.slotNumber.size(); j++) { //finding the target slot number (what slot is the dragged plane being released in)
                int targetX = MergePlane.slotNumber.get(j).x;
                int targetY = MergePlane.slotNumber.get(j).y;
                if (thisXCenter >= targetX-mergeAreaX && thisXCenter <= targetX+mergeAreaX &&
                    thisYCenter >= targetY-mergeAreaY && thisYCenter <= targetY+mergeAreaY) {
                    targetSlot = j;
                    break;
                }
            }
            
            //merging mechanics
            if (targetSlot != MergePlane.randomSpot) { //making sure that the target slot does not have a plane inside the box
                if (targetSlot != -1) { //if dragged plane being moved into another slot
                    if (targetSlot != slot) { // if another slot is not the initial slot
                        if (merge.transLandingPlanes.get(targetSlot).isVisible() == false) {       //if another slot doesn't have a plane flying
                            if (merge.checkSlot.get(targetSlot) == true) {                         // if another slot has a plane 
                                if (merge.allPlanesLv.get(targetSlot) == merge.allPlanesLv.get(slot)) {         //if another plane has same level as dragged plane
                                    if (merge.allPlanesLv.get(targetSlot) != merge.allLandingPlanes.length-1) { // if the target planes are not max level
                                        merge.setLandingPlaneVisible(slot, false);             //hind plane in slot
                                        merge.landingPlanes.get(targetSlot).setVisible(false); //hind plane in targetSlot
                                        merge.checkSlot.set(slot, false);                      //mark plane in slot as empty
                                        merge.setPlaneLv(targetSlot, 1 + merge.allPlanesLv.get(targetSlot));    //increase lv of plane by 1 in target slot also update lv in allPlanesLv
                                        merge.startMergingAnimation(slot, targetSlot);                          //start merging animation at given target slot and with slot
                                        if (merge.slotsAreFull == true) merge.startSpawnBoxTime();              //start spawning box as all slots are no longer full after merging
                                        int targetPlaneLv = merge.allPlanesLv.get(targetSlot);
                                        merge.rotateFlyingPlaneImages(targetSlot, targetPlaneLv); //create rotated images if new plane for flying
                                        if (merge.getCurrentHighestLv() < targetPlaneLv) { //check for highest plane level after merging
                                            merge.setCurrentHighestPlane(targetPlaneLv);   //storing what is the highest plane
                                            merge.calculateNextPlanePercentage();
                                            if (merge.getCurrentHighestLv() % 4 == 0) merge.currentSpawnPlaneLv++;
                                        }
                                    }
                                    else { // if the target plane are max level - switch planes in each slot
                                        merge.startSwitchingAnimation(slot, targetSlot);
                                    } 
                                }
                                else { // if both planes are not the same level - switch planes in each slot
                                    merge.setLandingPlaneVisible(slot, false);
                                    merge.setLandingPlaneVisible(targetSlot, false);
                                    merge.startSwitchingAnimation(slot, targetSlot);
                                    
                                }
                            }
                            else { //if another slot does not have a plane
                                merge.setPlaneLv(targetSlot, merge.allPlanesLv.get(slot)); //set target slot lv to slot lv also update all plane lv in allPlanesLv
                                merge.landingPlanes.get(slot).setVisible(false);           //hind plane in chosen slot
                                merge.landingPlanes.get(targetSlot).setVisible(true);      //show plane in target slot
                                merge.checkSlot.set(slot, false);                          //mark slot as empty
                                merge.checkSlot.set(targetSlot, true);                     //mark target slot as not empty
                            }
                        }
                    } 
                }
                //flying merchanics
                else if (thisXCenter >= 10 && thisXCenter <= 58 && thisYCenter <= 528 && thisYCenter >= 215) { //if plane is released in flying zone
                    int checkTotal = 0;
                    for (int j = 0; j < merge.flyingPlaneInterval.size(); j++) 
                        //using flying plane interval to check if the plane is flying or not
                        if (merge.flyingPlaneInterval.get(j) != 0) checkTotal++;
                    if (checkTotal != 10) { //if flying planes total is not 10
                        merge.stopLightingFlightPath();
                        merge.numberOfPlaneLabel.setText((checkTotal+1)+"/10");
                        merge.flyingSpaceIcons.set(checkTotal, 1);
                        if (checkTotal+1 == 10) merge.numberOfPlaneLabel.setBounds(10,234,60,50);
                        checkTotal = 0;
                        //get flying plane image same level as the landing plane(dragged one)
                        ImageIcon icon = new ImageIcon(new ImageIcon(merge.allFlyingPlanes[merge.allPlanesLv.get(slot)]).getImage().getScaledInstance(merge.planeWidth, merge.planeHeight, Image.SCALE_SMOOTH));
                        merge.setFlyingPlaneImages(slot, icon);  //set flying slot image
                        merge.flyingPlaneDisplacement.set(slot, merge.distanceTake.get(merge.allPlanesLv.get(slot))); //set displacement of flying plane in slot
                        merge.flyingPlaneInterval.set(slot, merge.timeTake.get(merge.allPlanesLv.get(slot)));         //set interval of flying plane in slot
                        merge.scheduleFlyingPlaneSpeed(slot);    //set the flying plane speed in slot
                        merge.flyPlane = true; //at least one plane is flying
                        merge.landingPlanes.get(slot).setVisible(false);     //hind landing plane 
                        merge.transLandingPlanes.get(slot).setVisible(true); //show a transparent image of landing plane
                    }
                }
                //selling plane
                else if (thisXCenter >= 205 && thisXCenter <= 253 && thisYCenter >= 770 && thisYCenter <= 820) {
                    merge.setLandingPlaneVisible(slot, false);             //hind plane in slot
                    merge.checkSlot.set(slot, false);                      //mark plane in slot as empty
                    if (merge.slotsAreFull == true) merge.startSpawnBoxTime();   //start spawning box as all slots are no longer full after sellin
                    merge.updateGold(merge.planeGoldEarn.get(merge.allPlanesLv.get(slot)).divide(new BigInteger("2")));    //earn half amount of gold
                    merge.calculateNextPlanePercentage(); 
                    merge.createTrashDust();
                    merge.createTrashCoins();
                }
            }
            //set the dragged plane to initial location after being released from the mouse.
            component.setLocation(initialPos.x - merge.planeWidth/2, initialPos.y - merge.planeHeight/2); //set dragged plane to initial location 
        }
    }
    
    private BufferedImage transparent(ImageIcon image) //make an image half transparent
    {
        BufferedImage transparentImage = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) transparentImage.getGraphics();
        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.drawImage(image.getImage(),0,0,null);
        g.dispose();
        return transparentImage;
    }
    
    public int getCurrentSlot(JLabel plane) //find what slot is this plane is in
    {
        for (int j = 0; j < merge.landingPlanes.size(); j++) 
            if (plane == merge.landingPlanes.get(j) || plane == merge.transLandingPlanes.get(j)) 
                return j;
        return -1;
    }
}

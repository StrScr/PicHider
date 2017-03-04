package pichider;

import java.awt.image.BufferedImage;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author oscar
 */
public class PicHide {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        final JFileChooser fc = new JFileChooser();
        //Start File Chooser Set-Up
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Select image...");
        fc.setFileHidingEnabled(false);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileNameExtensionFilter("Image Files","jpg","jpeg","png","gif"));
        //End File Chooser Set-Up

        //test thing
        BufferedImage testIMG=null;

        int op;
        boolean exit=false;
        while(!exit){
            System.out.println("PicHider");
            System.out.println("========");
            System.out.println("1 - Hide text in image");
            System.out.println("2 - Hide image in image [X]");
            System.out.println("3 - Reveal text in image");
            System.out.println("4 - Reveal image in image [X]");
            System.out.println("5 - Quit");
            op=OptInput(5);
            switch(op){
                case 1:{//Hide text in image
                    System.out.println("Hide Text in Image");
                    System.out.println("==================");
                    System.out.println("1 - Input Text First");
                    System.out.println("2 - Choose Image First [X]");
                    op=OptInput(2);
                    String hidetext;
                    if(op==1){//Input Text First
                        System.out.print("Input string to hide: ");
                        hidetext = sc.nextLine();
                        System.out.println("Need at least a "+Math.ceil((hidetext.length()*8+8)/3.0)+"px 32-bit color image or "+(hidetext.length()*8+8)+"px grayscale image.");
                        if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                            BufferedImage img = null;
                            try{
                                img = ImageIO.read(fc.getSelectedFile());
                            }catch(Exception e){
                                System.out.println("Error opening image.");
                                break;
                            }
                            boolean isGrayscale = img.getRaster().getNumDataElements()==1;
                            System.out.println((isGrayscale?"Grayscale":"Color")+" image found.");
                            if(img.getHeight()*img.getWidth()*(isGrayscale?1:3)<(hidetext.length()*8+8)){
                                System.out.println("Image not big enough! "+img.getHeight()*img.getWidth()*(isGrayscale?1:3)+" elements found, "
                                        + "but "+(hidetext.length()*8+8)+" elements needed! Operation cancelled.");
                                break;
                            }else{
                                //ACTUAL HIDING PROCESS CALLED
                                hideTextInImage(hidetext, img, isGrayscale);
                                testIMG=img;
                                //img passed as reference, no return needed
                            }
                            System.out.println("Hiding succesful. Saving modified image...");
                            if(fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
                                try{
                                    ImageIO.write(img, "png", fc.getSelectedFile());
                                }catch(Exception e){
                                    System.out.println("Error writing to file.");
                                }
                            }else{
                                System.out.println("Image save cancelled.");
                            }
                        }else{
                            System.out.println("Image open cancelled.");
                        }
                    }else{//Choose image first
                        if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                            
                        }
                    }
                    break;
                }
                case 2:{//Hide image in image
                    break;
                }
                case 3:{//Reveal text in image
                    
                    //test thing
                    //System.out.println("Text found: "+RevealTextInImage(testIMG));
                    
                    if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                        BufferedImage img = null;
                        try{
                            img = ImageIO.read(fc.getSelectedFile());
                        }catch(Exception e){
                            System.out.println("Error opening image.");
                            break;
                        }
                        System.out.println("Text found: "+RevealTextInImage(img));
                    }else{
                        System.out.println("Image open cancelled.");
                    }
                    break;
                }
                case 4:{//Reveal image in image
                    break;
                }
                default:{//Quit
                    exit = true;
                }
            }
            System.out.println("Done.");
            System.out.println("========");
        }
    }
    
    //OptInput: Offers numerical input to user, until a number from 1 to maxopt is input.
    static int OptInput(int maxopt){
        maxopt = Math.max(1, maxopt);
        int opin;
        Scanner sc = new Scanner(System.in);
        do{
            System.out.print("Input: ");
            try{
                opin = sc.nextInt();
            }catch(Exception e){
                System.out.println("Numerical input expected. Try again.");
                opin = 0;
            }
            sc = new Scanner(System.in);
        }while(opin<1 || opin>maxopt);
        return opin;
    }
    
    //Hides text within an image, assuming it fits. No validation is done, so previous validation is expected.
    static void hideTextInImage(String hidetext, BufferedImage img, boolean isGrayscale){
        RGB c = null;
        int chr;
        for(int i = 0; i<(hidetext.length()*8+8); i++){
            if(i<hidetext.length()*8){
                chr = (((int)hidetext.charAt(i/8))>>(i%8))%2;
            }else{
                chr = 0;
            }
            if(isGrayscale){//grayscale
                c = new RGB(img.getRGB(i%img.getWidth(), i/img.getWidth()));
                c.setAllBits(chr);
                img.setRGB(i%img.getWidth(),i/img.getWidth(),c.getColorInt());
            }else{//color
                switch(i%3){
                    case 0:{
                        c = new RGB(img.getRGB((i/3)%img.getWidth(), (i/3)/img.getWidth()));
                        c.setRedBit(chr);
                        break;
                    }
                    case 1:{
                        c.setGreenBit(chr);
                        break;
                    }
                    default:{
                        c.setBlueBit(chr);
                        img.setRGB((i/3)%img.getWidth(),(i/3)/img.getWidth(),c.getColorInt());
                        break;
                    }
                }
                if(i==(hidetext.length()*8+7)){
                    img.setRGB((i/3)%img.getWidth(),(i/3)/img.getWidth(),c.getColorInt());
                }
            }
        }
    }
    
    //Attempts to find text within an image. Will stop at a null character.
    static String RevealTextInImage(BufferedImage img){
        boolean isGrayscale = img.getRaster().getNumDataElements()==1;
        boolean stringFound = false;
        System.out.println((isGrayscale?"Grayscale":"Color")+" image found.");
        String res = "";
        int chr = 0;
        RGB c = null;
        for(int i=0; i<img.getHeight()*img.getWidth()*(isGrayscale?1:3); i++){
            //chr<<=1; //first try
            if(isGrayscale){
                c = new RGB(img.getRGB(i%img.getWidth(), i/img.getWidth()));
                chr += (c.r%2)<<(i%8);
            }else{
                switch(i%3){
                    case 0:{
                        c = new RGB(img.getRGB((i/3)%img.getWidth(), (i/3)/img.getWidth()));
                        chr += (c.r%2)<<(i%8);
                        break;
                    }
                    case 1:{
                        chr += (c.g%2)<<(i%8);
                        break;
                    }
                    default:{
                        chr += (c.b%2)<<(i%8);
                        break;
                    }
                }
            }
            if((i+1)%8==0){//Assuming 8bit ASCII characters
                if(chr==0){
                    stringFound=true;
                    break;
                }
                res += (char)chr;
                chr = 0;
            }
        }
        if(stringFound && res.length()==0){
            System.out.println("No text found?");
        }
        if(!stringFound){
            System.out.println("No proper hidden text found. Possible garbage.");
        }
        return res;
    }
}

class RGB{
    int r=0;
    int g=0;
    int b=0;
    
    public RGB(int red, int green, int blue){
        r=red%256;
        g=green%256;
        b=blue%256;
    }
    
    public RGB(int colorint){
        b=colorint%256;
        colorint>>=8;
        g=colorint%256;
        colorint>>=8;
        r=colorint%256;
    }
    
    public int getColorInt(){
        int c=r;
        c<<=8;
        c+=g;
        c<<=8;
        c+=b;
        return c;
    }
    
    public void setAllBits(int bit){
        bit%=2;
        r-=r%2;
        g-=g%2;
        b-=b%2;
        r+=bit;
        g+=bit;
        b+=bit;
    }
    
    public void setRedBit(int bit){
        bit%=2;
        r-=r%2;
        r+=bit;
    }
    
    public void setGreenBit(int bit){
        bit%=2;
        g-=g%2;
        g+=bit;
    }
    
    public void setBlueBit(int bit){
        bit%=2;
        b-=b%2;
        b+=bit;
    }
}
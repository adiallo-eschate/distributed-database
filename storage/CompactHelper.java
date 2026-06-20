import java.util.*;


class CompactHelper extends TimerTask {

   TieredCompaction compactor;
   public static int i = 0;

   CompactHelper(TieredCompaction compactor){
      this.compactor = compactor;
   }
    
   public void run(){
      System.out.println("Timer ran: " + i++);
      System.out.println("Compact Daemon Running...");
      this.compactor.compact();
    }

}

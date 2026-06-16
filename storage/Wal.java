class Wal {
    Stack<String> walStack = new Stack<>();
    String filePath = "temp.wal";
    BufferedWriter w;
    BufferedReader r;

    void printStack(){
       
       System.out.println("------ Start Print ------");
       for (String item : walStack){
            System.out.println(item);
       }
       System.out.println("------- End Print --------");
       
    }

    
    void setWalStack(String key){
        this.walStack.push(key);
        System.out.println("set good '" + this.walStack.peek() + "'");
    }

    String getWalStack(){
        return this.walStack.pop();
    }

    
    void write(String key){
        this.walStack.push(key);
    }


    void replayWal(){
        
        try(BufferedReader r = new BufferedReader(new FileReader(this.filePath))){

            String line;
            while((line = r.readLine()) != null){
               System.out.println("Read Line: " + line);
            }
         

            r.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void flushStack(){

        try(BufferedWriter w = new BufferedWriter(new FileWriter(this.filePath, true))){
            
            while(!this.walStack.empty()){
                w.write(this.walStack.pop());
                w.newLine();
            }

            w.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
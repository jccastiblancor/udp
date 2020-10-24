public class test {

    public static void main(String[] args) throws Exception {

        for (int i=0; i<25;i++){
            clientTest thread=new clientTest();
            thread.start();
        }
    }
}

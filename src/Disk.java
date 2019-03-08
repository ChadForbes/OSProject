//Disk class for OS Project
//Declare RAM separately in a diff class
public class Disk {
    List<String> diskMemory = new ArrayList<String>();

    public void Loader(String fileName){
        BufferedReader read = new BufferedReader(new FileReader(fileName));
        //While the next line is not null
        while((String line = read.readLine()) != null) {
            diskMemory.add(line);
            //System.out.println(line);
        }
        read.close();
    }

    public String getOpcodeAt(int index){
        return diskMemory[index];
    }
//    public static void main(String[]args){
//        Disk d = new Disk();
//        d.Loader("asdf.txt");
//    }
}

//If we want to change this ArrayList to String[], use this
//String[] disk = diskMemory.toArray(new String[]{});
import java.io.*;
import java.util.*;

public class FileSimilarity {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        // Create a map to store the fingerprint for each file
        Map<String, List<Long>> fileFingerprints = new HashMap<>();

        ArrayList<Thread> threads = new ArrayList<Thread>();
        
        // Calculate the fingerprint for each file
        for (String path : args) {
            Thread t = new Thread(() -> {
                try {
                    List<Long> fingerprint = fileSum(path);
                    putSync(fileFingerprints, path, fingerprint);
                } catch (Exception e) {
                }
            });

            threads.add(t);
            t.start();
        }

        for (Thread t : threads){
            t.join();
        }

        // Compare each pair of files
        for (int i = 0; i < args.length; i++) {
            for (int j = i + 1; j < args.length; j++) {
                int ti = i;
                int tj = j;

                Thread t = new Thread(() -> {
                    String file1 = args[ti];
                    String file2 = args[tj];
                    List<Long> fingerprint1 = fileFingerprints.get(file1);
                    List<Long> fingerprint2 = fileFingerprints.get(file2);
                    float similarityScore = similarity(fingerprint1, fingerprint2);
                    System.out.println("Similarity between " + file1 + " and " + file2 + ": " + (similarityScore * 100) + "%");
                });
                
                t.start();
            }
        }
    }

    private static synchronized void putSync(Map<String, List<Long>> fileFingerprints, String path , List<Long> fingerprint){
        fileFingerprints.put(path, fingerprint); 
    } 

    private static List<Long> fileSum(String filePath) throws IOException {
        File file = new File(filePath);
        List<Long> chunks = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[100];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                long sum = sum(buffer, bytesRead);
                chunks.add(sum);
            }
        }
        return chunks;
    }

    private static long sum(byte[] buffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Byte.toUnsignedInt(buffer[i]);
        }
        return sum;
    }

    private static float similarity(List<Long> base, List<Long> target) {
        int counter = 0;
        List<Long> targetCopy = new ArrayList<>(target);

        for (Long value : base) {
            if (targetCopy.contains(value)) {
                counter++;
                targetCopy.remove(value);
            }
        }

        return (float) counter / base.size();
    }
}
